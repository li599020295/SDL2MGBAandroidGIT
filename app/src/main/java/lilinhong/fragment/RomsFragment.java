package lilinhong.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tencent.mmkv.MMKV;

import org.libsdl.app.R;
import org.libsdl.app.SDLActivity;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lilinhong.activity.MainActivity;
import lilinhong.dialog.GameInfoDialog;
import lilinhong.dialog.SearchFileDialog;
import lilinhong.dialog.TipsDialog;
import lilinhong.model.GameRom;
import lilinhong.model.IconData;
import lilinhong.utils.PermissionSystem;
import lilinhong.utils.PreferencesData;
import lilinhong.utils.Utils;

public class RomsFragment extends Fragment {
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                gameRomsAdapter.notifyDataSetChanged();
            }
        }
    };
    private String TAG = RomsFragment.class.getName();
    private RomsFragment.GameRomsAdapter gameRomsAdapter = null;
    private ListView game_roms_listview = null;
    private List<GameRom> gameARomList = null;
    private PreferencesData preferencesData = null;
    private PermissionSystem permissionSystem = null;
    //存储权限返回标志
    private static final int REQUEST_EXTERNAL_STORAGE = 3;
    private View mainView = null;
    //是否刷新
    private boolean isReFresh = false;
    //保存icon数据防止重复加载
    private Map<Integer, IconData> iconMap = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.roms_fragment, container, false);
        initData();
        initUI();
        initFinish();
        return mainView;
    }

    public void onResume(){
        super.onResume();

        if(preferencesData == null){
            preferencesData = PreferencesData.getInstance(getActivity());
        }

        if(preferencesData!=null && gameARomList!=null){
            gameARomList = preferencesData.getRoms();
        }
        if(gameRomsAdapter!=null){
            gameRomsAdapter.notifyDataSetChanged();
        }
    }

    public void reFreshData(){
        gameARomList = preferencesData.getRoms();
        Message msg = handler.obtainMessage();
        msg.what = 1;
        handler.sendMessage(msg);
    }

    public void setReFresh(boolean isReFresh){
        this.isReFresh = isReFresh;
    }

    private void initData() {
        iconMap = new HashMap<>();
        permissionSystem = new PermissionSystem(getActivity());
        preferencesData = PreferencesData.getInstance();
        gameARomList = preferencesData.getRoms();
        gameRomsAdapter = new RomsFragment.GameRomsAdapter(getActivity());
    }

    private void initUI(){
        game_roms_listview = (ListView)mainView.findViewById(R.id.game_roms_listview);
        game_roms_listview.setAdapter(gameRomsAdapter);
        game_roms_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GameRom gameRom = (GameRom)gameRomsAdapter.getItem(position);
                Intent sdlActivityIntent = new Intent(getActivity(), SDLActivity.class);
                Bundle bundle=new Bundle();
                bundle.putSerializable("GAME_ROM",gameRom);
                sdlActivityIntent.putExtras(bundle);
                startActivity(sdlActivityIntent);
            }
        });
    }
    private void initFinish(){
        if( gameARomList.size() == 0 && !preferencesData.getFirstScance()){
            scanGameFileTipsDialog();
        }
    }
    private void applyFilePermissionsTipsDialog(){
        final TipsDialog permissTips = Utils.showTips(getActivity(), getString(R.string.file_read_write_permiss), getString(R.string.file_read_write_permiss_desc));
        permissTips.setCancelable(false);
        permissTips.setOnButton1Listener(getString(R.string.agree), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissTips.dismiss();
                if(!permissionSystem.checkStoragePermissions()){
                    permissionSystem.verifyStoragePermissions(REQUEST_EXTERNAL_STORAGE);
                }else{
                    fileScanDialog();
                }
            }
        }).setOnButton2Listener(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissTips.dismiss();
            }
        }).show();
    }

    private void scanGameFileTipsDialog(){
        final TipsDialog scanFileTips = Utils.showTips(getActivity(), getString(R.string.first_game_file_scanle), getString(R.string.first_game_file_sacnle_desc));
        scanFileTips.setCancelable(false);
        scanFileTips.setOnButton1Listener(getString(R.string.ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFileTips.dismiss();
                if(permissionSystem.checkStoragePermissions()){
                    fileScanDialog();
                }else{
                    applyFilePermissionsTipsDialog();
                }
            }
        }).setOnButton2Listener(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFileTips.dismiss();
            }
        }).show();
    }

    public void fileScanDialog(){
        if(permissionSystem.checkStoragePermissions()){
            SearchFileDialog searchFileDialog = new SearchFileDialog(getActivity());
            searchFileDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    gameARomList = preferencesData.getRoms();
                    gameRomsAdapter.notifyDataSetChanged();
                }
            });
            searchFileDialog.show();
        }
    }
    //权限检测
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            boolean isHaveFilePermissions = false;
            for (int i = 0; i < permissions.length; i++) {
                if(permissions[i].equals("android.permission.WRITE_EXTERNAL_STORAGE")  && grantResults[i] >= 0){
                    isHaveFilePermissions = true;
                }
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
            if(isHaveFilePermissions){
                fileScanDialog();
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isReFresh){
            reFreshData();
            isReFresh = false;
        }
    }

    class GameRomsAdapter extends BaseAdapter {
        private Context context;

        public GameRomsAdapter(Context context){
            this.context = context;
        }
        @Override
        public int getCount() {
            return gameARomList.size();
        }

        @Override
        public Object getItem(int position) {
            return gameARomList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            RomsFragment.GameRomsAdapter.HoldView holdView = null;
            GameRom rom = gameARomList.get(position);
            if(convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.roms_fragment_listview_item,null);
                holdView = new RomsFragment.GameRomsAdapter.HoldView();
                holdView.roms_fragment_item_image = (ImageView)convertView.findViewById(R.id.roms_fragment_item_image);
                holdView.roms_fragment_item_name = (TextView) convertView.findViewById(R.id.roms_fragment_item_name);
                holdView.roms_fragment_item_desc = (TextView)convertView.findViewById(R.id.roms_fragment_item_desc);
                holdView.roms_fragment_info_imagebtn = (ImageButton)convertView.findViewById(R.id.roms_fragment_info_imagebtn);
                holdView.roms_fragment_togglebtn_colle = (ToggleButton)convertView.findViewById(R.id.roms_fragment_togglebtn_colle);
                holdView.roms_fragment_info_imagebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GameRom gameRom = (GameRom)v.getTag();
                        final GameInfoDialog gameInfoDialog = new GameInfoDialog(getActivity(),gameRom);
                        gameInfoDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if(gameInfoDialog.getIsRefresh()){
                                    GameRom rom = gameInfoDialog.getGameRom();
                                    //移除一个游戏xxxx
                                    preferencesData.removeRomGame(rom);
                                    reFreshData();

                                }
                            }
                        });
                        gameInfoDialog.show();
                    }
                });

                holdView.roms_fragment_togglebtn_colle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            buttonView.setBackgroundResource(R.mipmap.collect_btn);
                        }else {
                            buttonView.setBackgroundResource(R.mipmap.un_collect_btn);
                        }

                        GameRom gameRom1 = (GameRom)buttonView.getTag();
                        if(gameRom1.isCollect() == isChecked){
                            return;
                        }
                        gameRom1.setCollect(isChecked);

                        {
                            GameRom gr = gameARomList.get(position);
                            gr.setCollect(isChecked);
                        }

                        preferencesData.setCollectRom(gameRom1);
                        notifyDataSetChanged();
                        MainActivity.getMainActivity().setFragmentRefresh();
                    }
                });
                convertView.setTag(holdView);
            }else{
                holdView = (RomsFragment.GameRomsAdapter.HoldView)convertView.getTag();
            }
            //设置游戏Romd的位置
            holdView.roms_fragment_info_imagebtn.setTag(rom);
            holdView.roms_fragment_togglebtn_colle.setTag(rom);
            String image =  rom.getImage();
            String name = rom.getName();
            String desc = rom.getDesc();

            if(!image.equals("")){
                File file = new File(image);
                if(file.exists()) {
                    boolean isHaveIcon = iconMap.containsKey(position);
                    Bitmap bitmap = null;
                    if(isHaveIcon){
                        IconData iconData = iconMap.get(position);
                        if(iconData.getFilePath().equals(image)){
                            bitmap = iconData.getBitmap();
                        }else{
                            if(bitmap!=null) {
                                bitmap.recycle();
                            }
                            bitmap = Utils.getLoacalBitmap(image);
                            iconMap.put(position,new IconData(bitmap,image,position));
                        }
                    }else{
                        bitmap = Utils.getLoacalBitmap(image);
                        iconMap.put(position,new IconData(bitmap,image,position));
                    }
                    holdView.roms_fragment_item_image.setImageBitmap(bitmap);
                }else{
                    //drawable://
                    ImageLoader.getInstance().displayImage("drawable://" + R.mipmap.gba_item_icon, holdView.roms_fragment_item_image);
                }
            }else{
                ImageLoader.getInstance().displayImage("drawable://" + R.mipmap.gba_item_icon, holdView.roms_fragment_item_image);
            }

            holdView.roms_fragment_item_name.setText(name);

            if(desc.equals("")){
                holdView.roms_fragment_item_desc.setVisibility(View.GONE);
            }else{
                holdView.roms_fragment_item_desc.setVisibility(View.VISIBLE);
                holdView.roms_fragment_item_desc.setText(desc);
            }

            boolean isCollection = rom.isCollect();
            holdView.roms_fragment_togglebtn_colle.setChecked(isCollection);

            return convertView;
        }
        class HoldView{
            ImageButton roms_fragment_info_imagebtn = null;
            ImageView roms_fragment_item_image= null;
            TextView roms_fragment_item_name= null;
            TextView roms_fragment_item_desc= null;
            ToggleButton roms_fragment_togglebtn_colle = null;
        }
    }
}
