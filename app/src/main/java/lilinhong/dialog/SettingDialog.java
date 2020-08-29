package lilinhong.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.libsdl.app.GamePadRelativeLayout;
import org.libsdl.app.R;
import org.libsdl.app.SDLActivity;
import java.io.File;
import lilinhong.utils.Utils;

public class SettingDialog extends BaseDialog {
    private View viewSetNormal = null;
    private View viewSetSlot = null;
    private View viewSetCheat = null;
    private Button set_normal_include_btn_audio = null;
    private SlotAdapter adapter = null;
    private Context context = null;
    private String gamePath = null;
    private boolean audioSwitch = true;
    public SettingDialog(Context context,String gamePath) {
        super(context, R.style.mdialog);
        this.context = context;
        this.gamePath = gamePath;
        this.initData();
        this.initUI();
    }

    private void initData() {
        adapter = new SlotAdapter(context);
    }

    private void initUI(){
        setContentView(R.layout.set_dialog);
        Button set_dialog_btn_normal = findViewById(R.id.set_dialog_btn_normal);
        Button set_dialog_btn_slot = findViewById(R.id.set_dialog_btn_slot);
        Button set_dialog_btn_cheat = findViewById(R.id.set_dialog_btn_cheat);
        Button set_dialog_btn_close = findViewById(R.id.set_dialog_btn_close);
        viewSetNormal = findViewById(R.id.set_dialog_include_normal);
        viewSetSlot = findViewById(R.id.set_dialog_include_slot);
        viewSetCheat = findViewById(R.id.set_dialog_include_cheat);

        set_dialog_btn_normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePage(0);
            }
        });

        set_dialog_btn_slot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePage(1);
            }
        });

        set_dialog_btn_cheat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePage(2);
            }
        });

        set_dialog_btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        changePage(0);
        //普通设置
        Button set_normal_include_btn_restart = viewSetNormal.findViewById(R.id.set_normal_include_btn_restart);
        set_normal_include_btn_audio = viewSetNormal.findViewById(R.id.set_normal_include_btn_audio);
        Button set_normal_include_btn_button = viewSetNormal.findViewById(R.id.set_normal_include_btn_button);
        Button set_normal_include_btn_gamepad = viewSetNormal.findViewById(R.id.set_normal_include_btn_gamepad);

        set_normal_include_btn_restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SDLActivity.onDataKey(GamePadRelativeLayout.PAD1_R,true);
                dismiss();
            }
        });

        set_normal_include_btn_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioSwitch = !audioSwitch;
                changeAudio();
                SDLActivity.onDataKey(GamePadRelativeLayout.PAD1_V,audioSwitch);

            }
        });
        set_normal_include_btn_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        set_normal_include_btn_gamepad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //存档设置
        ListView set_slot_include_listview = (ListView)viewSetSlot.findViewById(R.id.set_slot_include_listview);
        set_slot_include_listview.setAdapter(adapter);

        this.changeAudio();
    }

    private void changeAudio(){
        if(audioSwitch){
            set_normal_include_btn_audio.setText(context.getString(R.string.audio)+":"+context.getString(R.string.on));
        }else{
            set_normal_include_btn_audio.setText(context.getString(R.string.audio)+":"+context.getString(R.string.off));
        }
    }
    private void changePage(int index){
        viewSetNormal.setVisibility(View.GONE);
        viewSetSlot.setVisibility(View.GONE);
        viewSetCheat.setVisibility(View.GONE);
        if (index == 0) {
            viewSetNormal.setVisibility(View.VISIBLE);
        }else if (index == 1) {
            viewSetSlot.setVisibility(View.VISIBLE);
        }else if (index == 2) {
            viewSetCheat.setVisibility(View.VISIBLE);
        }
    }

    public void setAudioSwitch(boolean audioSwitch){
        this.audioSwitch = audioSwitch;
        changeAudio();
    }
    public boolean getAudioSwitch(){
        return audioSwitch;
    }
    class SlotAdapter extends BaseAdapter {
        private Context context;
        public SlotAdapter(Context context){
            this.context = context;
        }

        @Override
        public int getCount() {
            return 8;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HoldView holdView = null;
            if(convertView == null){
                holdView = new HoldView();
                convertView = LayoutInflater.from(context).inflate(R.layout.set_slot_include_listview_item,null);
                holdView.set_slot_include_listview_item_text_tips = convertView.findViewById(R.id.set_slot_include_listview_item_text_tips);
                holdView.set_slot_include_listview_item_relative_add = convertView.findViewById(R.id.set_slot_include_listview_item_relative_add);
                holdView.set_slot_include_listview_item_linear_content = convertView.findViewById(R.id.set_slot_include_listview_item_linear_content);
                holdView.set_slot_include_listview_item_imagebtn_add = convertView.findViewById(R.id.set_slot_include_listview_item_imagebtn_add);
                holdView.set_slot_include_listview_item_image_icon = convertView.findViewById(R.id.set_slot_include_listview_item_image_icon);
                holdView.set_slot_include_listview_item_text_slot = convertView.findViewById(R.id.set_slot_include_listview_item_text_slot);
                holdView.set_slot_include_listview_item_text_time = convertView.findViewById(R.id.set_slot_include_listview_item_text_time);
                holdView.set_slot_include_listview_item_btn_start = convertView.findViewById(R.id.set_slot_include_listview_item_btn_start);
                holdView.set_slot_include_listview_item_btn_delete = convertView.findViewById(R.id.set_slot_include_listview_item_btn_delete);
                holdView.set_slot_include_listview_item_btn_start.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = (int)v.getTag();
                        SDLActivity.onSlotNum(pos,false);
                        dismiss();
                    }
                });
                holdView.set_slot_include_listview_item_imagebtn_add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = (int)v.getTag();
                        SDLActivity.onSlotNum(pos,true);
                        adapter.notifyDataSetChanged();
                    }
                });
                holdView.set_slot_include_listview_item_btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = (int)v.getTag();
                        String filePath = Utils.getSlotPath(gamePath,pos);
                        File file = new File(filePath);
                        if(file.exists()){
                            file.delete();
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
                convertView.setTag(holdView);
            }
            holdView = (HoldView)convertView.getTag();
            holdView.set_slot_include_listview_item_btn_start.setTag(position);
            holdView.set_slot_include_listview_item_imagebtn_add.setTag(position);
            holdView.set_slot_include_listview_item_btn_delete.setTag(position);
            holdView.set_slot_include_listview_item_text_slot.setText(String.format(context.getString(R.string.slot),position+1));
            String slotPath = Utils.getSlotPath(gamePath,position);
            File file = new File(slotPath);
            if(file.exists()){
                Bitmap bitmap = Utils.getLoacalBitmap(slotPath);
                holdView.set_slot_include_listview_item_image_icon.setImageBitmap(bitmap);
                holdView.set_slot_include_listview_item_linear_content.setVisibility(View.VISIBLE);
                holdView.set_slot_include_listview_item_relative_add.setVisibility(View.INVISIBLE);

                String fileTime = Utils.getFileLastModifiedTime(file);
                holdView.set_slot_include_listview_item_text_time.setText(String.format(context.getString(R.string.time_s),fileTime));
            }else{
                holdView.set_slot_include_listview_item_linear_content.setVisibility(View.INVISIBLE);
                holdView.set_slot_include_listview_item_relative_add.setVisibility(View.VISIBLE);

                holdView.set_slot_include_listview_item_text_tips.setText(String.format(context.getString(R.string.save_game_data_slot),position+1));
            }

            return convertView;
        }
        class HoldView{
            RelativeLayout set_slot_include_listview_item_relative_add;
            TextView set_slot_include_listview_item_text_tips = null;
            LinearLayout set_slot_include_listview_item_linear_content;
            ImageButton set_slot_include_listview_item_imagebtn_add;
            ImageView set_slot_include_listview_item_image_icon;
            TextView set_slot_include_listview_item_text_slot;
            TextView set_slot_include_listview_item_text_time;
            ImageButton set_slot_include_listview_item_btn_start;
            ImageButton set_slot_include_listview_item_btn_delete;
        }
    }

}
