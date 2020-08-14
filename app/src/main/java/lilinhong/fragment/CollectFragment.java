package lilinhong.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;
import org.libsdl.app.R;
import java.util.List;
import lilinhong.model.GameRom;
import lilinhong.utils.PreferencesData;

public class CollectFragment extends Fragment {
    private CollectGameRomsAdapter adapter;
    private View mainView = null;
    private ListView collect_game_roms_listview = null;
    private PreferencesData preferencesData = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.collect_roms_fragment, container, false);
        initData();
        initUI();
        return mainView;
    }

    private void initData() {
        preferencesData = PreferencesData.getInstance(getContext());
    }

    private void initUI() {
        collect_game_roms_listview = (ListView)mainView.findViewById(R.id.collect_game_roms_listview);
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
