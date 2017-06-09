package org.tensorflow.demo;

/**
 * Created by Nigel_xxY on 2017/6/8.
 */

import android.content.Context;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import android.media.MediaScannerConnection;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;

import butterknife.BindView;

import com.astuetz.PagerSlidingTabStrip;

import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import jp.co.cyberagent.android.gpuimage.GPUImageView;

import org.insta.InstaFilter;

public class FilterActivity extends AppCompatActivity implements View.OnClickListener,ViewPager.OnPageChangeListener, Target, Handler.Callback {

    private static final int MSG_SWITH_FILTER = 1001;
    private static final int MSG_SAVE_IMAGE = 1002;

    @BindView(R.id.image) GPUImageView mImageView;

    @BindView(R.id.pager) ViewPager pager;

    @BindView(R.id.tabs) PagerSlidingTabStrip tabs;

    @BindView(R.id.link) TextView link;

    FilterPageAdapter adapter;

    HandlerThread thread;

    Handler handler;
    File input_image;

    int mFilter;
    int mWidth;

    String image_filepath ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter1);
        ButterKnife.bind(this);

        WindowManager w = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display d = w.getDefaultDisplay();
        int rotation = d.getRotation();
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            mWidth = d.getWidth();
        } else {
            mWidth = d.getHeight();
        }
        Intent intent = getIntent();
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(mWidth, mWidth);
        mImageView.setLayoutParams(p);
        image_filepath = intent.getStringExtra("image_filepath");
        input_image = new File(image_filepath);
        Log.e("image_filepath",image_filepath);

        adapter = new FilterPageAdapter(getSupportFragmentManager());
        pager.setOffscreenPageLimit(0);
        pager.setAdapter(adapter);
        tabs.setViewPager(pager);
        tabs.setOnPageChangeListener(this);
        link.setOnClickListener(this);

        thread = new HandlerThread("filter");
        thread.start();
        handler = new Handler(thread.getLooper(), this);
        Picasso.with(this).load(input_image).into(this);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.link:
                saveBitmap();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (thread != null) thread.quit();
        FilterHelper.destroyFilters();
    }

    private class FilterPageAdapter extends FragmentPagerAdapter {

        private final int[] TITLES = {
                R.string.filter_normal,
                R.string.filter_amaro,
                R.string.filter_rise,
                R.string.filter_hudson,
                R.string.filter_xproii,
                R.string.filter_sierra,
                R.string.filter_lomo,
                R.string.filter_earlybird,
                R.string.filter_sutro,
                R.string.filter_toaster,
                R.string.filter_brannan,
                R.string.filter_inkwell,
                R.string.filter_walden,
                R.string.filter_hefe,
                R.string.filter_valencia,
                R.string.filter_nashville,
                R.string.filter_1977,
                R.string.filter_lordkelvin
        };

        public FilterPageAdapter(FragmentManager fm) {
            super(fm);
        }

        public CharSequence getPageTitle(int position) {
            return getString(TITLES[position]);
        }

        public int getCount() {
            return TITLES.length;
        }

        public Fragment getItem(int position) {
            return new DummyFragment();
        }
    };

    private class DummyFragment extends Fragment {

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return new TextView(FilterActivity.this);
        }

    };

    public void onPageScrollStateChanged(int state){
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void onPageSelected(int position) {
        if (position != mFilter) {
            handler.removeMessages(MSG_SWITH_FILTER);
            Message m = handler.obtainMessage(MSG_SWITH_FILTER);
            m.arg1 = position;
            handler.sendMessage(m);
        }
    }

    public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
        mImageView.setImage(bitmap);
    }

    public void onBitmapFailed(Drawable errorDrawable) {
    }

    public void onPrepareLoad(Drawable placeHolderDrawable) {
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SWITH_FILTER:
                int index = msg.arg1;
                mFilter = index;
                try {
                    InstaFilter filter = FilterHelper.getFilter(this, mFilter);
                    if (filter != null) {
                        mImageView.setFilter(filter);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
            case MSG_SAVE_IMAGE:
                saveBitmap();
                break;
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.save:
                handler.sendMessage(handler.obtainMessage(MSG_SAVE_IMAGE));
                break;
        }
        return true;
    }

    void saveBitmap() {
        BufferedOutputStream bos = null;
        try {

            String rootDir = "";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            } else {
                rootDir = getFilesDir().getAbsolutePath();
            }

            if (!TextUtils.isEmpty(rootDir)) {
                String saveDir = rootDir + File.separator + "biyesheji";
                File dir = new File(saveDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String tmpFilePath = saveDir + File.separator + System.currentTimeMillis() + ".jpg";
                Log.e("imagepath",tmpFilePath);
                File tmpFile = new File(tmpFilePath);
                Bitmap bitmap = mImageView.capture();
                bos = new BufferedOutputStream(new FileOutputStream(tmpFile.getAbsolutePath()));
                bitmap.compress(Bitmap.CompressFormat.JPEG,100, bos);
                bitmap.recycle();
                MediaScannerConnection.scanFile(this, new String[]{tmpFilePath}, new String[]{"image/jpg"}, null);
                Toast.makeText(this, getString(R.string.save_tip, tmpFilePath), Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra("newimagepath",tmpFilePath);
                setResult(RESULT_OK,intent);
                finish();
            }

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (Exception e2) {
                }
            }
        }
    }

}
