package org.tensorflow.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nigel_xxY on 2017/4/23.
 */

public class AdapterList extends BaseAdapter {

    private Activity activity;
    private Info data;
    private static LayoutInflater inflater = null;
    public AdapterList(Activity a,Info d){
        activity = a;
        data = d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return data.getData().size();
    }

    @Override
    public Object getItem(int i) {
        return data.getData().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vi = view;
        if(view==null)
            vi = inflater.inflate(R.layout.list_raw,null);
        TextView des = (TextView)vi.findViewById(R.id.des);
        TextView price = (TextView)vi.findViewById(R.id.price);
        TextView type = (TextView)vi.findViewById(R.id.type);
        ImageView imageView = (ImageView)vi.findViewById(R.id.list_image);

        des.setText(data.getData().get(i).getDescription());
        price.setText(data.getData().get(i).getPrice()+"");
        type.setText(data.getData().get(i).getType());
        byte[] imageByte = data.getData().get(i).getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte,0,imageByte.length);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float)100)/width;
        float scaleHeight = ((float)100)/height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);
        Bitmap newBm = Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
        imageView.setImageBitmap(newBm);
        return vi;
    }
}
