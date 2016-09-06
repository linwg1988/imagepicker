package com.rs.framework;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.wglin.imagepicker.ImageLoader;

/**
 * Created by wengui on 2016/9/5.
 */
public class GlideImageLoader implements ImageLoader {
    @Override
    public void loadImage(Context context, String imgPath, ImageView targetView) {
        Glide.with(context).load(imgPath).placeholder(R.drawable.img_picker_default_img)
                .dontAnimate().error(R.drawable.transition).into(targetView);
    }
}
