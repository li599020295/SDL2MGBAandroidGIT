package lilinhong.activity;

import android.app.Application;
import android.content.Context;

import com.tencent.mmkv.MMKV;

public class MMKVApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        String rootDir = MMKV.initialize(this);
        System.out.println("mmkv root: " + rootDir);
        mContext = getApplicationContext();
    }

    public static Context getInstance() {
        return mContext;
    }
}
