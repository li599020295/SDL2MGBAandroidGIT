package org.libsdl.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lilinhong.activity.GamePadActivity;
import lilinhong.dialog.LoadSlotDialog;
import lilinhong.dialog.SaveSlotDialog;
import lilinhong.dialog.SettingDialog;
import lilinhong.model.GamePad;
import lilinhong.model.ViewSize;
import lilinhong.utils.GlobalConfig;
import lilinhong.utils.PreferencesData;
import lilinhong.utils.Utils;

public class GamePadRelativeLayout extends RelativeLayout {
    //1=横向不全屏 ，2=横向全屏，3=不横向不全屏
    private static int SCREEN_MODE = 2;
    private final static int SDLK_SCANCODE_MASK = (1<<30);
    //按钮设置
    //so玩家1对应按键位置
    public final static int PAD1_UP = (82| SDLK_SCANCODE_MASK);
    public final static int PAD1_DOWN = (81| SDLK_SCANCODE_MASK);
    public final static int PAD1_LEFT = (80| SDLK_SCANCODE_MASK);
    public final static int PAD1_RIGHT = (79| SDLK_SCANCODE_MASK);
    public static final int PAD1_START = (int)'\r';
    public static final int PAD1_SELECT = (int)'\b';
    public static final int PAD1_L = (int) 'a';
    public static final int PAD1_S = (int)'s';
    public static final int PAD1_A = (int)'x';
    public static final int PAD1_B = (int)'z';
    //重置
    public static final int PAD1_R = (int)'r';
    //声音
    public static final int PAD1_V = (int)'v';
    //加速
    public static final int PAD1_SPEED = (int)'\t';
    //倒退
    public static final int PAD1_REWIND = (int)'`';
    //截图
    public static final int PAD1_CAPTURE =(69|SDLK_SCANCODE_MASK);
    //快速保存
    public static final int PAD1_SAVE_SLOT1 = 1000001;
    //快速读取
    public static final int PAD1_LOAD_SLOT1 = 1000002;
    private Context context;
    private GamePadView gamePadView = null;
    private  View viewGamePadUtil = null;
    //设置里面的按钮缩放
    private LinearLayout game_relative_util_linear_buttonsize = null;
    //记录声音是否打开
    private boolean audioSwitch = true;
    //存储按钮的视图
    private ArrayList<View> gamePadList = null;
    //所有按钮除了菜单
    private ArrayList<View> gameAllButton = null;
    //保存map gamepad1
    private Map<Integer,Integer> gamepadMap1 = null;
    //数据保存
    private PreferencesData preferencesData = null;
    //用于移动开始和选择按钮
    private LinearLayout gamepad_relative_linear_startselect = null;
    //
    private SeekBar game_relative_util_linear_seeksize = null;
    //在视图初始化完成需要加载的数据
    private boolean firstLoadData = false;
    public GamePadRelativeLayout(Context context) {
        super(context);
        this.context = context;
        this.initData();
        this.initUI();
        this.initUIFinish();
    }

    private void initUIFinish() {
        //初始化数据
        this.initGamePad();
        this.virtualButtonControl();
    }

    private void initUI() {
        RelativeLayout gamepadRelativeLayout = (RelativeLayout)LayoutInflater.from(this.context).inflate(R.layout.gamepad_relative_layout,null);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(gamepadRelativeLayout,params);
        this.viewGamePadUtil = gamepadRelativeLayout.findViewById(R.id.gamepad_relative_util_layout);
        //b初始化设置utton Size
        this.initUISetButtonSize();
        this.setGamePadUtilsVisible(false);
        //初始化按钮
        this.initUIButton(gamepadRelativeLayout);
    }

    public void onWindowFocusChanged(boolean hasWindowFocus){
        super.onWindowFocusChanged(hasWindowFocus);
        //设置宽高
        for(int i=0;i<gamePadList.size();i++){
            View view = gamePadList.get(i);
            Object obj = view.getTag();
            if(obj!=null){
                continue;
            }
            view.setTag(new ViewSize(view.getWidth(),view.getHeight(),view.getTop(),view.getLeft(),view.getRight(),view.getBottom()));
        }

        if(!firstLoadData){
            int svbs = preferencesData.getVirtualButtonSize();
            game_relative_util_linear_seeksize.setProgress(svbs);

            firstLoadData = true;
        }
        //设置虚拟按钮大小调整设置
        if(this.viewGamePadUtil!=null) {
            //设置buttonSize窗口大小
            ViewGroup.LayoutParams layoutParams = this.viewGamePadUtil.getLayoutParams();
            layoutParams.width = (int) (getWidth() * 0.75f);
            layoutParams.height = LayoutParams.WRAP_CONTENT;
            this.viewGamePadUtil.setLayoutParams(layoutParams);
        }
    }

    private void initData(){
        this.gamepadMap1 = new HashMap<>();
        this.preferencesData = PreferencesData.getInstance(context);
        this.gameAllButton = new ArrayList<>();
        this.gamePadList = new ArrayList<>();
        this.audioSwitch = true;

    }

    private void initGamePad(){
        List<GamePad> gamePads1 = preferencesData.getGamePadList1(context);
        for (GamePad gamePad:gamePads1){
            if(gamePad.getKeyMapCode() == -1){
                continue;
            }
            this.gamepadMap1.put(gamePad.getKeyMapCode(),gamePad.getKeyCode());
        }
    }

    private void initUIButton(RelativeLayout gamepadRelativeLayout){
        this.gamePadView = (GamePadView)gamepadRelativeLayout.findViewById(R.id.gamepad_view_joy);
        this.gamePadView.setOnJoystickMoveListener(new GamePadView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                boolean isDowm = true;
                int preKey = gamePadView.getPreUseKey();
                //如果按钮抬起就取消按下状态
                if(GamePadView.UPKEY_AND_CANCEL == direction){
                    onKeyCheck(preKey,!isDowm);
                    return;
                }
                //如果跟上次一样就不处理
                if(preKey == direction){
                    return;
                }
                //取消上次点击
                onKeyCheck(preKey,!isDowm);
                //重新点击新的
                onKeyCheck(direction,isDowm);
            }
        });

        gamePadList.add(gamePadView);
        {
            gamepad_relative_linear_startselect = gamepadRelativeLayout.findViewById(R.id.gamepad_relative_linear_startselect);
            RelativeLayout gamepad_relative_4btn = gamepadRelativeLayout.findViewById(R.id.gamepad_relative_4btn);
            gamePadList.add(gamepad_relative_4btn);
            gameAllButton.add(gamepad_relative_4btn);
            //FrameLayout gamepad_relative_gamepadview = gamepadRelativeLayout.findViewById(R.id.gamepad_relative_gamepadview);
            //gamePadList.add(gamepad_relative_gamepadview);
            gameAllButton.add(this.gamePadView);

            Button gamepad_btn_orien = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_orien);
            Button gamepad_btn_start = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_start);
            Button gamepad_btn_select = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_select);
            Button gamepad_btn_a = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_a);
            Button gamepad_btn_b = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_b);
            Button gamepad_btn_l = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_l);
            Button gamepad_btn_r = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_r);
            Button gamepad_btn_menu = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_menu);
            Button gamepad_btn_read = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_read);
            Button gamepad_btn_screenshot = (Button)gamepadRelativeLayout.findViewById(R.id.gamepad_btn_screenshot);
            Button gamepad_btn_save = (Button)gamepadRelativeLayout.findViewById(R.id.gamepad_btn_save);
            final ToggleButton gamepad_togbtn_speed = (ToggleButton)gamepadRelativeLayout.findViewById(R.id.gamepad_togbtn_speed);
            final ToggleButton gamepad_togbtn_rewind = (ToggleButton)gamepadRelativeLayout.findViewById(R.id.gamepad_togbtn_rewind);

            gameAllButton.add(gamepad_togbtn_rewind);
            gameAllButton.add(gamepad_btn_orien);
            gameAllButton.add(gamepad_btn_start);
            gameAllButton.add(gamepad_btn_select);
            gameAllButton.add(gamepad_btn_read);
            gameAllButton.add(gamepad_btn_screenshot);
            gameAllButton.add(gamepad_btn_save);
            gameAllButton.add(gamepad_togbtn_speed);

            gamePadList.add(gamepad_btn_a);
            gamePadList.add(gamepad_btn_b);
            gamePadList.add(gamepad_btn_l);
            gamePadList.add(gamepad_btn_r);
            gamePadList.add(gamepad_btn_start);
            gamePadList.add(gamepad_btn_select);

            //gamepad_btn_start.getBackground().setAlpha(200);
            //gamepad_btn_select.getBackground().setAlpha(200);
            gamepad_btn_a.getBackground().setAlpha(150);
            gamepad_btn_b.getBackground().setAlpha(150);
            gamepad_btn_l.getBackground().setAlpha(150);
            gamepad_btn_r.getBackground().setAlpha(150);

            gamepad_btn_orien.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    FrameLayout frameLayout = SDLActivity.getContentView();
                    ViewGroup.LayoutParams lp = frameLayout.getLayoutParams();
                    if(event.getAction() == MotionEvent.ACTION_UP){
                        SCREEN_MODE +=1;
                        //0.66666666
                        float widthPixels = getWidth();
                        float heightPixels = getHeight();
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)gamepad_relative_linear_startselect.getLayoutParams();
                        if(SCREEN_MODE == 3){
                            //height=720 width=1280
                            int height = (int)(heightPixels*0.66666f + 0.5f);
                            lp.width = (int) heightPixels;
                            lp.height = height;
                            SDLActivity.onScreenSize(false,lp.width,lp.height);
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,0);
                            params.addRule(RelativeLayout.ALIGN_PARENT_TOP,1);
                            SDLActivity.getmSingleton().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        }else{
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,1);
                            params.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
                            if(SCREEN_MODE == 1){
                                int width = (int)((widthPixels*0.6)+widthPixels+ 0.5f);
                                if(width > heightPixels){
                                    width = (int) heightPixels;
                                }
                                lp.width = width;
                                lp.height = (int) widthPixels;
                            }else if(SCREEN_MODE == 2) {
                                lp.width = (int)widthPixels;
                                lp.height = (int)heightPixels;
                            }
                            SDLActivity.onScreenSize(false,lp.width,lp.height);
                            SDLActivity.getmSingleton().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        }
                        SDLActivity.getContentView().setLayoutParams(lp);
                        if(SCREEN_MODE>=3){
                            SCREEN_MODE = 0;
                        }
                    }
                    return false;
                }
            });

            gamepad_btn_read.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_UP){
                        String slotPath = Utils.getSlotPath(SDLActivity.getmSingleton().getGamePath(),0);
                        if(!new File(slotPath).exists()){
                            Toast.makeText(context,context.getString(R.string.load_slot_file_not_find),Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        LoadSlotDialog loadSlotDialog = new LoadSlotDialog(SDLActivity.getmSingleton(),slotPath);
                        loadSlotDialog.show();

                    }
                    return true;
                }
            });

            gamepad_togbtn_rewind.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    Intent intent = new Intent(context,GamePadActivity.class);
                    context.startActivity(intent);
//                    if(gamepad_togbtn_speed.isChecked()){
//                        Toast.makeText(context,context.getString(R.string.speed_runing_stop),Toast.LENGTH_SHORT).show();
//                        compoundButton.setChecked(false);
//                        return;
//                    }
//                    if(b){
//                        SDLActivity.onSlotNum(8,true);
//                        gamepad_togbtn_rewind.setBackgroundResource(R.mipmap.reback_press);
//                        SDLActivity.onDataKey(PAD1_REWIND,true);
//                    }else{
//                        gamepad_togbtn_rewind.setBackgroundResource(R.mipmap.reback_normal);
//                        SDLActivity.onDataKey(PAD1_REWIND,false);
//                    }
                }
            });
            //暂时用于打开其他试用功能
            gamepad_btn_menu.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_UP){
                        int width = getWidth();
                        int height = getHeight();
                        final SettingDialog setDialog = new SettingDialog(context,SDLActivity.getmSingleton().getGamePath());
                        setDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                audioSwitch = setDialog.getAudioSwitch();
                                if(setDialog.getBackCode() == 1){
                                    setShowGamePadUtilsButtonSize();
                                }
                                virtualButtonControl();
                            }
                        });
                        setDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                setDialog.setAudioSwitch(audioSwitch);
                            }
                        });
                        int requestedOrientation = SDLActivity.mSingleton.getRequestedOrientation();
                        if(requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
                            setDialog.show((int) (width*0.9),(int)(height *0.60f));
                        }else{
                            setDialog.show((int) (width*0.75),(int)(height *0.75f));
                        }
                    }
                    return false;
                }
            });

            gamepad_btn_r.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            view.getBackground().setAlpha(200);
                            SDLActivity.onDataKey(PAD1_S,true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            view.getBackground().setAlpha(150);
                            SDLActivity.onDataKey(PAD1_S,false);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                    }
                    return true;
                }
            });

            gamepad_btn_l.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            view.getBackground().setAlpha(200);
                            SDLActivity.onDataKey(PAD1_L,true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            view.getBackground().setAlpha(150);
                            SDLActivity.onDataKey(PAD1_L,false);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                    }
                    return true;
                }
            });

            gamepad_btn_start.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            view.getBackground().setAlpha(200);
                            SDLActivity.onDataKey(PAD1_START,true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            view.getBackground().setAlpha(150);
                            SDLActivity.onDataKey(PAD1_START,false);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                    }
                    return true;
                }
            });

            gamepad_btn_select.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            view.getBackground().setAlpha(200);
                            SDLActivity.onDataKey(PAD1_SELECT,true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            view.getBackground().setAlpha(150);
                            SDLActivity.onDataKey(PAD1_SELECT,false);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                    }
                    return true;
                }
            });

            gamepad_btn_a.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            view.getBackground().setAlpha(200);
                            SDLActivity.onDataKey(PAD1_A,true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            view.getBackground().setAlpha(150);
                            SDLActivity.onDataKey(PAD1_A,false);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                    }
                    return true;
                }
            });

            gamepad_btn_b.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            view.getBackground().setAlpha(200);
                            SDLActivity.onDataKey(PAD1_B,true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            view.getBackground().setAlpha(150);
                            SDLActivity.onDataKey(PAD1_B,false);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                    }
                    return true;
                }
            });

            gamepad_togbtn_speed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    if(gamepad_togbtn_rewind.isChecked()){
//                        Toast.makeText(context,context.getString(R.string.rewind_runing_stop),Toast.LENGTH_SHORT).show();
//                        compoundButton.setChecked(false);
//                        return;
//                    }
                    if(b){
                        gamepad_togbtn_speed.setBackgroundResource(R.mipmap.speed_press);
                        SDLActivity.onDataKey(PAD1_SPEED,true);
                    }else{
                        gamepad_togbtn_speed.setBackgroundResource(R.mipmap.speed_normal);
                        SDLActivity.onDataKey(PAD1_SPEED,false);
                    }
                }
            });

            gamepad_btn_save.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_UP){
                        //Bitmap bitmap = Utils.captureView(SDLActivity.getContentView());
                        SaveSlotDialog saveSlotDialog = new SaveSlotDialog(SDLActivity.getmSingleton());
                       // saveSlotDialog.setIconBitmap(bitmap);
                        saveSlotDialog.show();
                    }
                    return true;
                }
            });

            gamepad_btn_screenshot.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            SDLActivity.onDataKey(PAD1_CAPTURE,true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            SDLActivity.onDataKey(PAD1_CAPTURE,false);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                    }
                    return true;
                }
            });
        }
    }

    private void initUISetButtonSize(){
        game_relative_util_linear_buttonsize = (LinearLayout)viewGamePadUtil.findViewById(R.id.game_relative_util_linear_buttonsize);
        game_relative_util_linear_seeksize = (SeekBar) viewGamePadUtil.findViewById(R.id.game_relative_util_linear_seeksize);
        final TextView game_relative_util_linear_textsize = (TextView)viewGamePadUtil.findViewById(R.id.game_relative_util_linear_textsize);
        game_relative_util_linear_textsize.setText(String.format(context.getString(R.string.btn_size),0)+"%");
        game_relative_util_linear_seeksize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float pro = progress - 500;
                if(pro < 0){
                    pro = (pro/500.0f) * 100;
                }
                for (View view : gamePadList){
                    ViewSize viewSize = (ViewSize)view.getTag();
                    if(viewSize == null){
                        continue;
                    }
                    ViewGroup.LayoutParams params = view.getLayoutParams();
                    if(pro<0){
                        float scale = pro/100 + 1;
                        params.height =(int) (viewSize.getHeight()* scale);
                        params.width =(int) (viewSize.getWidth()* scale);
                    }else if(pro>0){
                        float scale = pro/100 + 1;
                        params.height =(int) (viewSize.getHeight() * scale);
                        params.width =(int) (viewSize.getWidth() * scale);
                    }else{
                        params.height =(int) (viewSize.getHeight());
                        params.width =(int) (viewSize.getWidth());

                        view.setScaleX(1);
                        view.setScaleY(1);
                    }
                    view.setLayoutParams(params);
                }
                preferencesData.setVirtualButtonSize(progress);
                game_relative_util_linear_textsize.setText(String.format(context.getString(R.string.btn_size),(int)pro)+"%");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        Button game_relative_util_linear_btnok = (Button)findViewById(R.id.game_relative_util_linear_btnok);
        game_relative_util_linear_btnok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setGamePadUtilsVisible(false);
            }
        });
        Button game_relative_util_linear_btnreset = (Button)findViewById(R.id.game_relative_util_linear_btnreset);
        game_relative_util_linear_btnreset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                game_relative_util_linear_seeksize.setProgress(500);
            }
        });
    }

    private void onKeyCheck(int orition,boolean isdown){

        switch (orition) {
            case GamePadView.FRONT:
                SDLActivity.onDataKey(PAD1_UP,isdown);
                gamePadView.setPreUseKey(GamePadView.FRONT);
                break;
            case GamePadView.FRONT_RIGHT:
                SDLActivity.onDataKey(PAD1_UP,isdown);
                SDLActivity.onDataKey(PAD1_RIGHT,isdown);
                gamePadView.setPreUseKey(GamePadView.FRONT_RIGHT);
                break;
            case GamePadView.RIGHT:
                SDLActivity.onDataKey(PAD1_RIGHT,isdown);
                gamePadView.setPreUseKey(GamePadView.RIGHT);
                break;
            case GamePadView.RIGHT_BOTTOM:
                SDLActivity.onDataKey(PAD1_RIGHT,isdown);
                SDLActivity.onDataKey(PAD1_DOWN,isdown);
                gamePadView.setPreUseKey(GamePadView.RIGHT_BOTTOM);
                break;
            case GamePadView.BOTTOM:
                SDLActivity.onDataKey(PAD1_DOWN,isdown);
                gamePadView.setPreUseKey(GamePadView.BOTTOM);
                break;
            case GamePadView.BOTTOM_LEFT:
                SDLActivity.onDataKey(PAD1_DOWN,isdown);
                SDLActivity.onDataKey(PAD1_LEFT,isdown);
                gamePadView.setPreUseKey(GamePadView.BOTTOM_LEFT);
                break;
            case GamePadView.LEFT:
                SDLActivity.onDataKey(PAD1_LEFT,isdown);
                gamePadView.setPreUseKey(GamePadView.LEFT);
                break;
            case GamePadView.LEFT_FRONT:
                SDLActivity.onDataKey(PAD1_LEFT,isdown);
                SDLActivity.onDataKey(PAD1_UP,isdown);
                gamePadView.setPreUseKey(GamePadView.LEFT_FRONT);
                break;
        }
        //用户取消点击
        if(!isdown){
            gamePadView.setPreUseKey(GamePadView.UPKEY_AND_CANCEL);
            gamePadView.setGamePadOriBG(GamePadView.UPKEY_AND_CANCEL);
        }else{
            gamePadView.setGamePadOriBG(orition);
        }
    }

    //重置数据
    public void onResume(){
        initGamePad();
    }

    private void virtualButtonControl(){
        if(preferencesData != null){
            GlobalConfig.VIRTUAL_BUTTON_CONTROL = preferencesData.getVirtualButtonControl();
        }

        if(gameAllButton!=null){
            for (View view:gameAllButton){
                if(GlobalConfig.VIRTUAL_BUTTON_CONTROL){
                    view.setVisibility(VISIBLE);
                }else{
                    view.setVisibility(INVISIBLE);
                }
            }
        }
    }
    private void setGamePadUtilsVisible(boolean visible){
        if(visible){
            viewGamePadUtil.setVisibility(VISIBLE);
        }else{
            viewGamePadUtil.setVisibility(GONE);
            game_relative_util_linear_buttonsize.setVisibility(GONE);
        }
    }

    private void setShowGamePadUtilsButtonSize(){
        setGamePadUtilsVisible(true);
        game_relative_util_linear_buttonsize.setVisibility(VISIBLE);
    }

    public boolean onKey( int keyCode, KeyEvent event){
        if(!gamepadMap1.containsKey(keyCode)){
            return false;
        }

        int gameKeyCode = gamepadMap1.get(keyCode);
        if(event.getAction() == KeyEvent.ACTION_UP){
            if(gameKeyCode == GamePadRelativeLayout.PAD1_SAVE_SLOT1){
                SDLActivity.onSlotNum(0,true);
                Toast.makeText(SDLActivity.getmSingleton(), String.format(SDLActivity.getmSingleton().getString(R.string.slote_save_desc),1),Toast.LENGTH_SHORT).show();
            }
            else if(gameKeyCode == GamePadRelativeLayout.PAD1_LOAD_SLOT1)
            {
                SDLActivity.onSlotNum(0,false);
                Toast.makeText(SDLActivity.getmSingleton(), String.format(SDLActivity.getmSingleton().getString(R.string.slote_load_desc),1),Toast.LENGTH_SHORT).show();
            }
            else
            {
                SDLActivity.onDataKey(gameKeyCode,false);
            }
            return true;
        }else if(event.getAction() == KeyEvent.ACTION_DOWN){
                SDLActivity.onDataKey(gameKeyCode,true);
            return true;
        }
        return false;
    }

}
