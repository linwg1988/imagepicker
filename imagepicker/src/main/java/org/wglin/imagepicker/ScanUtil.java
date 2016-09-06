package org.wglin.imagepicker;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by wengui on 2016/9/5.
 */
public class ScanUtil {
    public static void scanFile(Context context, String path, File myCaptureFile, String imagename) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            MediaScannerConnection.scanFile(context, new String[]{path}, null, null);
        } else {
            try {
                MediaStore.Images.Media.insertImage(context.getContentResolver(),
                        myCaptureFile.getAbsolutePath(), imagename, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
        }
    }
}
