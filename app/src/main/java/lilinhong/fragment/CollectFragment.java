package lilinhong.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
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
import lilinhong.dialog.GameInfoDialog;
import lilinhong.model.GameRom;
import lilinhong.utils.PreferencesData;

public class CollectFragment extends Fragment {
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                List<GameRom>romList = (List<GameRom>)msg.obj;
                adapter.setRomsDataList(romList);
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.collect_roms_fragment, container, false);
        initData();
        initUI();
        Log.i(TAG,"onCreateView");
        return mainView;
    }

    public void reFreshData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<GameRom> collectRomList = preferencesData.getCollectRoms();
                Message msg = handler.obtainMessage();
                msg.what = 1;
                msg.obj = collectRomList;
                handler.sendMessage(msg);
            }
        }).start();
    }

    public void setReFresh(boolean isReFresh){
        this.isReFresh = isReFresh;
    }

    private void initData() {
        preferencesData = PreferencesData.getInstance(getContext());
        gameRomList = preferencesData.getCollectRoms();
        adapter = new CollectGameRomsAdapter(getActivity(),gameRomList);
    }

    private void initUI() {
        collect_game_roms_listview = (ListView)mainView.findViewById(R.id.collect_game_roms_listview);
        collect_game_roms_listview.setAdapter(adapter);
        collect_game_roms_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GameRom gameRom = (GameRom)adapter.getItem(position);
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
        private List<GameRom> gameRomList = null;

        public void setRomsDataList(List<GameRom> gameRomList) {
            this.gameRomList = gameRomList;
        }

        public CollectGameRomsAdapter(Context context, List<GameRom> gameRomList) {
            this.context = context;
            this.gameRomList = gameRomList;
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
        public View getView(int position, View convertView, ViewGroup parent) {
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
                    }
                });
                convertView.setTag(holdView);
            }else{
                holdView = (HoldView)convertView.getTag();
            }
            //设置游戏Romd的位置
            holdView.collect_roms_fragment_info_imagebtn.setTag(gameRom);
            String image =  gameRom.getImage();
            String name = gameRom.getName();
            String desc = gameRom.getDesc();

            if(!image.equals("")){
                File file = new File(image);
                if(file.exists()) {
                    ImageLoader.getInstance().displayImage(image, holdView.collect_roms_fragment_item_image);
                }else{
                    //drawable://
                    ImageLoader.getInstance().displayImage("drawable://" + R.mipmap.ic_launcher, holdView.collect_roms_fragment_item_image);
                }
            }else{
                ImageLoader.getInstance().displayImage("drawable://" + R.mipmap.ic_launcher, holdView.collect_roms_fragment_item_image);
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
