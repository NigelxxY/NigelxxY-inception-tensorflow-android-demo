package org.tensorflow.demo;

import android.content.Context;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import org.insta.IF1977Filter;
import org.insta.IFAmaroFilter;
import org.insta.IFBrannanFilter;
import org.insta.IFEarlybirdFilter;
import org.insta.IFHefeFilter;
import org.insta.IFHudsonFilter;
import org.insta.IFInkwellFilter;
import org.insta.IFLomofiFilter;
import org.insta.IFLordKelvinFilter;
import org.insta.IFNashvilleFilter;
import org.insta.IFNormalFilter;
import org.insta.IFRiseFilter;
import org.insta.IFSierraFilter;
import org.insta.IFSutroFilter;
import org.insta.IFToasterFilter;
import org.insta.IFValenciaFilter;
import org.insta.IFWaldenFilter;
import org.insta.IFXproIIFilter;
import org.insta.InstaFilter;
/**
 * Created by Nigel_xxY on 2017/6/8.
 */

public class FilterHelper extends GPUImageFilter {
    private FilterHelper() {}

    private static final int FILTER_NUM = 18;
    private static InstaFilter[] filters;

    public static InstaFilter getFilter(Context context, int index) {
        if (filters == null) {
            filters = new InstaFilter[FILTER_NUM];
        }
        try {
            switch (index){
                case 0:
                    filters[index] = new IFNormalFilter(context);
                    break;
                case 1:
                    filters[index] = new IFAmaroFilter(context);
                    break;
                case 2:
                    filters[index] = new IFRiseFilter(context);
                    break;
                case 3:
                    filters[index] = new IFHudsonFilter(context);
                    break;
                case 4:
                    filters[index] = new IFXproIIFilter(context);
                    break;
                case 5:
                    filters[index] = new IFSierraFilter(context);
                    break;
                case 6:
                    filters[index] = new IFLomofiFilter(context);
                    break;
                case 7:
                    filters[index] = new IFEarlybirdFilter(context);
                    break;
                case 8:
                    filters[index] = new IFSutroFilter(context);
                    break;
                case 9:
                    filters[index] = new IFToasterFilter(context);
                    break;
                case 10:
                    filters[index] = new IFBrannanFilter(context);
                    break;
                case 11:
                    filters[index] = new IFInkwellFilter(context);
                    break;
                case 12:
                    filters[index] = new IFWaldenFilter(context);
                    break;
                case 13:
                    filters[index] = new IFHefeFilter(context);
                    break;
                case 14:
                    filters[index] = new IFValenciaFilter(context);
                    break;
                case 15:
                    filters[index] = new IFNashvilleFilter(context);
                    break;
                case 16:
                    filters[index] = new IF1977Filter(context);
                    break;
                case 17:
                    filters[index] = new IFLordKelvinFilter(context);
                    break;
            }
        } catch (Throwable e) {
        }
        return filters[index];
    }

    public static void destroyFilters() {
        if (filters != null) {
            for (int i = 0; i < filters.length; i++) {
                try {
                    if (filters[i] != null) {
                        filters[i].destroy();
                        filters[i] = null;
                    }
                } catch (Throwable e) {
                }
            }
        }
    }
}
