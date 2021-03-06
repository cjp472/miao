package com.liuzhuni.lzn.core.goods.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Andrew Lee on 2015/4/24.
 * E-mallprice:jieooo7@163.com
 * Date: 2015-04-24
 * Time: 16:10
 */
public class GoodsModel implements Serializable {

    private String img;
    private String title;
    private String price;
    private String mallname;
    private String code;
    private String discount;
    private String range;
    private String priceOld;
    private String url;
    private boolean isLowest;
    private boolean  remind;
    private ArrayList<ShopModel> mallprice;

    public GoodsModel(String img, String title, String price, String mallname, String discount, String range, String priceOld, String url, boolean isLowest, ArrayList<ShopModel> mallprice) {
        this.img = img;
        this.title = title;
        this.price = price;
        this.mallname = mallname;
        this.discount = discount;
        this.range = range;
        this.priceOld = priceOld;
        this.url = url;
        this.isLowest = isLowest;
        this.mallprice = mallprice;
    }


    public boolean isRemind() {
        return remind;
    }

    public void setRemind(boolean remind) {
        this.remind = remind;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isLowest() {
        return isLowest;
    }

    public void setLowest(boolean isLowest) {
        this.isLowest = isLowest;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getMallname() {
        return mallname;
    }

    public void setMallname(String mallname) {
        this.mallname = mallname;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getPriceOld() {
        return priceOld;
    }

    public void setPriceOld(String priceOld) {
        this.priceOld = priceOld;
    }

    public ArrayList<ShopModel> getMallprice() {
        return mallprice;
    }

    public void setMallprice(ArrayList<ShopModel> mallprice) {
        this.mallprice = mallprice;
    }
}
