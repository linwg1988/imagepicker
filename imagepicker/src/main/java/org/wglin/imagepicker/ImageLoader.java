package org.wglin.imagepicker;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by wengui on 2016/9/5.
 */
public interface ImageLoader {
    void loadImage(Context context, String imgPath, ImageView targetView);
}
