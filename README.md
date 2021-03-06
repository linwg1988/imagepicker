# imagepicker

一个图片选择器ImagePicker，可以被当成dialog使用，同样的可以被当做片段Fragmnet使用.
添加依赖：
~~~Java
dependencies {
    implementation 'org.linwg1988:imagepicker:1.0.7'
}
~~~
从1.0.1版本开始在ImagePicker作为Fragment使用时每次点击图片都会回调一次OnImagePickerListener.
## 1.0.6 版本更新说明：androidx编译，支持选择视频（不支持点击播放）
## 1.0.7 版本更新说明：修复文件扩展名不存在时的崩溃错误
使用方法：

#### 1.配置ImageLoader
~~~Java
//ImagePicker默认没有使用图加载框架，考虑到不同项目可能使用不同的图片加载框架，这里讲图片的加载过程分离出来，
//使用时只用在初始化的时候配置这个ImageLoader就可
ImagePicker.setImageLoader(new GlideImageLoader());
    
//例如本例中的GlideImageLoader：
public GlideImageLoader implements ImageLoader {
    public void loadImage(Context context,String imagePath,ImageView targetView){
        Glide.with(context).load(imagePath).into(targetView);
    }
}
~~~
这只是简单的为图片选择器提供了一个ImageLoader.

#### 2.调起ImagePicker的Activity必须实现OnImagePickerListener.
#### 3.实例化ImagePicker并显示：
~~~Java
  ImagePicker.Builder builder = new ImagePicker.Builder();
  builder.openCamera(true);
  builder.maxPictureNumber(9);
  builder.build().show(getSupportFragmentManager(), "ImagePicker");
~~~
#### ImagePicker支持的一些配置：</br>
##### a)openCamera        是否开启摄像按钮</br>
##### b)supportTitleBar   是否显示标题栏，一般用于片段展示的时候，此时不再需要标题栏</br>
##### c)maxPictureNumber  可选的最大图片数量</br>
##### d)selectedImages    初始选中的图片地址</br>
##### e)imgDirPath        初始选中的图片文件夹</br>
##### f)supportWebp       是否支持webp格式的图片，默认不支持</br>
##### g)isPickVideo       是否选择视频模式，默认否</br>

