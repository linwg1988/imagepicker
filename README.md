# imagepicker

一个图片选择器ImagePicker，可以被当成dialog使用，同样的可以被当做片段Fragmnet使用.
添加依赖：

dependencies {
    compile 'org.linwg1988:imagepicker:1.0.0'
}

使用方法：

1.配置ImageLoader
    ImagePicker默认没有使用图加载框架，考虑到不同项目可能使用不同的图片加载框架，这里讲图片的加载过程分离出来，使用时只用在初始化的时候配置
这个ImageLoader就可，例如GlideImageLoader：
    public GlideImageLoader implements ImageLoader {
        public void loadImage(Context context,String imagePath,ImageView targetView){
            Glide.with(context).load(imagePath).into(targetView);
        }
    }
这只是简单的为图片选择器提供了一个ImageLoader.

2.调起ImagePicker的Activity必须实现OnImagePickerListener.
3.实例化ImagePicker并显示：
  ImagePicker.Builder builder = new ImagePicker.Builder();
  builder.openCamera(true);
  builder.maxPictureNumber(9);
  builder.build().show(getSupportFragmentManager(), "ImagePicker");

ImagePicker支持的一些配置：
a)openCamera        是否开启摄像按钮
b)supportTitleBar   是否显示标题栏，一般用于片段展示的时候，此时不再需要标题栏
c)maxPictureNumber  可选的最大图片数量
d)selectedImages    初始选中的图片地址
e)imgDirPath        初始选中的图片文件夹
f)supportWebp       是否支持webp格式的图片，默认不支持

ps：网上的图片选择器应该蛮多的，这次上传目的主要是熟练jcenter和git,如果能帮到其他人那最好不过了~~.
