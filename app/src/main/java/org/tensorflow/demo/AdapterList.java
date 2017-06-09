package org.tensorflow.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nigel_xxY on 2017/4/23.
 */

public class AdapterList extends BaseAdapter {

    private Activity activity;
    private viewHolder holder;
    private ArrayList<FoodModel> data;
    private static LayoutInflater inflater = null;
    public AdapterList(Activity a,ArrayList<FoodModel> data){
        activity = a;
        this.data = data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vi = view;
        if(view==null) {
            vi = inflater.inflate(R.layout.list_raw, null);
            holder = new viewHolder();
            holder.imageView = (ImageView)vi.findViewById(R.id.image_main);
            holder.topNum = (TextView)vi.findViewById(R.id.top_number);
            holder.lable = (TextView)vi.findViewById(R.id.picture_name);
            holder.confindence=(TextView)vi.findViewById(R.id.confidence);
            vi.setTag(holder);
        }else{
            holder=(viewHolder)vi.getTag();
        }
        Bitmap bm = BitmapFactory.decodeFile(data.get(i).getImageUri());
        holder.imageView.setImageBitmap(bm);
        List<Classifier.Recognition> tmp = data.get(i).getResult();
        if(tmp==null){
            holder.lable.setText("无分类结果");
        }else {
            String result_title = "";
            String result_confidence  = "置信度为：";
            int tmp_size = tmp.size();
            for(int j=0;j<tmp_size;j++){
                result_title = result_title +"/" + tmp.get(j).getTitle();
                result_confidence = result_confidence + "/"+tmp.get(j).getConfidence();
            }
            holder.topNum.setText("分类结果为：");
            holder.lable.setText(result_title);
            holder.confindence.setText(result_confidence);
        }
        return vi;
    }
    static class viewHolder{
        public ImageView imageView;
        public TextView topNum;
        public TextView lable;
        public TextView confindence;
    }

}
