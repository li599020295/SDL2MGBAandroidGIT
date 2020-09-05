package lilinhong.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.tencent.mmkv.MMKV;

import org.libsdl.app.R;
import lilinhong.utils.PreferencesData;

public class BootActivity extends AppCompatActivity {
    private PreferencesData preferencesData = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initData();
    }

    private void initData() {
        preferencesData = PreferencesData.getInstance(BootActivity.this);
        {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                    .cacheInMemory(false) //设置下载的图片是否缓存在内存中
                    .cacheOnDisk(false)//设置下载的图片是否缓存在SD卡中
                    .showImageOnLoading(R.mipmap.loading)
                    .showImageOnFail(R.mipmap.loadfail)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .considerExifParams(true)//是否考虑JPEG图像EXIF参数（旋转，翻转）
                    .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
                    .build();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(BootActivity.this)
                    .memoryCacheExtraOptions(480, 800) // max width, max height，即保存的每个缓存文件的最大长宽
                    .threadPoolSize(3) //线程池内加载的数量
                    .threadPriority(Thread.NORM_PRIORITY - 2)
                    .denyCacheImageMultipleSizesInMemory()
                    .diskCacheFileNameGenerator(new Md5FileNameGenerator()) //将保存的时候的URI名称用MD5 加密
                    .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
                    .memoryCacheSize(2 * 1024 * 1024) // 内存缓存的最大值
                    .diskCacheSize(50 * 1024 * 1024)  // 50 Mb sd卡(本地)缓存的最大值
                    .tasksProcessingOrder(QueueProcessingType.LIFO)
                    .defaultDisplayImageOptions(options)// 由原先的discCache -> diskCache
                    .diskCache(new UnlimitedDiskCache(BootActivity.this.getFilesDir()))//自定义缓存路径
                    .imageDownloader(new BaseImageDownloader(BootActivity.this, 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
                    .writeDebugLogs() // Remove for release app
                    .build();
            com.nostra13.universalimageloader.core.ImageLoader.getInstance().init(config);//全局初始化此配置
        }
        Intent intent = new Intent(BootActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
