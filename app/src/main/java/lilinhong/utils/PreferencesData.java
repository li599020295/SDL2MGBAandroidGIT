package lilinhong.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lilinhong.model.GameRom;

import static android.content.Context.MODE_PRIVATE;

public class PreferencesData {
    private static PreferencesData preferencesData = null;
    private Context context;
    private SharedPreferences sp;

    public static PreferencesData getInstance(Context context) {
        if (preferencesData == null) {
            preferencesData = new PreferencesData(context);
        }
        return preferencesData;
    }

    public static PreferencesData getInstance() {
        return preferencesData;
    }

    public PreferencesData(Context context) {
        this.context = context;
        sp = context.getSharedPreferences("datas", MODE_PRIVATE);
    }

    //是否第一次扫描
    public boolean getFirstScance(){
        return sp.getBoolean("first_scance",false);
    }
    //
    public void setFirstScance(boolean value){
        sp.edit().putBoolean("first_scance",value).commit();
    }
    //获取数据
    public static List<GameRom> getRoms(){
        String roms = preferencesData.sp.getString("roms","");
        Gson gson1=new Gson();
        List<GameRom> list= gson1.fromJson(roms, new TypeToken<List<GameRom>>() {}.getType());
        if(list == null){
            list = new ArrayList<>();
        }
        return list;
    }
    //添加多个文件
    public void addGameSearchAllRomList(List<GameRom> romList){
        Gson gson2=new Gson();
        String str=gson2.toJson(romList);
        sp.edit().putString("roms",str).commit();
        System.out.println(str);
    }

    public List<GameRom> checkFileExits(List<GameRom> gameRomList){
        for (int i=gameRomList.size()-1;i>=0;i--){
            File file = new File(gameRomList.get(i).getPath());
            if(!file.exists()){
                gameRomList.remove(i);
            }
        }
        return gameRomList;
    }
    //添加一个文件
    public void addGameRomList(List<GameRom> romList){
        List<GameRom> gameRomList = getRoms();
        if(gameRomList.size() == 0){
            gameRomList = romList;
        }else{
            gameRomList = checkFileExits(gameRomList);
            for (GameRom gameRom: romList) {
                for (GameRom gameAllRom : gameRomList) {
                    if (gameAllRom.getMd5().equals(gameRom.getMd5())) {
                        continue;
                    } else {
                        gameRomList.add(gameRom);
                    }
                }
            }
        }
        Gson gson2=new Gson();
        String str=gson2.toJson(gameRomList);
        sp.edit().putString("roms",str).commit();
    }
}