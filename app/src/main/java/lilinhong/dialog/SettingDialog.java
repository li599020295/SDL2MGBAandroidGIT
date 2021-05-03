package lilinhong.dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import org.libsdl.app.GamePadRelativeLayout;
import org.libsdl.app.R;
import org.libsdl.app.SDLActivity;
import java.io.File;
import java.util.List;
import lilinhong.activity.GamePadActivity;
import lilinhong.model.CheatData;
import lilinhong.utils.GlobalConfig;
import lilinhong.utils.PreferencesData;
import lilinhong.utils.Utils;

public class SettingDialog extends BaseDialog {
    private View viewSetNormal = null;
    private View viewSetSlot = null;
    private View viewSetCheat = null;
    private Button set_normal_include_btn_audio = null;
    private SlotAdapter slotAdapter = null;
    private Context context = null;
    private String gamePath = null;
    private boolean audioSwitch = true;
    private LinearLayout set_cheat_include_linrat_listcode = null;
    private LinearLayout set_cheat_include_linrat_code = null;
    //返回码用于处理关闭事件显示一些东西 1 = button,
    private int backCode = -1;
    //秘籍list
    private List<CheatData>cheatDataList = null;
    //数据管理
    private PreferencesData preferencesData = null;
    //作弊器适配器
    private CheatAdapter cheatAdapter = null;
    private String gameName = "";
    public SettingDialog(Context context,String gamePath) {
        super(context, R.style.mdialog);
        this.context = context;
        this.gamePath = gamePath;
        this.initData();
        this.initUI();
    }

    private void initData() {
        File file = new File(this.gamePath);
        gameName = file.getName();
        if(preferencesData == null){
            preferencesData = PreferencesData.getInstance(this.context);
        }
        preferencesData = PreferencesData.getInstance(context);
        cheatDataList = preferencesData.getCheatList(gameName);
        if(!GlobalConfig.FIRST_RUN_GAME_CHEAT.equals(gameName)) {
            for (CheatData cheatData : cheatDataList) {
                cheatData.setEnable(false);
                //第一次进入关闭作弊码
                SDLActivity.addCheatCode(cheatData.getName(),cheatData.getCode(),false);
            }
            preferencesData.setCheatList(cheatDataList, gameName);
        }
        GlobalConfig.FIRST_RUN_GAME_CHEAT = gameName;
        slotAdapter = new SlotAdapter(context);
        cheatAdapter = new CheatAdapter();
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
        final Button set_normal_include_btn_shbutton = viewSetNormal.findViewById(R.id.set_normal_include_btn_shbutton);
        Button set_normal_include_btn_quick_save = viewSetNormal.findViewById(R.id.set_normal_include_btn_quick_save);
        Button set_normal_include_btn_quick_read = viewSetNormal.findViewById(R.id.set_normal_include_btn_quick_read);
        set_normal_include_btn_quick_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SDLActivity.onSlotNum(0,false);
                dismiss();
            }
        });
        set_normal_include_btn_quick_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SDLActivity.onSlotNum(0,true);
                dismiss();
            }
        });
        set_normal_include_btn_shbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean vbc = !preferencesData.getVirtualButtonControl();
                preferencesData.setVirtualButtonControl(vbc);
                if(vbc){
                    set_normal_include_btn_shbutton.setText(context.getString(R.string.hide_button));
                }else{
                    set_normal_include_btn_shbutton.setText(context.getString(R.string.show_button));
                }
                GlobalConfig.VIRTUAL_BUTTON_CONTROL = vbc;
                dismiss();
            }
        });
        boolean vbc = !preferencesData.getVirtualButtonControl();
        if(vbc){
            set_normal_include_btn_shbutton.setText(context.getString(R.string.show_button));
        }else{
            set_normal_include_btn_shbutton.setText(context.getString(R.string.hide_button));
        }

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
                setBackCode(1);
                dismiss();
            }
        });
        set_normal_include_btn_gamepad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GamePadActivity.class);
                context.startActivity(intent);
                dismiss();
            }
        });
        //存档设置
        ListView set_slot_include_listview = (ListView)viewSetSlot.findViewById(R.id.set_slot_include_listview);
        set_slot_include_listview.setAdapter(slotAdapter);
        this.changeAudio();

        //add cheat
        set_cheat_include_linrat_listcode = viewSetCheat.findViewById(R.id.set_cheat_include_linrat_listcode);
        set_cheat_include_linrat_code = viewSetCheat.findViewById(R.id.set_cheat_include_linrat_code);
        ListView set_cheat_include_listview = set_cheat_include_linrat_listcode.findViewById(R.id.set_cheat_include_listview);
        set_cheat_include_listview.setAdapter(cheatAdapter);
        final EditText set_cheat_include_edit_codename = set_cheat_include_linrat_code.findViewById(R.id.set_cheat_include_edit_codename);
        final EditText set_cheat_include_edit_code = set_cheat_include_linrat_code.findViewById(R.id.set_cheat_include_edit_code);
        Button set_cheat_include_btn_addcode = set_cheat_include_linrat_listcode.findViewById(R.id.set_cheat_include_btn_addcode);
        set_cheat_include_btn_addcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_cheat_include_linrat_code.setVisibility(View.VISIBLE);
            }
        });
        set_cheat_include_linrat_code.setVisibility(View.INVISIBLE);
        Button set_cheat_include_btn_codeok = set_cheat_include_linrat_code.findViewById(R.id.set_cheat_include_btn_codeok);
        set_cheat_include_btn_codeok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(set_cheat_include_edit_codename.getText())){
                    Toast.makeText(context,context.getString(R.string.input_chaet_name),Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(set_cheat_include_edit_code.getText())){
                    Toast.makeText(context,context.getString(R.string.input_chaet_code),Toast.LENGTH_SHORT).show();
                    return;
                }
                String name = set_cheat_include_edit_codename.getText().toString();
                String codeData = set_cheat_include_edit_code.getText().toString();
                int code = SDLActivity.addCheatCode(name,codeData,true);
                if(code == 0|| code < 0){
                    Toast.makeText(context,context.getString(R.string.cheat_error),Toast.LENGTH_SHORT).show();
                    return;
                }
                cheatDataList.add(new CheatData(name,codeData));

                preferencesData.setCheatList(cheatDataList,gameName);
                cheatAdapter.notifyDataSetChanged();
                set_cheat_include_linrat_code.setVisibility(View.INVISIBLE);
            }
        });
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

    public int getBackCode() {
        return backCode;
    }

    public void setBackCode(int backCode) {
        this.backCode = backCode;
    }

    class CheatAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return cheatDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return cheatDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return cheatDataList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CheatData cheatData = cheatDataList.get(position);
            HoldView holdView = null;
            if(convertView == null){
                holdView = new HoldView();
                convertView = LayoutInflater.from(context).inflate(R.layout.set_cheat_include_listview_item,null);
                holdView.set_cheat_include_listview_item_text_name = convertView.findViewById(R.id.set_cheat_include_listview_item_text_name);
                holdView.set_cheat_include_listview_item_text_code = convertView.findViewById(R.id.set_cheat_include_listview_item_text_code);
                holdView.set_cheat_include_listview_item_toggle_switch = convertView.findViewById(R.id.set_cheat_include_listview_item_toggle_switch);
                holdView.set_cheat_include_listview_item_imagebtn_delete = convertView.findViewById(R.id.set_cheat_include_listview_item_imagebtn_delete);
                holdView.set_cheat_include_listview_item_toggle_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int pos = (int)buttonView.getTag();
                        SDLActivity.enableCheat(pos,isChecked);
                        cheatDataList.get(pos).setEnable(isChecked);
                        preferencesData.setCheatList(cheatDataList,gameName);
                    }
                });

                holdView.set_cheat_include_listview_item_imagebtn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = (int)v.getTag();
                        SDLActivity.removeCheat(pos);
                        cheatDataList.remove(pos);
                        preferencesData.setCheatList(cheatDataList,gameName);
                        cheatAdapter.notifyDataSetChanged();
                    }
                });
                convertView.setTag(holdView);
            }
            holdView = (HoldView) convertView.getTag();
            holdView.set_cheat_include_listview_item_imagebtn_delete.setTag(position);
            holdView.set_cheat_include_listview_item_toggle_switch.setTag(position);
            holdView.set_cheat_include_listview_item_toggle_switch.setChecked(cheatData.isEnable());
            holdView.set_cheat_include_listview_item_text_name.setText(cheatData.getName());
            holdView.set_cheat_include_listview_item_text_code.setText(cheatData.getCode());
            return convertView;
        }

        class HoldView{
            TextView set_cheat_include_listview_item_text_name;
            TextView set_cheat_include_listview_item_text_code;
            ToggleButton set_cheat_include_listview_item_toggle_switch;
            ImageButton set_cheat_include_listview_item_imagebtn_delete;
        }
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
                        slotAdapter.notifyDataSetChanged();
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
                        slotAdapter.notifyDataSetChanged();
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
