package lilinhong.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import org.libsdl.app.R;
import org.libsdl.app.SDLActivity;
import java.io.File;
import java.util.List;

import lilinhong.activity.MainActivity;
import lilinhong.dialog.GameInfoDialog;
import lilinhong.dialog.SearchFileDialog;
import lilinhong.dialog.TipsDialog;
import lilinhong.model.GameRom;
import lilinhong.utils.PermissionSystem;
import lilinhong.utils.PreferencesData;
import lilinhong.utils.Utils;

public class RomsFragment extends Fragment {
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                List<GameRom>romList = (List<GameRom>)msg.obj;
                gameRomsAdapter.setRomsDataList(romList);
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.roms_fragment, container, false);
        initData();
        initUI();
        initFinish();
        Log.i(TAG,"onCreateView");
        return mainView;
    }

    public void reFreshData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<GameRom> gameRomList = preferencesData.getRoms();
                Message msg = handler.obtainMessage();
                msg.what = 1;
                msg.obj = gameRomList;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private void initData() {
        permissionSystem = new PermissionSystem(getActivity());
        preferencesData = PreferencesData.getInstance(getActivity());
        gameARomList = preferencesData.getRoms();
        gameRomsAdapter = new RomsFragment.GameRomsAdapter(getActivity(),gameARomList);
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
                    gameRomsAdapter.setRomsDataList(preferencesData.getRoms());
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

    class GameRomsAdapter extends BaseAdapter {
        private Context context;
        private List<GameRom> gameRomList = null;
        public void setRomsDataList(List<GameRom> gameRomList){
            this.gameRomList = gameRomList;
        }
        public GameRomsAdapter(Context context, List<GameRom> gameRomList){
            this.context = context;
            this.gameRomList = gameRomList;
        }
        @Override
        public int getCount() {
            return this.gameRomList.size();
        }

        @Override
        public Object getItem(int position) {
            return gameRomList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            RomsFragment.GameRomsAdapter.HoldView holdView = null;
            GameRom rom = gameRomList.get(position);
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
                        GameRom gameRom1 = (GameRom)buttonView.getTag();
                        if(isChecked){
                            buttonView.setBackgroundResource(R.mipmap.collect_btn);
                        }else {
                            buttonView.setBackgroundResource(R.mipmap.un_collect_btn);
                        }
                        gameRom1 = gameRomList.get(position).setCollect(isChecked);
                        preferencesData.setCollectRom(gameRom1);
                        notifyDataSetChanged();
                        MainActivity.getMainActivity().setCollectRefresh();
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
                    ImageLoader.getInstance().displayImage(image, holdView.roms_fragment_item_image);
                }else{
                    //drawable://
                    ImageLoader.getInstance().displayImage("drawable://" + R.mipmap.ic_launcher, holdView.roms_fragment_item_image);
                }
            }else{
                ImageLoader.getInstance().displayImage("drawable://" + R.mipmap.ic_launcher, holdView.roms_fragment_item_image);
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
