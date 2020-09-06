package lilinhong.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import org.libsdl.app.R;

import java.util.ArrayList;
import java.util.List;

import lilinhong.dialog.MapKeyCodeDialog;
import lilinhong.model.GamePad;
import lilinhong.utils.PreferencesData;
import lilinhong.utils.Utils;

public class GamePadActivity extends AppCompatActivity {
    private PreferencesData preferencesData = null;
    private List<GamePad> gamePadList = null;
    private GamePadAdapter adapter = null;
    private int bgSelectColor = -1;
    private int bgUnSelectColor = -1;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamepad_activity);
        initData();
        initUI();
    }
    private void initData() {
        preferencesData = PreferencesData.getInstance();
        gamePadList = preferencesData.getGamePadList1(this);
        bgSelectColor = Utils.getColor(this,R.color.colorPrimary);
        bgUnSelectColor = Utils.getColor(this,R.color.white);
        adapter = new GamePadAdapter();
    }
    private void initUI() {
        ListView gamepad_activity_listview_player1 = findViewById(R.id.gamepad_activity_listview_player1);
        final Button gamepad_activity_btn_player1 = findViewById(R.id.gamepad_activity_btn_player1);
        final Button gamepad_activity_btn_player2 = findViewById(R.id.gamepad_activity_btn_player2);
        gamepad_activity_btn_player1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gamepad_activity_btn_player1.setBackgroundColor(bgSelectColor);
                gamepad_activity_btn_player2.setBackgroundColor(bgUnSelectColor);
            }
        });
        gamepad_activity_btn_player2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GamePadActivity.this,getString(R.string.no_current_supperted),Toast.LENGTH_SHORT).show();
                //gamepad_activity_btn_player1.setBackgroundColor(bgUnSelectColor);
                //gamepad_activity_btn_player2.setBackgroundColor(bgSelectColor);
            }
        });
        gamepad_activity_listview_player1.setAdapter(adapter);
        gamepad_activity_listview_player1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GamePad gamePad = gamePadList.get(position);
                final MapKeyCodeDialog mapKeyCodeDialog = new MapKeyCodeDialog(GamePadActivity.this,gamePad.getKeyCode(),gamePad.getKeyMapCode());

                mapKeyCodeDialog.show();
                mapKeyCodeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        int mapKey = mapKeyCodeDialog.getMapKeyCode();
                        if(mapKey == -1){
                            return;
                        }
                        for (int i=0;i<gamePadList.size();i++){
                            GamePad gp = gamePadList.get(i);
                            if(gp.getKeyCode() == mapKeyCodeDialog.getKeyCode()){
                                gp.setKeyMapCode(mapKey);
                            }
                        }
                        preferencesData.saveGamePadList1(gamePadList);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    class GamePadAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return gamePadList.size();
        }

        @Override
        public Object getItem(int position) {
            return gamePadList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return gamePadList.get(position).getKeyCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GamePad gamePad = gamePadList.get(position);
            HoldView holdView = null;
            if(convertView == null){
                holdView = new HoldView();
                convertView = LayoutInflater.from(GamePadActivity.this).inflate(R.layout.gamepad_listview_item,null);
                holdView.gamepad_listview_item_text_keydesc = convertView.findViewById(R.id.gamepad_listview_item_text_keydesc);
                holdView.gamepad_listview_item_text_mapkey = convertView.findViewById(R.id.gamepad_listview_item_text_mapkey);
                convertView.setTag(holdView);
            }else {
                holdView = (HoldView)convertView.getTag();
            }

            holdView.gamepad_listview_item_text_keydesc.setText(gamePad.getDesc());
            int mapKey = gamePad.getKeyMapCode();
            if(mapKey != -1){
                holdView.gamepad_listview_item_text_mapkey.setText(GamePadActivity.this.getString(R.string.gamepad_code)+" : "+mapKey+" ("+((char)(mapKey+1))+")");
            }else{
                holdView.gamepad_listview_item_text_mapkey.setText(GamePadActivity.this.getString(R.string.gamepad_code)+":"+GamePadActivity.this.getString(R.string.no));
            }
            return convertView;
        }

        class HoldView{
            TextView gamepad_listview_item_text_keydesc;
            TextView gamepad_listview_item_text_mapkey;
        }
    }
}
