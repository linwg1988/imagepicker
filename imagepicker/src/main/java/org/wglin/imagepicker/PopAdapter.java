package org.wglin.imagepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

@SuppressLint("InflateParams")
class PopAdapter extends BaseAdapter {
    private final ImageLoader imageLoader;
    private Context context;
    private List<ImageFolder> imgFolder;
    /**
     * At the first this is the largest dir ,and then it is the choice.
     */
    private File expandDir;

    public PopAdapter(Context context, ImageLoader imageLoader, List<ImageFolder> imgFolder, File expandDir) {
        this.context = context;
        this.imageLoader = imageLoader;
        this.imgFolder = imgFolder;
        this.expandDir = expandDir;
    }

    public void setExpandDir(File expandDir) {
        this.expandDir = expandDir;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_dir_item_select_image, null);
        }
        TextView itemName = ViewHolderUtils.get(convertView, R.id.id_dir_item_name);
        TextView itemCount = ViewHolderUtils.get(convertView, R.id.id_dir_item_count);
        ImageView itemImage = ViewHolderUtils.get(convertView, R.id.id_dir_item_image);
        ImageView itemChoose = ViewHolderUtils.get(convertView, R.id.iv_item_choose);

        if (expandDir.getAbsolutePath().equals(imgFolder.get(position).getDir())) {
            itemChoose.setImageResource(R.drawable.img_picker_dir_choose);
        } else {
            itemChoose.setImageResource(R.drawable.transition);
        }

        itemName.setText(imgFolder.get(position).getName());
        itemCount.setText(imgFolder.get(position).getCount() + context.getResources().getString(R.string.piece));
        imageLoader.loadImage(context, "file://" + imgFolder.get(position).getFirstImagePath(),
                itemImage);
        return convertView;
    }

    @Override
    public int getCount() {
        return imgFolder.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}