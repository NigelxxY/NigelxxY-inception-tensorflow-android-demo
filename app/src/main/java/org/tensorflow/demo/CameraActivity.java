package org.tensorflow.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;


public class CameraActivity extends Activity implements View.OnClickListener{
    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";
    private static final int NUM_CLASSES = 1001;
    private static final int INPUT_SIZE= 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input:0";
    private static final String OUTPUT_NAME = "output:0";
    private final TensorFlowClassifier tensorflow = new TensorFlowClassifier();

    private static final int TAKE_PHOTO = 1;
    private static final int FILTE = 2;
    private static final int CHOOSE_PHOTO = 3;
    private int now_seleceed = -1;
    private static Context context;
    private ListView listView;
    private Button filter,wechat_share,choosephoto_button,takephoto_button;
    private AdapterList adapterList;
    private ArrayList<String> imageList = new ArrayList<>();
    private ArrayList<FoodModel> imageResultList = new ArrayList<>();

    Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    FoodModel tmp_foodmodel = (FoodModel)msg.obj;
                    imageResultList.add(tmp_foodmodel);
                    adapterList = new AdapterList(CameraActivity.this,imageResultList);
                    listView.setAdapter(adapterList);
                    break;
                case 0:
                    Toast.makeText(CameraActivity.this,"ERROR",Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    FoodModel end_foodmodel = (FoodModel)msg.obj;
                    imageResultList.add(end_foodmodel);
                    adapterList = new AdapterList(CameraActivity.this,imageResultList);
                    listView.setAdapter(adapterList);
                default:
                    break;
            }
        }
    };


    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_camera);
        final AssetManager assetManager = getAssets();
        tensorflow.initializeTensorFlow(assetManager,MODEL_FILE,LABEL_FILE,NUM_CLASSES,INPUT_SIZE,IMAGE_MEAN, IMAGE_STD,INPUT_NAME,OUTPUT_NAME);
        initView();
    }

    private void initView() {
        context=getApplicationContext();
        listView = (ListView)findViewById(R.id.image_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String image_filepath = imageResultList.get(i).getImageUri();
                now_seleceed = i;
                Intent intent = new Intent();
                intent.setClass(CameraActivity.this,FilterActivity.class);
                intent.putExtra("image_filepath",image_filepath);
                startActivityForResult(intent,FILTE);
            }
        });
        filter = (Button)findViewById(R.id.image_filter_button);
        filter.setOnClickListener(this);
        filter.setVisibility(View.GONE);
        wechat_share = (Button)findViewById(R.id.image_wechat_button);
        wechat_share.setVisibility(View.GONE);
        wechat_share.setOnClickListener(this);
        takephoto_button = (Button)findViewById(R.id.imagelist_takephotos);
        takephoto_button.setOnClickListener(this);
        choosephoto_button = (Button)findViewById(R.id.imagelist_pictures);
        choosephoto_button.setOnClickListener(this);
    }
    public static int dp2px(float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Toast.makeText(CameraActivity.this, "Activity resultCode error", Toast.LENGTH_SHORT).show();
            return;
        }
        wechat_share.setVisibility(View.VISIBLE);
        filter.setVisibility(View.VISIBLE);
        switch (requestCode) {
            case TAKE_PHOTO:
                imageList = data.getStringArrayListExtra("imageList");
                imageClassification();
                break;
            case FILTE:
                String newimagepath = data.getStringExtra("newimagepath");
                imageResultList.get(now_seleceed).setImageUri(newimagepath);
                adapterList = new AdapterList(this,imageResultList);
                listView.setAdapter(adapterList);
                break;
            case CHOOSE_PHOTO:
                imageList = data.getStringArrayListExtra("paths");
                imageClassification();
                break;
            default:
                break;
        }
    }
    private void imageClassification(){
        int size = imageList.size();
        for (int i=0 ;i<size;i++){
            final String filepath_tmp = imageList.get(i);
            new Thread(){
                public void run(){
                    try {
                        Bitmap bitmap = BitmapFactory.decodeFile(filepath_tmp);
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();
                        float scaleWidth = ((float)INPUT_SIZE)/width;
                        float scaleHeight = ((float)INPUT_SIZE)/height;
                        Matrix matrix = new Matrix();
                        matrix.postScale(scaleWidth,scaleHeight);
                        Bitmap newbm = Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
                        final List<Classifier.Recognition> results = tensorflow.recognizeImage(newbm);
                        FoodModel food_tmp = new FoodModel();
                        food_tmp.setImageUri(filepath_tmp);
                        food_tmp.setResult(results);
                        Message message = new Message();
                        message.what = 1;
                        message.obj = food_tmp;
                        handler.sendMessage(message);
                    }catch (Exception e){
                        Message message = new Message();
                        message.what = 0;
                        handler.sendMessage(message);
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }
    public void onClick(View view){
        switch (view.getId()){
            case R.id.imagelist_takephotos:
                Intent intent = new Intent();
                intent.setClass(CameraActivity.this,SnapActivity.class);
                startActivityForResult(intent,TAKE_PHOTO);
                break;
            case R.id.image_filter_button:
                Intent intent2 = new Intent();
                intent2.setClass(CameraActivity.this,ActivityCamera.class);
                startActivityForResult(intent2,FILTE);
            case R.id.imagelist_pictures:
                Intent intent1 = new Intent();
                intent1.setClass(CameraActivity.this,SelectPictureActivity.class);
                startActivityForResult(intent1,CHOOSE_PHOTO);
                break;
            case R.id.image_wechat_button:
                Toast.makeText(getApplicationContext(),"动态分享",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

}

