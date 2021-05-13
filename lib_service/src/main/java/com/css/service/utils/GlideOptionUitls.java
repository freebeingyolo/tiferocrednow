package com.css.service.utils;

import com.blankj.utilcode.util.SizeUtils;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.request.RequestOptions;
import com.css.service.R;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;


public class GlideOptionUitls {

    public static RequestOptions getDefault() {
        RequestOptions options = new RequestOptions();
//        options.placeholder(R.drawable.ic_placeholer1);
//        options.error(R.drawable.ic_placeholer1);
        return options;
    }

    /**
     * @return  默认配置，圆角5dp
     */
    public static RequestOptions getCornerOptions() {
        RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(SizeUtils.dp2px(5), 0, RoundedCornersTransformation.CornerType.ALL);
        RequestOptions options = getDefault().transforms(new CenterCrop(), roundedCornersTransformation);
        return options;
    }


    /**
     * @param cornerDp 圆角dp
     * @return
     */
    public static RequestOptions getCornerOptions(int cornerDp) {
        RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(SizeUtils.dp2px(cornerDp), 0, RoundedCornersTransformation.CornerType.ALL);
        RequestOptions options = getDefault().transforms(new CenterCrop(), roundedCornersTransformation);
        return options;
    }
    public static RequestOptions getCornerOptions2(int cornerDp) {
        RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(SizeUtils.dp2px(cornerDp), 0, RoundedCornersTransformation.CornerType.ALL);
        RequestOptions options = getDefault().transforms(new CenterInside(), roundedCornersTransformation);
        return options;
    }

    /**
     * @return 左边圆角 5dp
     */
    public static RequestOptions getLeftCornerOptions() {
        RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(SizeUtils.dp2px(5), 0, RoundedCornersTransformation.CornerType.LEFT);
        RequestOptions options = getDefault().transforms(new CenterCrop(), roundedCornersTransformation);
        return options;
    }

    /**
     * @return 顶部圆角 5dp
     */
    public static RequestOptions getTopCornerOptions() {
        RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(SizeUtils.dp2px(5), 0, RoundedCornersTransformation.CornerType.TOP);
        RequestOptions options = getDefault().transforms(new CenterCrop(), roundedCornersTransformation);
        return options;
    }

    /**
     * @return 顶部圆角 10dp
     */
    public static RequestOptions getTop10dpCornerOptions() {
        RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(SizeUtils.dp2px(10), 0, RoundedCornersTransformation.CornerType.TOP);
        RequestOptions options = getDefault().transforms(new CenterCrop(), roundedCornersTransformation);
        return options;
    }

    public static RequestOptions getCornerOptions(int dp, RoundedCornersTransformation.CornerType cornerType) {
        RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(SizeUtils.dp2px(5), 0,cornerType);
        RequestOptions options = getDefault().transforms(new CenterCrop(), roundedCornersTransformation);
        return options;
    }


    public static RequestOptions getBitmapTransformation() {
        RequestOptions options = getDefault().bitmapTransform(new BlurTransformation(15, 4));  // “23”：设置模糊度(在0.0到25.0之间)，默认”25";"4":图片缩放比例,默认“1”。
        return options;
    }



}
