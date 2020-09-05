package lilinhong.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.tencent.mmkv.MMKV;

import org.libsdl.app.R;
import java.util.ArrayList;
import java.util.List;
import lilinhong.adapter.MainFragmentPagerAdapter;
import lilinhong.fragment.CollectFragment;
import lilinhong.fragment.RomsFragment;
import lilinhong.utils.GlobalConfig;
import lilinhong.utils.PreferencesData;

public class MainActivity extends AppCompatActivity {
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
                RomsFragment romsFragment = (RomsFragment)fragmentList.get(0);
                romsFragment.fileScanDialog();
                return true;
            case R.id.game_roms_set:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
}
