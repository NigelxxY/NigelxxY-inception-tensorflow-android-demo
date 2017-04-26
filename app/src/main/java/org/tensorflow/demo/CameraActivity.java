package org.tensorflow.demo;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;



public class CameraActivity extends Activity {
  private static final int TAKE_PHOTO = 1;
  private static final int CROP_PHOTO = 2;
  private static final int CHOOSE_PHOTO = 3;
  private Uri imageUri;
  private String filename;
  private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
  private static final String LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";
  private static final int NUM_CLASSES = 1001;
  private static final int INPUT_SIZE= 224;
  private static final int IMAGE_MEAN = 117;
  private static final float IMAGE_STD = 1;
  private static final String INPUT_NAME = "input:0";
  private static final String OUTPUT_NAME = "output:0";
  private final TensorFlowClassifier tensorflow = new TensorFlowClassifier();
  private TextView mResultText;
  private String UrlPath = "http://112.74.61.160:8080/learningSpringMVC-1.0-SNAPSHOT/image/";

  Handler handler = new Handler(){
    public void handleMessage(Message msg){
      switch (msg.what){
        case 1:
          Gson gson = new Gson();
          Info info = gson.fromJson((String)msg.obj,Info.class);
          AdapterList adapterList = new AdapterList(CameraActivity.this,info);
          ListView listView = (ListView)findViewById(R.id.list);
          listView.setAdapter(adapterList);
          break;
        case 0:
          Toast.makeText(CameraActivity.this,"ERROR",Toast.LENGTH_LONG).show();
          break;
        default:
          break;
      }
    }
  };

  @Override
  public void onCreate(Bundle saveInstanceState){
    super.onCreate(saveInstanceState);
    setContentView(R.layout.activity_camera);

    final AssetManager assetManager = getAssets();
    tensorflow.initializeTensorFlow(assetManager,MODEL_FILE,LABEL_FILE,NUM_CLASSES,INPUT_SIZE,IMAGE_MEAN,
            IMAGE_STD,INPUT_NAME,OUTPUT_NAME);

    Button button = (Button)findViewById(R.id.b01);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        SimpleDateFormat format =  new SimpleDateFormat("yyyyMMddHHmmss");
        Date date  = new Date(System.currentTimeMillis());
        filename = format.format(date);
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File outputImage = new File(path,filename+".jpg");
        try{
          if(outputImage.exists())
            outputImage.delete();
          outputImage.createNewFile();
        }catch (IOException e){
          e.printStackTrace();
        }
        imageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,TAKE_PHOTO);
      }
    });
    Button button1 = (Button)findViewById(R.id.b02);
    button1.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,CHOOSE_PHOTO);
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data){
    super.onActivityResult(requestCode,resultCode,data);
    if(resultCode!=RESULT_OK) {
      Toast.makeText(CameraActivity.this, "Activity resultCode error", Toast.LENGTH_SHORT).show();
      return;
    }
    switch (requestCode){
      case TAKE_PHOTO:
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imageUri,"image/*");
        intent.putExtra("scale",true);
        intent.putExtra("aspectX",1);
        intent.putExtra("aspectY",1);
        intent.putExtra("outputX",340);
        intent.putExtra("outputY",340);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        Intent ibc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        ibc.setData(imageUri);
        this.sendBroadcast(ibc);
        startActivityForResult(intent,CROP_PHOTO);
        break;
      case CHOOSE_PHOTO:
        Uri uri = data.getData();
        Log.i("uro",uri.toString());
        ContentResolver cr = this.getContentResolver();
        try{
          Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
          dealPic(bitmap);
        }catch(FileNotFoundException e){
          Log.e("Exception",e.getMessage(),e);
        }
        break;
      case CROP_PHOTO:
        try{
          Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
          dealPic(bitmap);
        }catch (FileNotFoundException e){
          e.printStackTrace();
        }
        break;
      default:
        break;
    }
  }
  //处理图片
  private void dealPic(Bitmap bitmap){
    ImageView imageView = (ImageView)findViewById(R.id.iv01);
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    float scaleWidth = ((float)INPUT_SIZE)/width;
    float scaleHeight = ((float)INPUT_SIZE)/height;
    Matrix matrix = new Matrix();
    matrix.postScale(scaleWidth,scaleHeight);
    Bitmap newbm = Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
    imageView.setImageBitmap(newbm);
    final List<Classifier.Recognition> results = tensorflow.recognizeImage(newbm);
    final String title = getLable(results.get(0).getTitle());
    Thread t = new Thread(){
      @Override
      public  void run(){
        HttpURLConnection conn = null;
        try{
          URL url = new URL(UrlPath+title);
          conn = (HttpURLConnection)url.openConnection();
          conn.setRequestMethod("GET");
          conn.setConnectTimeout(10*1000);
          conn.setReadTimeout(10*1000);
          conn.connect();
          if(conn.getResponseCode() == 200){
            InputStream is = conn.getInputStream();
            Message msg = new Message();
            msg.obj = HttpUtils.jsonData(is);
            msg.what = 1;
            handler.sendMessage(msg);
          }
          else{
            Message msg = handler.obtainMessage();
            msg.what=0;
            handler.sendMessage(msg);
          }
        }catch (Exception e){
          e.printStackTrace();
        }finally {
          conn.disconnect();
        }
      }
    };
    t.start();
  }
  private String getLable(String result){
    switch (result){
      case "mouse":
        return "mouse";
      case "computer keyboard":
      case "typewriter keyboard":
        return "keyboard";
      case "Polaroid camera":
      case "reflex camera":
        return "camera";
      case "desktop computer":
      case "laptop":
        return "laptop";
      default:
        return "book";
    }
  }
}

