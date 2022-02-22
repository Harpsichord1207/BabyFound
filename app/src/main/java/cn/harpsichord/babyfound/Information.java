package cn.harpsichord.babyfound;

import android.content.res.Resources;

import java.io.Serializable;

public class Information implements Serializable {

    public int id;
    public String imageURL;
    public Detail detail;
    public double longitude;
    public double latitude;

    public static class Detail implements Serializable {

        public String name;
        public String detail;
        public String dateStr;
        public String contact;
        public String address;

        public Detail(String text) {
            int i = 0;
            for (String part: text.split("\\*#\\*")) {
                i += 1;
                if (i == 1) {
                    name = part;
                } else if (i == 2) {
                    detail = part;
                } else if (i == 3) {
                    dateStr = part;
                } else if (i == 4) {
                    contact = part;
                } else if (i == 5) {
                    address = part;
                }
            }
        }
    }

    public String getName() {
        return "姓名：" + detail.name;
    }

    public String getDetail() {
        return "详情：" + detail.detail;
    }

    public String getDate() {
        return "走失时间：" + detail.dateStr;
    }

    public String getContact() {
        return "联系方式：" + detail.contact;
    }

    public String getAddress() {
        return detail.address;
    }

    public String getDetailDisplay() {
        return getName() + "\n" + getDetail() + "\n" + getDate() + "\n" + getContact() + "\n" + getAddress();
    }

}

