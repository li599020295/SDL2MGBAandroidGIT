package lilinhong.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.libsdl.app.R;
import org.libsdl.app.SDLActivity;
import java.io.File;
import java.util.List;

import lilinhong.dialog.SearchFileDialog;
import lilinhong.dialog.TipsDialog;
import lilinhong.model.GameRom;
import lilinhong.utils.PermissionSystem;
import lilinhong.utils.PreferencesData;
import lilinhong.utils.Utils;

public class GameRomsActivity extends AppCompatActivity {
    private GameRomsAdapter gameRomsAdapter = null;
    private ListView game_roms_listview = null;
    private List<GameRom> gameARomList = null;
    private PreferencesData preferencesData = null;
    private PermissionSystem permissionSystem = null;
    //存储权限返回标志
    private static final int REQUEST_EXTERNAL_STORAGE = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_roms_layout);
        initData();
        initUI();
        initFinish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_roms_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.game_roms_add_game:
                fileScanDialog();
                return true;
            case R.id.game_roms_set:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initData() {
        permissionSystem = new PermissionSystem(this);
        preferencesData = PreferencesData.getInstance(GameRomsActivity.this);
        gameARomList = PreferencesData.getRoms();
        gameRomsAdapter = new GameRomsAdapter(GameRomsActivity.this,gameARomList);

    }
    private void initUI(){
        game_roms_listview = (ListView)findViewById(R.id.game_roms_listview);
        game_roms_listview.setAdapter(gameRomsAdapter);
        game_roms_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GameRom gameRom = (GameRom)gameRomsAdapter.getItem(position);
                Intent sdlActivityIntent = new Intent(GameRomsActivity.this, SDLActivity.class);
                sdlActivityIntent.putExtra("path",gameRom.getPath());
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
        final TipsDialog permissTips = Utils.showTips(GameRomsActivity.this, getString(R.string.file_read_write_permiss), getString(R.string.file_read_write_permiss_desc));
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
        final TipsDialog scanFileTips = Utils.showTips(GameRomsActivity.this, getString(R.string.first_game_file_scanle), getString(R.string.first_game_file_sacnle_desc));
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

    private void fileScanDialog(){
        if(permissionSystem.checkStoragePermissions()){
            SearchFileDialog searchFileDialog = new SearchFileDialog(GameRomsActivity.this);
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
        public View getView(int position, View convertView, ViewGroup parent) {
            HoldView holdView = null;
            GameRom gameRom = gameRomList.get(position);
            if(convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.game_roms_listview_item,null);
                holdView = new HoldView();
                holdView.game_roms_item_image = (ImageView)convertView.findViewById(R.id.game_roms_item_image);
                holdView.game_roms_item_name = (TextView) convertView.findViewById(R.id.game_roms_item_name);
                holdView.game_roms_item_desc = (TextView)convertView.findViewById(R.id.game_roms_item_desc);
                convertView.setTag(holdView);
            }else{
                holdView = (HoldView)convertView.getTag();
            }
            String image =  gameRom.getImage();
            String name = gameRom.getName();
            String desc = gameRom.getDesc();
            if(!image.equals("")){
                File file = new File(image);
                if(file.exists()) {
                    ImageLoader.getInstance().displayImage(image, holdView.game_roms_item_image);
                }else{
                    //drawable://
                    ImageLoader.getInstance().displayImage("drawable://" + R.mipmap.ic_launcher, holdView.game_roms_item_image);
                }
            }else{
                ImageLoader.getInstance().displayImage("drawable://" + R.mipmap.ic_launcher, holdView.game_roms_item_image);
            }
            holdView.game_roms_item_name.setText(name);
            if(desc.equals("")){
                holdView.game_roms_item_desc.setVisibility(View.GONE);
            }else{
                holdView.game_roms_item_desc.setVisibility(View.VISIBLE);
                holdView.game_roms_item_desc.setText(desc);
            }

            return convertView;
        }
        class HoldView{
            ImageView game_roms_item_image= null;
            TextView game_roms_item_name= null;
            TextView game_roms_item_desc= null;
        }
    }
}
