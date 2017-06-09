package org.tensorflow.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class SnapActivity extends Activity implements SurfaceHolder.Callback{
    private static String TAG = "SnapActivity";
    private Camera mCamera;
    private ImageButton mButton0,mButton1,mButton2;
    private SurfaceView mSurfaceView;
    private SurfaceHolder holder;
    private Camera.AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
    private String path = "biyesheji";//图片所在文件夹名
    private String path1;
    private Bitmap bmp;
    private Calendar c;
    private ArrayList<String> imageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_snap);
        mSurfaceView = (SurfaceView)findViewById(R.id.mSurfaceView);
        mSurfaceView.setZOrderOnTop(false);
        holder = mSurfaceView.getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(SnapActivity.this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mButton0 = (ImageButton)findViewById(R.id.myButton);
        mButton1 = (ImageButton)findViewById(R.id.myButton1);
        mButton2 = (ImageButton)findViewById(R.id.myButton2);
        mButton0.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                mCamera.autoFocus(mAutoFocusCallback);
            }
        });

        mButton1.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                if(bmp!=null){
                    if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
                        Toast.makeText(SnapActivity.this,"no sd card",Toast.LENGTH_LONG).show();
                    }
                    else{
                        try {
                            c = Calendar.getInstance();
                            File f = new File(Environment.getExternalStorageDirectory(),path);
                            if(!f.exists()){
                                f.mkdir();
                            }
                            path1 = String.valueOf(c.get(Calendar.MILLISECOND))+"camera.jpg";
                            File n = new File(f,path1);
                            FileOutputStream bos = new FileOutputStream(n.getAbsolutePath());
                            bmp.compress(Bitmap.CompressFormat.JPEG,100,bos);
                            bos.flush();
                            bos.close();
                            imageList.add(n.getAbsolutePath());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                mButton0.setVisibility(View.VISIBLE);
                mButton1.setVisibility(View.VISIBLE);
                mButton2.setVisibility(View.VISIBLE);
                initCamera();
            }
        });
        mButton2.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent();
                intent.setClass(SnapActivity.this,CameraActivity.class);
                intent.putExtra("imageList",imageList);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }
    @Override
    public void surfaceCreated(SurfaceHolder surfaceholder) {
        try {
                    /* 打开相机， */
            mCamera = Camera.open();
            mCamera.setPreviewDisplay(holder);
            Log.i(TAG,"create camera---");
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w,
                               int h) {
            /* 相机初始化 */
        Log.i(TAG,"init camera");
        initCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        Log.i(TAG,"destoryed camera");
        stopCamera();
        mCamera.release();
        mCamera = null;
    }

    /* 拍照的method */
    private void takePicture() {
        if (mCamera != null) {
            Log.i(TAG,"takePicture");
            mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
        }
    }
    private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {

        }
    };

    private Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] _data, Camera _camera) {
        }
    };

    private Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] _data, Camera _camera) {
                    /* 取得相仞 */
            try {
                            /* 设定Button可见性 */
                mButton0.setVisibility(View.VISIBLE);
                mButton1.setVisibility(View.VISIBLE);
                mButton2.setVisibility(View.VISIBLE);
                            /* 取得Bitmap对象 */
                Bitmap bm = BitmapFactory.decodeByteArray(_data, 0, _data.length);
                int height = bm.getHeight();
                int width = bm.getWidth();
                int INPUT_SIZE = CameraActivity.dp2px(80);
                float scaleWidth = ((float) INPUT_SIZE) / width;
                float scaleHeight = ((float) INPUT_SIZE) / height;
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                matrix.postRotate(90);
                bmp = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /* 告定义class AutoFocusCallback */
    public final class AutoFocusCallback implements
            android.hardware.Camera.AutoFocusCallback {
        public void onAutoFocus(boolean focused, Camera camera) {
                    /* 对到焦点拍照 */
            if (focused) {
                takePicture();
            }
        }
    };

    /* 相机初始化的method */
    private void initCamera() {
        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();

                parameters.setPictureFormat(PixelFormat.JPEG);
                parameters.setPictureSize(1024, 768);
                mCamera.setDisplayOrientation(90);
                mCamera.setParameters(parameters);
                            /* 开启预览画面 */
                mCamera.startPreview();
                Log.i(TAG, "init camera!!!!!!------");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* 停止相机的method */
    private void stopCamera() {
        if (mCamera != null) {
            try {
                            /* 停止预览 */
                mCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
