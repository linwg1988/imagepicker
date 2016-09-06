package com.rs.framework;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.wglin.imagepicker.ImagePicker;

import java.util.List;

/**
 * Created by wengui on 2016/9/5.
 */
public class MainActivity extends AppCompatActivity implements ImagePicker.OnImagePickerListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImagePicker.setImageLoader(new GlideImageLoader());
        findViewById(R.id.btnDialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });
        findViewById(R.id.btnLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replace();
            }
        });
    }

    private void replace() {
        ImagePicker.Builder builder = new ImagePicker.Builder();
        ImagePicker imagePicker = builder.openCamera(true).hideTitleBar()
                .maxPictureNumber(6).build();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_container, imagePicker).commit();
    }

    private void show() {
        ImagePicker.Builder builder = new ImagePicker.Builder();
        builder.openCamera(true);
        builder.maxPictureNumber(6);
        builder.build().show(getSupportFragmentManager(), "ImagePicker");
    }

    @Override
    public void onImagesPicked(List<String> imgPaths, String selectedDir) {
        TextView tvResult = (TextView) findViewById(R.id.tvResult);
        tvResult.setText("imgPaths:" + imgPaths.toString() + "\nselectedDir:" + selectedDir);
    }

    @Override
    public void onCameraCallBack(String imgPath) {
        TextView tvResult = (TextView) findViewById(R.id.tvResult);
        tvResult.setText("imgPath:" + imgPath);
    }
}
