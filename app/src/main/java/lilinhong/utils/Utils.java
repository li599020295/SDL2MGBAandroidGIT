package lilinhong.utils;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import lilinhong.dialog.TipsDialog;
import lilinhong.model.GameRom;

public class Utils {
    public static void addGameFileList(List<File> fileList) {
        if(fileList == null || fileList.size() == 0){
            return;
        }

        List<GameRom> gameRomList = new ArrayList<>();
        for (File file : fileList) {
            Log.e("xxxxaddFile",file.getAbsolutePath());
            String md5 = getFileMD5(file);
            if (md5 == null) {
                continue;
            }
            Log.e("addFile",file.getAbsolutePath());
            GameRom gameRom = new GameRom();
            gameRom.setName(file.getName());
            gameRom.setMd5(md5);
            gameRom.setPath(file.getAbsolutePath());
            gameRom.setPreUseTime(0);
            gameRom.setDesc("");
            gameRom.setImage("");
            gameRomList.add(gameRom);
        }

        if(gameRomList.size() > 0){
            PreferencesData preferencesData = PreferencesData.getInstance();
            preferencesData.addGameSearchAllRomList(gameRomList);
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
    public static boolean isGameFile(File file){
        String name = file.getName();
        String suffix = name.substring(name.lastIndexOf(".")+1).toLowerCase();
        if(suffix.equals(GlobalConfig.GBA_SUFFIX) || suffix.equals(GlobalConfig.GB_SUFFIX)){
            return true;
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
