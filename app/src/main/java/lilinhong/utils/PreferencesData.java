package lilinhong.utils;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.mmkv.MMKV;
import org.libsdl.app.GamePadRelativeLayout;
import org.libsdl.app.R;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lilinhong.model.CheatData;
import lilinhong.model.GamePad;
import lilinhong.model.GameRom;

public class PreferencesData {
    private static PreferencesData preferencesData = null;
    private static MMKV kv = null;

    public PreferencesData(Context context){
        MMKV.initialize(context);
        kv = MMKV.defaultMMKV();
    }
    public static PreferencesData getInstance(Context context) {
        //if (preferencesData == null) {
            preferencesData = new PreferencesData(context);
        //}
        return preferencesData;
    }

    public static PreferencesData getInstance(){
        return preferencesData;
    }

    //是否第一次扫描
    public boolean getFirstScance(){
        return kv.getBoolean("first_scance",false);
    }
    //
    public void setFirstScance(boolean value){
        kv.putBoolean("first_scance",value);
    }

    //获取map数据
    public Map<String,GameRom> getMapRoms(){
        String roms = preferencesData.kv.getString("roms","");
        Log.e("getMapRoms",roms);
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
        if(!gameRomMap.containsKey(rom.getMd5())){
            return;
        }
        GameRom gameRom1 =  gameRomMap.get(rom.getMd5());
        gameRom1.setPlayTime(rom.getPlayTime());
        gameRom1.setLastPlayTime(rom.getLastPlayTime());
        gameRom1.setImage(rom.getImage());
        addGameAllRomList(gameRomMap);
    }

    //添加一个收藏
    public void setCollectRom(GameRom rom){
        Map<String,GameRom> gameRomMap = getMapRoms();
        if(!gameRomMap.containsKey(rom.getMd5())){
            return;
        }
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
        kv.putString("roms",str);
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
        kv.putString("roms",str);
    }

    public List<GamePad> getGamePadList1(Context context){
        String gamePadStr = kv.getString("game_pads1","");
        if(gamePadStr!=null && !gamePadStr.equals("")){
            gamePadStr = Utils.decodeBase64ToString(gamePadStr);
        }
        Gson gson1=new Gson();
        List<GamePad> gamePads = gson1.fromJson(gamePadStr, new TypeToken<List<GamePad>>() {}.getType());
        if(gamePads == null){
            gamePads = new ArrayList<>();
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_UP,-1,context.getString(R.string.key_up)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_DOWN,-1,context.getString(R.string.key_down)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_LEFT,-1,context.getString(R.string.key_left)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_RIGHT,-1,context.getString(R.string.key_right)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_START,-1,context.getString(R.string.key_start)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_SELECT,-1,context.getString(R.string.key_select)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_L,-1,context.getString(R.string.key_l)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_S,-1,context.getString(R.string.key_s)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_A,-1,context.getString(R.string.key_a)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_B,-1,context.getString(R.string.key_b)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_R,-1,context.getString(R.string.key_r)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_V,-1,context.getString(R.string.key_v)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_SPEED,-1,context.getString(R.string.key_speed)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_REWIND,-1,context.getString(R.string.key_rewind)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_CAPTURE,-1,context.getString(R.string.key_capture)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_SAVE_SLOT1,-1,context.getString(R.string.key_saveslot1)));
            gamePads.add(new GamePad(GamePadRelativeLayout.PAD1_LOAD_SLOT1,-1,context.getString(R.string.key_loadslot1)));
            saveGamePadList1(gamePads);
        }
        return gamePads;
    }
    public void saveGamePadList1(List<GamePad> gamePads){
        Gson gson2=new Gson();
        String str=gson2.toJson(gamePads);
        str = Utils.encodeBase64ToString(str);
        kv.putString("game_pads1",str);
    }

    private void saveCheatList(List<CheatData> cheatList,String name){
        Gson gson2=new Gson();
        String str=gson2.toJson(cheatList);
        kv.putString("cheat_"+name,str);
    }
    public List<CheatData> getCheatList(String name){
        String cheatDataStr = kv.getString("cheat_"+name,"");
        Gson gson1=new Gson();
        List<CheatData> cheatDataList = gson1.fromJson(cheatDataStr, new TypeToken<List<CheatData>>() {}.getType());
        if(cheatDataList == null){
            cheatDataList = new ArrayList<>();
        }
        return cheatDataList;
    }
    public void setCheatList(List<CheatData> cheatList,String name){
        saveCheatList(cheatList,name);
    }

    public void setVirtualButtonControl(boolean isShow){
        kv.putBoolean("virtual_button_control",isShow);
    }
    //虚拟按钮控制
    public boolean getVirtualButtonControl(){
        return kv.getBoolean("virtual_button_control",true);
    }
}