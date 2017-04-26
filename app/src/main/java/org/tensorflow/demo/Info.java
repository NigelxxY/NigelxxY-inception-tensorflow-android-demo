package org.tensorflow.demo;

import java.util.List;

public class Info {
    private String status;
    private String message;
    private List<DataBean> data;

    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus(){
        return this.status;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public List<DataBean> getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public static class DataBean {
        private String id;
        private String description;
        private double price;
        private byte[] image;
        private String type;

        public void setImage(byte[] image) {
            this.image = image;
        }

        public byte[] getImage() {
            return image;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getPrice() {
            return price;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}