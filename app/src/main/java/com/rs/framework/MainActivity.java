package com.rs.framework;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.wglin.imagepicker.ImagePicker;

import java.util.ArrayList;
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},123);
        }else{
            new ImagePicker.Builder().openCamera(true).maxPictureNumber(6).build().show(getSupportFragmentManager(), "imagePicker");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 123){
            List<String> deniedPermissions = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++)
            {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                {
                    deniedPermissions.add(permissions[i]);
                }
            }
            if (deniedPermissions.size() > 0)
            {
                Toast.makeText(this,"获取权限失败",Toast.LENGTH_SHORT).show();
            } else
            {
                new ImagePicker.Builder().maxPictureNumber(6).openCamera(true).build().show(getSupportFragmentManager(), "imagePicker");
            }
        }
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
