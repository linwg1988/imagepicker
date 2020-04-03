package org.wglin.imagepicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by wengui on 2016/2/26.
 */
public class ImagePicker extends DialogFragment {
    private static final String PATH = "/Android/Data/imgPicker/";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 20;
    private static ImageLoader imageLoader;
    private boolean isUseByDialog;
    private boolean isPickVideo = false;


    public static void setImageLoader(ImageLoader imageLoader) {
        ImagePicker.imageLoader = imageLoader;
    }

    /**
     * Number of picture in dir.
     */
    private int mPicsSize;
    /**
     * At the first this is the largest dir ,and then it is the choice.
     */
    private File mImgDir;

    private String mImgDirPath;
    /**
     * All of the pictures.
     */
    private List<String> mImgs = new ArrayList<String>();
    /**
     * Tem helper to prevent rescan of a file.
     */
    private HashSet<String> mDirPaths;

    private Map<String, Long> videoDurationMap = new HashMap<>();

    private final int REQUEST_CAMERA = 100;
    /**
     * If start photo by camera, this path string will be callback by listener
     * while the image is done;
     */
    private String imagePath;
    /**
     * User for whether open camera to pick a now time picture;
     */
    private boolean openCamera;
    /**
     * Once we want use ImagePicker as a fragment only,set this false to hide titleBar.
     */
    private boolean supportTitle = true;
    /**
     * From 4.0 support webp picture.Wo won't load webp pic default.
     */
    boolean supportWebp;
    /**
     * Limit of the picked picture
     */
    private int maxPictureNumber;
    /**
     * The hole path of the user choices.
     */
    public ArrayList<String> mSelectedImage;
    /**
     * Scan sdcard and get all folders that contains image.
     */
    private List<ImageFolder> imgFolder = new ArrayList<>();

    private GridView mGirdView;

    private RelativeLayout mBottomLy;

    private TextView mChooseDir;
    private TextView mImageCount;
    int totalCount = 0;

    private Handler mHandler = new Handler();

    //filename.endsWith(".jpg")
    //                                || filename.endsWith(".png")
    //                                || filename.endsWith(".jpeg")
    //                                || filename.endsWith(".gif")
    //                                || filename.endsWith(".webp");
    private ArrayList<String> fileTypeList = new ArrayList<>(Arrays.asList(".jpg", ".png", ".jpeg", ".gif"));
    private ArrayList<String> videoFileType = new ArrayList<>(Arrays.asList(".avi", ".mp4", ".mpe", ".mpeg", ".mpg", ".mpg4"));

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            imgProgressBar.setVisibility(View.GONE);
            getImages();
            dataView();
            initListDirPopupWindow();
        }
    };

    private ImageView backButton;

    private TextView selectNum;

    private ImageAdapter imageAdapter;

    private PopupWindow imagePopWindow;

    private ProgressBar imgProgressBar;

    private PopAdapter popAdapter;

    public ImagePicker() {
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar);
    }

    Context mContent;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof OnImagePickerListener)) {
            throw new UnsupportedOperationException("ImagePicker must call by an activity who implements OnImagePickerListener!");
        }
        if (imageLoader == null) {
            generateImageLoader();
        }
        this.mContent = context;
    }

    private void generateImageLoader() {
        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(Context context, String imgPath, ImageView targetView) {

            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        this.mSelectedImage = arguments.getStringArrayList("mSelectedImage");
        this.supportWebp = arguments.getBoolean("supportWebp");
        this.isPickVideo = arguments.getBoolean("isPickVideo");
        this.maxPictureNumber = arguments.getInt("maxPictureNumber");
        this.openCamera = arguments.getBoolean("openCamera");
        this.mImgDirPath = arguments.getString("mImgDirPath");
        this.supportTitle = arguments.getBoolean("supportTitleBar");
        if (savedInstanceState != null) {
            mSelectedImage = savedInstanceState.getStringArrayList("mSelectedImage");
            imagePath = savedInstanceState.getString("imagePath");
            mImgDirPath = savedInstanceState.getString("mImgDirPath");
        }
        if (isPickVideo) {
            openCamera = false;
        }
        return inflater.inflate(R.layout.fragment_select_image, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        init(view);
    }

    private void init(View view) {
        if (!supportTitle) {
            view.findViewById(R.id.img_picker_title_bar).setVisibility(View.GONE);
        }

        backButton = view.findViewById(R.id.img_picker_title_bar_left_menu);
        selectNum = view.findViewById(R.id.img_picker_title_bar_right_text);
        selectNum.setVisibility(View.VISIBLE);
        imgProgressBar = view.findViewById(R.id.img_picker_pb_load_img);

        selectNum.setText(mSelectedImage.size() + "/" + maxPictureNumber + getResources().getString(R.string.done));

        mGirdView = view.findViewById(R.id.img_picker_grid);
        mChooseDir = view.findViewById(R.id.img_picker_choose_dir);
        mImageCount = view.findViewById(R.id.img_pickertv_total_count);
        mBottomLy = view.findViewById(R.id.img_picker_bottom_ly);
        TextView tvTitle = view.findViewById(R.id.img_pick_title_text);
        if (isPickVideo) {
            tvTitle.setText(R.string.img_picker_picking_video);
            mChooseDir.setText(R.string.img_picker_all_video);
        }
        mBottomLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePopWindow.setAnimationStyle(R.style.anim_popup_dir);
                imagePopWindow.showAsDropDown(mBottomLy, 0, 0);

                WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                lp.alpha = .3f;
                getActivity().getWindow().setAttributes(lp);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
            }
        });

        selectNum.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mSelectedImage.size() == 0) {
                    toast(getResources().getString(isPickVideo ? R.string.img_picker_no_video_choiced : R.string.no_pic_choiced));
                    return;
                }
                FragmentActivity ac = getActivity();
                if (ac != null) {
                    ((OnImagePickerListener) ac).onImagesPicked(mSelectedImage, mImgDir.getAbsolutePath());
                }
                dismissAllowingStateLoss();
            }
        });
        mHandler.post(mRunnable);
    }

    private void toast(String content) {
        T.show(getContext(), content, Toast.LENGTH_SHORT);
    }

    private void dataView() {
        if (mImgDirPath != null) {
            mImgDirPath = mImgDirPath + "/temp.jpg";
            mImgDir = new File(mImgDirPath).getParentFile();
        }
        if (popAdapter != null) {
            popAdapter.setExpandDir(mImgDir);
            popAdapter.notifyDataSetChanged();
        }
        if (mImgDir == null) {
            T.show(getContext(), R.string.forder_not_exist, Toast.LENGTH_SHORT);
            imageAdapter = new ImageAdapter();
            mGirdView.setAdapter(imageAdapter);
            return;
        }

        String[] list = listFile();
        sortFile(list);
        imageAdapter = new ImageAdapter();
        mGirdView.setAdapter(imageAdapter);
        mChooseDir.setText(mImgDir.getAbsolutePath().substring(mImgDir.getAbsolutePath().lastIndexOf("/")));
        mImageCount.setText(mImgs.size() + getResources().getString(isPickVideo ? R.string.img_picker_qty : R.string.piece));
    }

    private void getImages() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            toast(getResources().getString(R.string.no_extra_storage));
            return;
        }

        String firstImage = null;

        Uri mImageUri = isPickVideo ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = getActivity().getContentResolver();

        // 4.0+ support webp picture
        String selection = "";
        String[] selectionArgs;
        if (isPickVideo) {
            selection = MediaStore.Images.Media.MIME_TYPE + "=? or "
                    + MediaStore.Images.Media.MIME_TYPE + "=?  or "
                    + MediaStore.Images.Media.MIME_TYPE + "=?";
            selectionArgs = new String[]{"video/x-msvideo", "video/mp4", "video/mpeg"};
        } else {
            if (supportWebp) {
                selection = MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?";
                selectionArgs = new String[]{"image/jpeg", "image/png", "image/gif", "image/webp"};
            } else {
                selection = MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?  or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?";
                selectionArgs = new String[]{"image/jpeg", "image/png", "image/gif"};
            }
        }
        Cursor mCursor = mContentResolver.query(mImageUri, null, selection, selectionArgs,
                MediaStore.Images.Media.DATE_MODIFIED);
        if (mDirPaths == null) {
            mDirPaths = new HashSet<>();
        }
        while (mCursor.moveToNext()) {
            String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            if (isPickVideo) {
                videoDurationMap.put(path.substring(path.lastIndexOf("/") + 1), mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
            }
            if (firstImage == null)
                firstImage = path;
            File parentFile = new File(path).getParentFile();
            if (parentFile == null)
                continue;
            String dirPath = parentFile.getAbsolutePath();
            ImageFolder imageFolder;
            if (mDirPaths.contains(dirPath)) {
                continue;
            } else {
                mDirPaths.add(dirPath);
                imageFolder = new ImageFolder();
                imageFolder.setDir(dirPath);
                imageFolder.setFirstImagePath(path);
            }


            String[] list = parentFile.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    String fileType = filename.contains(".") ? filename.substring(filename.indexOf(".")) : "";
                    if (isPickVideo) {
                        return videoFileType.contains(fileType);
                    }
                    if (supportWebp) {
                        return fileType.equals(".webp") || fileTypeList.contains(fileType);
                    } else {
                        return fileTypeList.contains(fileType);
                    }
                }
            });
            int picSize = list == null ? 0 : list.length;
            totalCount += picSize;

            imageFolder.setCount(picSize);
            imgFolder.add(imageFolder);

            if (picSize > mPicsSize) {
                mPicsSize = picSize;
                mImgDir = parentFile;
                if (popAdapter != null) {
                    popAdapter.setExpandDir(mImgDir);
                    popAdapter.notifyDataSetChanged();
                }
            }
        }
        mCursor.close();

        mDirPaths.clear();
        mDirPaths = null;
    }

    @SuppressLint("InflateParams")
    @SuppressWarnings("deprecation")
    private void initListDirPopupWindow() {
        View popView = getInflater().inflate(R.layout.list_select_image_pop, null);
        imagePopWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (ScreenUtils.getScreenHeight(getActivity()) * 0.7));
        imagePopWindow.setBackgroundDrawable(new BitmapDrawable());
        imagePopWindow.setFocusable(true);

        ListView mListDir = popView.findViewById(R.id.id_list_dir);
        popAdapter = new PopAdapter(getActivity(), imageLoader, imgFolder, mImgDir);
        mListDir.setAdapter(popAdapter);
        imagePopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                lp.alpha = 1.0f;
                getActivity().getWindow().setAttributes(lp);
            }
        });
        mListDir.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selected(imgFolder.get(position));
            }
        });
    }

    private String[] listFile() {
        return mImgDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                String fileType = filename.contains(".") ? filename.substring(filename.lastIndexOf(".")) : "";
                if (isPickVideo) {
                    return videoFileType.contains(fileType);
                } else {
                    if (supportWebp) {
                        return filename.endsWith(".webp") || fileTypeList.contains(fileType);
                    } else {
                        return fileTypeList.contains(fileType);
                    }
                }
            }
        });
    }

    private void sortFile(String[] list) {
        mImgs = Arrays.asList(list);
        Collections.sort(mImgs, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                File flhs = new File(mImgDir.getAbsolutePath() + "/" + lhs);
                File frhs = new File(mImgDir.getAbsolutePath() + "/" + rhs);
                if (flhs.lastModified() > frhs.lastModified()) {
                    return -1;
                } else if (flhs.lastModified() == frhs.lastModified()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
    }

    public void selected(ImageFolder folder) {
        mImgDir = new File(folder.getDir());
        if (popAdapter != null) {
            popAdapter.setExpandDir(mImgDir);
            popAdapter.notifyDataSetChanged();
        }
        String[] list = listFile();
        if (list != null) {
            sortFile(list);
            mImageCount.setText(folder.getCount() + getString(isPickVideo ? R.string.img_picker_qty : R.string.piece));
            mChooseDir.setText(folder.getName());
            imageAdapter.notifyDataSetChanged();
        } else {
            imgFolder.remove(folder);
            popAdapter.notifyDataSetChanged();
            T.show(getActivity(), R.string.forder_not_exist, Toast.LENGTH_SHORT);
        }
        imagePopWindow.dismiss();
    }

    class ImageAdapter extends BaseAdapter {
        int width;

        public ImageAdapter() {
            width = ScreenUtils.getScreenWidth(getActivity()) / 3;
        }

        @Override
        public int getViewTypeCount() {
            if (openCamera) {
                return 2;
            }
            return 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (openCamera) {
                if (position == 0) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                return 1;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                if (getItemViewType(position) == 0) {
                    convertView = getInflater().inflate(R.layout.grid_item_first_select_image, parent, false);
                } else {
                    convertView = getInflater().inflate(R.layout.grid_item_select_image, parent, false);
                }

                ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                layoutParams.width = width;
                layoutParams.height = width;
                convertView.setLayoutParams(layoutParams);
            }

            if (getItemViewType(position) == 0) {
                final ImageView mImageView = ViewHolderUtils.get(convertView, R.id.id_item_image);

                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkCameraPermission();
                    }
                });
            }

            if (getItemViewType(position) == 1) {
                final ImageView mImageView = ViewHolderUtils.get(convertView, R.id.id_item_image);
                final ImageView mSelect = ViewHolderUtils.get(convertView, R.id.id_item_select);
                final TextView tvDuration = ViewHolderUtils.get(convertView, R.id.tv_duration);
                tvDuration.setVisibility(isPickVideo ? View.VISIBLE : View.GONE);
                selectNum.setText(mSelectedImage.size() + "/" + maxPictureNumber + getResources().getString(R.string.done));
                final int pos;
                if (openCamera) {
                    pos = position - 1;
                } else {
                    pos = position;
                }
                if (isPickVideo) {
                    Long duration = videoDurationMap.get(mImgs.get(pos));
                    tvDuration.setText(longToTimeFormat(duration));
                }
                imageLoader.loadImage(getActivity(),
                        "file://" + mImgDir.getAbsolutePath() + "/" + mImgs.get(pos), mImageView);

                if (mSelectedImage.contains(mImgDir.getAbsolutePath() + "/" + mImgs.get(pos))) {
                    mSelect.setImageResource(R.drawable.img_picker_ic_selected);
                    mImageView.setColorFilter(Color.parseColor("#77000000"));
                } else {
                    mSelect.setImageResource(R.drawable.img_picker_ic_unselected);
                    mImageView.setColorFilter(null);
                }
                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mSelectedImage.contains(mImgDir.getAbsolutePath() + "/" + mImgs.get(pos))) {
                            mSelectedImage.remove(mImgDir.getAbsolutePath() + "/" + mImgs.get(pos));
                            mSelect.setImageResource(R.drawable.img_picker_ic_unselected);
                            mImageView.setColorFilter(null);
                            selectNum.setText(mSelectedImage.size() + "/" + maxPictureNumber + getResources().getString(R.string.done));
                        } else {
                            if (mSelectedImage.size() == maxPictureNumber) {
                                T.show(getActivity(), getResources().getString(R.string.img_picker_most_choice) + maxPictureNumber + getString(isPickVideo ? R.string.img_picker_qty_video : R.string.piece_pic), Toast.LENGTH_SHORT);
                                return;
                            }
                            mSelectedImage.add(mImgDir.getAbsolutePath() + "/" + mImgs.get(pos));
                            mSelect.setImageResource(R.drawable.img_picker_ic_selected);
                            mImageView.setColorFilter(Color.parseColor("#77000000"));
                            selectNum.setText(mSelectedImage.size() + "/" + maxPictureNumber + getResources().getString(R.string.done));
                        }
                        if (!isUseByDialog) {
                            FragmentActivity ac = getActivity();
                            if (ac != null && ac instanceof OnImagePickerListener) {
                                ((OnImagePickerListener) ac).onImagesPicked(mSelectedImage, mImgDir.getAbsolutePath());
                            }
                        }
                    }
                });
            }
            return convertView;
        }

        @Override
        public int getCount() {
            if (openCamera) {
                if (mImgs.size() == 0) {
                    return 1;
                }
                return mImgs.size() + 1;
            }
            return mImgs.size();
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

    private String longToTimeFormat(Long duration) {
        long second = duration / 1000;
        long s = second % 60;
        long m = second / 60;
        long h = m / 60;
        if (h > 0) {
            return wrapZero(h) + ":" + wrapZero(m) + ":" + wrapZero(s);
        } else {
            return wrapZero(m) + ":" + wrapZero(s);
        }
    }

    private String wrapZero(long l) {
        if (l < 10) {
            return "0" + l;
        }
        return String.valueOf(l);
    }

    private LayoutInflater getInflater() {
        return LayoutInflater.from(getActivity());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 777);
        } else {
            startPhoto();
        }
    }

    private void startPhoto() {
        String path = Environment.getExternalStorageDirectory().toString() + PATH;
        File path1 = new File(path);
        if (!path1.exists()) {
            path1.mkdirs();
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imagePath = path + System.currentTimeMillis() + ".jpg";

        ContentValues contentValues = new ContentValues(1);
        contentValues.put(MediaStore.Images.Media.DATA, imagePath);
        Uri uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 777) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPhoto();
            } else {
                Toast.makeText(getActivity(), R.string.request_camera_permission_decline, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }

        if (requestCode == REQUEST_CAMERA && Activity.RESULT_OK == resultCode) {
            String path = Environment.getExternalStorageDirectory().toString()
                    + PATH;
            final File ab = new File(path + imagePath);
            ScanUtil.scanFile(getContext(), path, ab, imagePath);
            FragmentActivity ac = getActivity();
            if (ac != null && ac instanceof OnImagePickerListener) {
                ((OnImagePickerListener) ac).onCameraCallBack(imagePath);
            }
            if (isUseByDialog) {
                dismissAllowingStateLoss();
            }
        }
    }

    public interface OnImagePickerListener {
        void onImagesPicked(List<String> imgPaths, String selectedDir);

        void onCameraCallBack(String imgPath);
    }

    public static class Builder {
        /**
         * User for whether open camera to pick a now time picture;
         */
        private boolean openCamera = true;
        /**
         * Once we want use ImagePicker as a fragment only,set this false to hide titleBar.
         */
        private boolean supportTitleBar = true;
        /**
         * Limit of the picked picture
         */
        private int maxPictureNumber;

        private ArrayList<String> mSelectedImage = new ArrayList<String>();

        private boolean supportWebp = false;

        private boolean isPickVideo = false;

        private String mImgDirPath;

        public Builder openCamera(boolean open) {
            this.openCamera = open;
            return this;
        }

        public Builder supportWebp(boolean supportWebp) {
            this.supportWebp = supportWebp;
            return this;
        }

        public Builder isPickVideo(boolean isPickVideo) {
            this.isPickVideo = isPickVideo;
            return this;
        }

        public Builder hideTitleBar() {
            this.supportTitleBar = false;
            return this;
        }

        public Builder maxPictureNumber(int max) {
            if (max < 0) {
                max = 1;
            }
            this.maxPictureNumber = max;
            return this;
        }

        public Builder imgDirPath(String path) {
            this.mImgDirPath = path;
            return this;
        }

        public Builder selectedImages(List<String> selected) {
            this.mSelectedImage.clear();
            if (selected != null) {
                this.mSelectedImage.addAll(selected);
            }
            return this;
        }

        public ImagePicker build() {
            final ImagePicker picker = new ImagePicker();
            Bundle argus = new Bundle();
            argus.putInt("maxPictureNumber", maxPictureNumber);
            argus.putBoolean("openCamera", openCamera);
            argus.putBoolean("supportTitleBar", supportTitleBar);
            argus.putBoolean("supportWebp", supportWebp);
            argus.putBoolean("isPickVideo", isPickVideo);
            argus.putStringArrayList("mSelectedImage", mSelectedImage);
            argus.putString("mImgDirPath", mImgDirPath);
            picker.setArguments(argus);
            return picker;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("mSelectedImage", mSelectedImage);
        outState.putString("imagePath", imagePath);
        outState.putString("mImgDirPath", mImgDirPath);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        isUseByDialog = true;
        try {
            Field mDismissed = getClass().getField("mDismissed");
            mDismissed.setAccessible(true);
            mDismissed.set(this, false);
            Field mShownByMe = getClass().getField("mShownByMe");
            mShownByMe.setAccessible(true);
            mShownByMe.set(this, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }
}
