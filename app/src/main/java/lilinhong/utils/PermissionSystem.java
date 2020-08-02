package lilinhong.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

//权限系统处理权限问题
public class PermissionSystem {
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    private Activity activity = null;
    public PermissionSystem(Activity activity){
        this.activity = activity;
    }
    //检测权限
    public  boolean checkStoragePermissions(){
        boolean isHaveFilePermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                //检测是否有写的权限
                int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    isHaveFilePermission = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return  isHaveFilePermission;
    }
    //然后通过一个函数来申请
    public boolean verifyStoragePermissions(int REQUEST_EXTERNAL_STORAGE) {
        boolean isHaveFilePermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                //检测是否有写的权限
                int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    // 没有写的权限，去申请写的权限，会弹出对话框
                    ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
                    isHaveFilePermission = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return  isHaveFilePermission;
    }
}
