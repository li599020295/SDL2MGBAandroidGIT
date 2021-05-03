package lilinhong.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import org.libsdl.app.R;
import java.util.ArrayList;
import java.util.List;
import lilinhong.adapter.MainFragmentPagerAdapter;
import lilinhong.dialog.SearchFileDialog;
import lilinhong.fragment.CollectFragment;
import lilinhong.fragment.RomsFragment;
import lilinhong.utils.AdmobHelper;
import lilinhong.utils.GlobalConfig;
import lilinhong.utils.PreferencesData;
import lilinhong.utils.Utils;

public class MainActivity extends AppCompatActivity {
    //没有网络延时窗口
    private Dialog delayDialog = null;
    private AdmobHelper admobHelper = null;
    //存储权限返回标志
    private static final int REQUEST_EXTERNAL_STORAGE = 3;
    private static MainActivity mainActivity = null;
    private TabLayout main_tablayout = null;
    private ViewPager main_viewpage = null;
    private PreferencesData preferencesData = null;
    private MainFragmentPagerAdapter mainFragmentPagerAdapter = null;
    private List<Fragment>fragmentList = null;
    private List<String>nameList = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        initData();
        initUI();
        initFinish();
    }
    private void initData(){
        admobHelper = new AdmobHelper(this);
        mainActivity = this;
        preferencesData = PreferencesData.getInstance(MainActivity.this);
        nameList = new ArrayList<>();
        nameList.add(getString(R.string.game_name));
        nameList.add(getString(R.string.game_collect));
        fragmentList = new ArrayList<>();
        fragmentList.add(new RomsFragment());
        fragmentList.add(new CollectFragment());
        mainFragmentPagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(),fragmentList);
        GlobalConfig.VIRTUAL_BUTTON_CONTROL = preferencesData.getVirtualButtonControl();
    }
    private void initUI(){
        main_tablayout = findViewById(R.id.main_tablayout);
        main_viewpage = findViewById(R.id.main_viewpage);
        main_viewpage.setAdapter(mainFragmentPagerAdapter);
        main_tablayout.setupWithViewPager(main_viewpage);
    }
    private void initFinish(){
        for (int i=0;i<fragmentList.size();i++){
            View view = LayoutInflater.from(this).inflate(R.layout.main_tab_item,null);
            TextView itemName = view.findViewById(R.id.main_tab_item_name);
            itemName.setText(nameList.get(i));
            main_tablayout.getTabAt(i).setCustomView(view);
        }
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
                SearchFileDialog searchFileDialog = new SearchFileDialog(MainActivity.this);
                searchFileDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        RomsFragment romsFragment = (RomsFragment)fragmentList.get(0);
                        romsFragment.reFreshData();
                    }
                });
                searchFileDialog.show();
                return true;
            case R.id.game_roms_wu_start:
                Utils.goGooglePlay(MainActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public AdmobHelper getAdmobHelper(){
        return admobHelper;
    }

    //是否刷新
    public void setFragmentRefresh(){
        if(fragmentList == null || fragmentList.size() == 0){
            return;
        }
        RomsFragment romsFragment = (RomsFragment) fragmentList.get(0);
        if(romsFragment!=null){
            romsFragment.setReFresh(true);
        }
       CollectFragment collectFragment = (CollectFragment) fragmentList.get(1);
       if(collectFragment!=null){
           collectFragment.setReFresh(true);
       }
    }
    public static MainActivity getMainActivity(){
        return mainActivity;
    }
    //延时进入游戏
    public void delayGoGame(){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(LayoutInflater.from(this).inflate(R.layout.dialog_loading,null));
        builder.setCancelable(false);
        delayDialog = builder.create();
        delayDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(3500);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                if(!isFinishing() && delayDialog!=null && delayDialog.isShowing()){
                                    delayDialog.dismiss();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            if(admobHelper!=null){
                                boolean isOK = admobHelper.showInterstitial();
                                if(!isOK){
                                    admobHelper.loadGame();
                                }
                            }
                        }
                    });
                }catch (Exception e){}
            }
        }).start();
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
                SearchFileDialog searchFileDialog = new SearchFileDialog(MainActivity.this);
                searchFileDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        RomsFragment romsFragment = (RomsFragment)fragmentList.get(0);
                        romsFragment.reFreshData();
                    }
                });
                searchFileDialog.show();
            }
        }
    }
}
