package lilinhong.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import org.libsdl.app.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import lilinhong.dialog.TipsDialog;
import lilinhong.model.GameRom;

public class Utils {
    private static Point size = new Point();
    public static int getDisplayWidth(Display display) {
        display.getSize(size);
        return size.x;
    }

    public static int getDisplayHeight(Display display) {
        display.getSize(size);
        return size.y;
    }
    public static void goGooglePlay(final Context context) {
        String pkName = context.getPackageName();
        //这里开始执行一个应用市场跳转逻辑，默认this为Context上下文对象
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + pkName)); //跳转到应用市场，非Google Play市场一般情况也实现了这个接口
        //存在手机里没安装应用市场的情况，跳转会包异常，做一个接收判断
        if (intent.resolveActivity(context.getPackageManager()) != null) { //可以接收
            context.startActivity(intent);
        } else { //没有应用市场，我们通过浏览器跳转到Google Play
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + pkName));
            //这里存在一个极端情况就是有些用户浏览器也没有，再判断一次
            if (intent.resolveActivity(context.getPackageManager()) != null) { //有浏览器
                context.startActivity(intent);
            } else { //天哪，这还是智能手机吗？
                Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //获取通知栏高度
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    public static String encodeBase64ToString(String str){
        try {
            return Base64.encodeToString(str.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
    /**
     * 字符Base64解密
     * @param str
     * @return
     */
    public static String decodeBase64ToString(String str){
        try {
            return new String(Base64.decode(str.getBytes("UTF-8"), Base64.DEFAULT));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Bitmap captureView(View view) {
        // 根据View的宽高创建一个空的Bitmap
        Bitmap bitmap = Bitmap.createBitmap(
                view.getWidth(),
                view.getHeight(),
                Bitmap.Config.RGB_565);
        // 利用该Bitmap创建一个空的Canvas
        Canvas canvas = new Canvas(bitmap);
        // 绘制背景(可选)
        canvas.drawColor(Color.WHITE);
        // 将view的内容绘制到我们指定的Canvas上
        view.draw(canvas);
        return bitmap;
    }

    //获取所有的slot数量
    public static List<File>getAllSlotList(String gamePath){
        List<File> slotList = new ArrayList<>();
        for (int i=0;i<8;i++){
            String slotPath = gamePath.substring(0,gamePath.lastIndexOf(".")+1)+"ss"+String.valueOf(i+1);
            File file =new File(slotPath);
            if(file.exists()){
                slotList.add(file);
            }
        }
        return slotList;
    }
    //路径去掉存储卡自带的那部分
    public static String getPathDeduplication(Context context,String gamePath){
        List<String>listPath = getAllStoragePathStr(context);
        for (String path : listPath){
            if(gamePath.indexOf(path)>=0){
                return gamePath.replaceAll(path,"");
            }
        }
        return gamePath;
    }

    //获取时间自动转换为分钟或者小时或者秒
    public static String getTimeHourOrMinuteOrSecon(long times,Context context){
        if(times <= 0){
            return times + " "+context.getString(R.string.second);
        }
        long second = times/1000;
        long minute = -1;
        long hour = -1;
        if(second > 60){
            minute = second / 60;
            if(minute > 60){
                hour = minute / 60;
            }
        }

        if(hour > 0){
            return hour + " "+context.getString(R.string.hour);
        }

        if (minute > 0){
            return minute + " "+context.getString(R.string.minute);
        }

        return second + " "+context.getString(R.string.second);
    }

    //获取所有存储的位置
    public static List<String>getAllStoragePathStr(Context context){
        File[] files;
        List<String> tempList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            files = context.getExternalFilesDirs(Environment.MEDIA_MOUNTED);
            try{
                for(File file:files){
                    if(file!=null){
                        Log.e("main:",file.getAbsolutePath());
                        tempList.add(file.getAbsolutePath());
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //低于4.1版本
        tempList.add(context.getExternalFilesDir(null).getAbsolutePath());
        //内存存储
        tempList.add(context.getFilesDir().getAbsolutePath());
        //外部存储
        tempList.add(Environment.getExternalStorageDirectory().getAbsolutePath());
        return tempList;
    }
    //获取所有存储路径
    public static List<File>getAllStoragePath(Context context){

        List<String> tempList = getAllStoragePathStr(context);
        //去重
        LinkedHashSet<String> hashSet = new LinkedHashSet<>(tempList);
        tempList = new ArrayList<>(hashSet);

        System.out.println(tempList.toString());

        List<File> pathList = new ArrayList<>();
        for (String mount : tempList) {
            if(mount !=null){
                File root = new File(mount);
                if (root.exists() && root.isDirectory() && root.canRead()) {
                    pathList.add(root);
                }
            }
        }
        return pathList;
    }
    //转换文件大小为M
    public static String getFileSizeForM(long fileSize){
        DecimalFormat df = new DecimalFormat( "0.## ");
        Float si=fileSize/1024f/1024f;
       // System.out.println(df.format(si)+"M");
        return df.format(si)+"M";
    }

    //设置颜色兼容
    public static final int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    //日月年美国获取时间方法
    public static String getFileLastModifiedTime(File file) {
        long time = file.lastModified();
        return getUSTime(time);
    }

    //获取美国时间
    public static String getUSTime(long ctime){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ctime);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH)+1;
        int year = cal.get(Calendar.YEAR);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        // 输出：修改时间[2] 2009-08-17 10:32:38///09 day 11 month in 2020
        return day +" day "+ month + " month in "+year+", "+hour+":"+minute+":"+second;
    }

    /**
     * 加载本地图片
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        if(url == null || url.equals("")){
            return null;
        }
        Bitmap bitmap = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(url);
            bitmap =  BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try{
                if(fis!=null){
                    fis.close();
                }
            }catch (Exception e){}
        }
        return bitmap;
    }
    public static String getSlotPath(String gamePath,int slot){
        if(gamePath == null &&gamePath.equals("")){
            return null;
        }
        String slotPath = gamePath.substring(0,gamePath.lastIndexOf(".")+1)+"ss"+String.valueOf(slot+1);
        return slotPath;
    }

    //添加文件列表
    public static void addGameFileList(List<File> fileList) {
        if(fileList == null || fileList.size() == 0){
            return;
        }
        PreferencesData preferencesData = PreferencesData.getInstance();
        Map<String,GameRom> tempGameRomMap = preferencesData.getMapRoms();
        Map<String,GameRom> gameRomMap = new HashMap<>();
        for (File file : fileList) {
            String md5 = getFileMD5(file);
            if (md5 == null) {
                continue;
            }
            boolean isHaveData = tempGameRomMap.containsKey(md5);
            GameRom gameRom = null;
            if(isHaveData){
                gameRom = tempGameRomMap.get(md5);
            }else{
                gameRom = new GameRom();
                gameRom.setName(file.getName());
                gameRom.setMd5(md5);
                gameRom.setPath(file.getAbsolutePath());
                gameRom.setLastPlayTime(0);
                gameRom.setDesc("");
                gameRom.setImage("");
            }
            gameRomMap.put(md5,gameRom);
        }

        if(gameRomMap.size() > 0){
            preferencesData.addGameAllRomList(gameRomMap);
        }
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytesToHexString(digest.digest());
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    //遍历游戏文件
    public static void getGameFiles(String path, List<File> fileList) {
        File file = new File(path);
        //判断是否是游戏文件
        if(file.isFile()){
            boolean isGameFile = isGameFile(file);
            if(isGameFile) {
                fileList.add(file);
            }
            return;
        }
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        //过滤.开头文件夹
        if(!GlobalConfig.SEARCH_DOT){
            String dirName = file.getName();
            if(dirName.indexOf(".") == 0){
                return;
            }
        }
        for (File f : files) {
            if (f.isDirectory()) {
                getGameFiles(f.getAbsolutePath(),fileList);//自己调用自己
            } else {
               boolean isGameFile = isGameFile(f);
               if(isGameFile){
                   fileList.add(f);
               }
            }
        }
    }

    public static boolean checkSUFFIX(String suffix){
        if(suffix.equals(GlobalConfig.GBA_SUFFIX) || suffix.equals(GlobalConfig.GB_SUFFIX) || suffix.equals(GlobalConfig.SGB_SUFFIX)
                || suffix.equals(GlobalConfig.GBC_SUFFIX)){
            return true;
        }
        return false;
    }

    public static String getSuffix(String name){
        String suffix = name.substring(name.lastIndexOf(".")+1).toLowerCase();
        return suffix;
    }

    //检测zip文件是否合格
    private static boolean accept(String filename) {
        if (filename.charAt(0) == '.')
            return false;

        String suffix = getSuffix(filename);
        if(checkSUFFIX(suffix)){
            return true;
        }
        return false;
    }

    //检测zip文件
    public static boolean checkZIPFile(File file){
        int counterRoms = 0;
        int counterEntry = 0;
        try{
            ZipFile zip = new ZipFile(file);
            zip.setFileNameCharset("GBK");
            //文件不合法
            if(!zip.isValidZipFile()){
                Log.e("ZIP_ERROR_FILE:",file.getAbsolutePath());
                return false;
            }

            if(zip.isEncrypted()){
                //额，无法输入密码
                //zip.setPassword(Utils.getZIPPassworld());
                return false;
            }

            List listFileHeaders  = zip.getFileHeaders();
            for (int i=0;i<listFileHeaders.size();i++) {
                FileHeader fileHeader = (FileHeader)listFileHeaders.get(i);
                counterEntry++;
                if ((!fileHeader.isDirectory())) {
                    String filename = fileHeader.getFileName();
                    if (accept(filename)) {
                        counterRoms++;
                        return true;
                    }
                }

                if (counterEntry > 30 && counterRoms == 0) {
                    //超过找的深度了
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isGameFile(File file){
        String name = file.getName();
        //获取文件后缀名字
        String suffix = getSuffix(name);
        boolean check = checkSUFFIX(suffix);
        if(check){
            return true;
        }

        //zip文件
        if(suffix.equals(GlobalConfig.ZIP_SUFFIX)){
            check = checkZIPFile(file);
            if(check){
                return true;
            }
        }
        return false;
    }

    public static TipsDialog showTips(Context context, String title, String desc){
        TipsDialog tipsDialog = new TipsDialog(context);
        tipsDialog.setData(title,desc);
        //tipsDialog.show();
        return tipsDialog;
    }

    //复制assets的数据到某个地方
    public static void copyAssetsData(Context context, String path){
        InputStream inStream = null;
        FileOutputStream outStream = null;
        try{
            String gameData = "gjzz2.gba";

            inStream = context.getResources().getAssets().open(gameData);

            System.out.println(path);

            File libcocos2dFile = new File(path+"/"+gameData);
            //如果存在先删除
            if(!libcocos2dFile.exists()){
                outStream = new FileOutputStream(libcocos2dFile);
                int len=0;
                byte[] buffer = new byte[1024*4];
                while((len=inStream.read(buffer))!=-1){
                    outStream.write(buffer, 0, len);//
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(inStream!=null){
                    inStream.close();
                }
                if(outStream!=null){
                    outStream.close();
                }
            }catch (Exception e){}
        }
    }
}
