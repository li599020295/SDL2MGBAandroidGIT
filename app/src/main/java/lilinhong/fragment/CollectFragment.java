package lilinhong.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.google.ads.mediation.admob.AdMobAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.libsdl.app.R;
import org.libsdl.app.SDLActivity;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lilinhong.activity.MainActivity;
import lilinhong.dialog.GameInfoDialog;
import lilinhong.model.GameRom;
import lilinhong.model.IconData;
import lilinhong.utils.AdmobHelper;
import lilinhong.utils.PreferencesData;
import lilinhong.utils.Utils;

public class CollectFragment extends Fragment {
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                adapter.notifyDataSetChanged();
            }
        }
    };
    private String TAG = CollectFragment.class.getName();
    private CollectGameRomsAdapter adapter;
    private List<GameRom>gameRomList = null;
    private View mainView = null;
    private ListView collect_game_roms_listview = null;
    private PreferencesData preferencesData = null;
    private boolean isReFresh = false;
    //保存icon数据防止重复加载
    private Map<Integer, IconData> iconMap = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.collect_roms_fragment, container, false);
        initData();
        initUI();
        Log.i(TAG,"onCreateView");
        return mainView;
    }

    public void onResume(){
        super.onResume();

        if(preferencesData == null){
            preferencesData = PreferencesData.getInstance(getActivity());
        }

        if(preferencesData!=null && gameRomList!=null){
            gameRomList = preferencesData.getCollectRoms();
        }
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
    }

    public void reFreshData(){
        gameRomList = preferencesData.getCollectRoms();
        Message msg = handler.obtainMessage();
        msg.what = 1;
        handler.sendMessage(msg);
    }

    public void setReFresh(boolean isReFresh){
        this.isReFresh = isReFresh;
    }

    private void initData() {
        iconMap = new HashMap<>();
        preferencesData = PreferencesData.getInstance();
        gameRomList = preferencesData.getCollectRoms();
        adapter = new CollectGameRomsAdapter(getActivity());
    }

    private void initUI() {
        collect_game_roms_listview = (ListView)mainView.findViewById(R.id.collect_game_roms_listview);
        collect_game_roms_listview.setAdapter(adapter);
        collect_game_roms_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GameRom gameRom = (GameRom)adapter.getItem(position);
                MainActivity mainActivity = MainActivity.getMainActivity();
                if(mainActivity!=null){
                    AdmobHelper admobHelper = mainActivity.getAdmobHelper();
                    if(admobHelper!=null){
                        admobHelper.setGameDescription(gameRom);
                        boolean isOK = admobHelper.showInterstitial(false);
                        if(!isOK){
                            mainActivity.delayGoGame();
                        }
                        return;
                    }
                }

                Intent sdlActivityIntent = new Intent(getActivity(), SDLActivity.class);
                Bundle bundle=new Bundle();
                bundle.putSerializable("GAME_ROM",gameRom);
                sdlActivityIntent.putExtras(bundle);
                startActivity(sdlActivityIntent);
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isReFresh){
            reFreshData();
            isReFresh = false;
        }
    }

    class CollectGameRomsAdapter extends BaseAdapter {
        private Context context;

        public CollectGameRomsAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return gameRomList.size();
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
            GameRom gameRom = gameRomList.get(position);
            HoldView holdView = null;
            if(convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.collect_roms_fragment_listview_item,null);
                holdView = new HoldView();
                holdView.collect_roms_fragment_item_image = (ImageView)convertView.findViewById(R.id.collect_roms_fragment_item_image);
                holdView.collect_roms_fragment_item_name = (TextView) convertView.findViewById(R.id.collect_roms_fragment_item_name);
                holdView.collect_roms_fragment_item_desc = (TextView)convertView.findViewById(R.id.collect_roms_fragment_item_desc);
                holdView.collect_roms_fragment_info_imagebtn = (ImageButton)convertView.findViewById(R.id.collect_roms_fragment_info_imagebtn);
                holdView.collect_roms_fragment_togglebtn_colle = (ToggleButton)convertView.findViewById(R.id.collect_roms_fragment_togglebtn_colle);
                holdView.collect_roms_fragment_info_imagebtn.setOnClickListener(new View.OnClickListener() {
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

                holdView.collect_roms_fragment_togglebtn_colle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                            GameRom gr = gameRomList.get(position);
                            gr.setCollect(isChecked);
                        }

                        preferencesData.setCollectRom(gameRom1);
                        gameRomList = preferencesData.getCollectRoms();

                        notifyDataSetChanged();
                        MainActivity.getMainActivity().setFragmentRefresh();
                    }
                });
                convertView.setTag(holdView);
            }else{
                holdView = (HoldView)convertView.getTag();
            }
            //设置游戏Romd的位置
            holdView.collect_roms_fragment_info_imagebtn.setTag(gameRom);
            holdView.collect_roms_fragment_togglebtn_colle.setTag(gameRom);
            String image =  gameRom.getImage();
            String name = gameRom.getName();
            String desc = gameRom.getDesc();

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
                    holdView.collect_roms_fragment_item_image.setImageBitmap(bitmap);
                }else{
                    //drawable://
                    ImageLoader.getInstance().displayImage("drawable://" + R.mipmap.gba_item_icon, holdView.collect_roms_fragment_item_image);
                }
            }else{
                ImageLoader.getInstance().displayImage("drawable://" + R.mipmap.gba_item_icon, holdView.collect_roms_fragment_item_image);
            }

            holdView.collect_roms_fragment_item_name.setText(name);

            if(desc.equals("")){
                holdView.collect_roms_fragment_item_desc.setVisibility(View.GONE);
            }else{
                holdView.collect_roms_fragment_item_desc.setVisibility(View.VISIBLE);
                holdView.collect_roms_fragment_item_desc.setText(desc);
            }

            boolean isCollection = gameRom.isCollect();
            holdView.collect_roms_fragment_togglebtn_colle.setChecked(isCollection);
            return convertView;
        }

        class HoldView{
            ImageButton collect_roms_fragment_info_imagebtn = null;
            ImageView collect_roms_fragment_item_image= null;
            TextView collect_roms_fragment_item_name= null;
            TextView collect_roms_fragment_item_desc= null;
            ToggleButton collect_roms_fragment_togglebtn_colle = null;
        }
    }
}
