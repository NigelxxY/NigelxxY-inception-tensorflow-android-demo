package org.tensorflow.demo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Nigel_xxY on 2017/6/6.
 */

public class FoodModel implements Serializable{
    private String imageUri;
    private List<Classifier.Recognition> result;
    public String getImageUri(){
        return imageUri;
    }
    public List<Classifier.Recognition> getResult() {
        return result;
    }
    public void setImageUri(String imageUri){
        this.imageUri = imageUri;
    }
    public void setResult(List<Classifier.Recognition> recognitions){
        this.result = recognitions;
    }
}
