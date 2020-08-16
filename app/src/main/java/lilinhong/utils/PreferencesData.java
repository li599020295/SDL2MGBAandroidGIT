package lilinhong.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    //获取map数据
    public static Map<String,GameRom> getMapRoms(){
        String roms = preferencesData.sp.getString("roms","");
        Gson gson1=new Gson();
        Map<String,GameRom> map= gson1.fromJson(roms, new TypeToken<Map<String,GameRom>>() {}.getType());
        if(map == null){
            map = new HashMap<>();
        }
        return map;
    }

    //移除一个游戏
    public void removeRomGame(GameRom rom){
        Map<String,GameRom> gameRomMap = getMapRoms();
        gameRomMap.remove(rom.getMd5());
        addGameAllRomList(gameRomMap);
    }

    //获取list数据
    public List<GameRom> getRoms(){
        Map<String,GameRom> gameRomMap = getMapRoms();
        List<GameRom> gameRomList = new ArrayList<>(gameRomMap.values()) ;
        return gameRomList;
    }

    //保存所有数据
    public void setSaveRomData(GameRom rom){
        Map<String,GameRom> gameRomMap = getMapRoms();
        GameRom gameRom1 =  gameRomMap.get(rom.getMd5());
        gameRom1.setPlayTime(rom.getPlayTime());
        gameRom1.setLastPlayTime(rom.getLastPlayTime());
        addGameAllRomList(gameRomMap);
    }

    //添加一个收藏
    public void setCollectRom(GameRom rom){
        Map<String,GameRom> gameRomMap = getMapRoms();
        GameRom gameRom1 =  gameRomMap.get(rom.getMd5());
        gameRom1.setCollect(rom.isCollect());
        addGameAllRomList(gameRomMap);
    }

    //获取收藏数据
    public List<GameRom>  getCollectRoms(){
        List<GameRom> romList = getRoms();
        for (int i=romList.size()-1;i>=0;i--){
            GameRom gameRom = romList.get(i);
            if(!gameRom.isCollect()){
                romList.remove(i);
            }
        }
        return romList;
    }

    //添加多个文件
    public void addGameAllRomList(Map<String,GameRom> mapRoms){
        Gson gson2=new Gson();
        String str=gson2.toJson(mapRoms);
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