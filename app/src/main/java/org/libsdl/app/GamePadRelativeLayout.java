package org.libsdl.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

public class GamePadRelativeLayout extends RelativeLayout {
    public final static int SDLK_SCANCODE_MASK = (1<<30);
    //#define SDL_SCANCODE_TO_KEYCODE(X)  (X | SDLK_SCANCODE_MASK)
    //按钮设置
    //so玩家1对应按键位置
    public final static int PAD1_UP = (82| SDLK_SCANCODE_MASK);
    public final static int PAD1_DOWN = (81| SDLK_SCANCODE_MASK);
    public final static int PAD1_LEFT = (80| SDLK_SCANCODE_MASK);
    public final static int PAD1_RIGHT = (79| SDLK_SCANCODE_MASK);
    private static final int PAD1_START = (int)'\r';
    private static final int PAD1_SELECT = (int)'\b';
    private static final int PAD1_L = (int) 'a';
    private static final int PAD1_R = (int)'s';
    private static final int PAD1_A = (int)'x';
    private static final int PAD1_B = (int)'z';
    private static final int PAD1_SPEED = (int)'\t';
    //截图
    private static final int PAD1_CAPTURE =(69|SDLK_SCANCODE_MASK);


    private Context context;
    private GamePadView gamePadView = null;
    public GamePadRelativeLayout(Context context) {
        super(context);
        this.context = context;
        this.initData();
    }

    public GamePadRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.initData();
    }

    public GamePadRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.initData();
    }

//    public GamePadRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        this.context = context;
//        this.initData();
//    }

    private void initData(){
        RelativeLayout gamepadRelativeLayout = (RelativeLayout)LayoutInflater.from(this.context).inflate(R.layout.gamepad_relative_layout,null);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(gamepadRelativeLayout,params);

        gamePadView = (GamePadView)gamepadRelativeLayout.findViewById(R.id.gamepad_view_joy);
        gamePadView.setOnJoystickMoveListener(new GamePadView.OnJoystickMoveListener() {
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

        {
            Button gamepad_btn_start = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_start);
            Button gamepad_btn_select = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_select);
            Button gamepad_btn_a = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_a);
            Button gamepad_btn_b = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_b);
            Button gamepad_btn_l = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_l);
            Button gamepad_btn_r = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_r);
            Button gamepad_btn_menu = gamepadRelativeLayout.findViewById(R.id.gamepad_btn_menu);
            //暂时用于打开其他试用功能
            gamepad_btn_menu.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_UP){

                    }
                    return false;
                }
            });
            gamepad_btn_r.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            SDLActivity.onDataKey(PAD1_R,true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            SDLActivity.onDataKey(PAD1_R,false);
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
                            SDLActivity.onDataKey(PAD1_L,true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
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
                            SDLActivity.onDataKey(PAD1_START,true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
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
                            SDLActivity.onDataKey(PAD1_SELECT,true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
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
                            SDLActivity.onDataKey(PAD1_A,true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
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
                            SDLActivity.onDataKey(PAD1_B,true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                           SDLActivity.onDataKey(PAD1_B,false);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                    }
                    return true;
                }
            });
            final ToggleButton gamepad_togbtn_speed = (ToggleButton)gamepadRelativeLayout.findViewById(R.id.gamepad_togbtn_speed);
            gamepad_togbtn_speed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b){
                        gamepad_togbtn_speed.setBackgroundResource(R.mipmap.speed_press);
                        SDLActivity.onDataKey(PAD1_SPEED,true);
                    }else{
                        gamepad_togbtn_speed.setBackgroundResource(R.mipmap.speed_normal);
                        SDLActivity.onDataKey(PAD1_SPEED,false);
                    }
                }
            });
            Button gamepad_btn_save = (Button)gamepadRelativeLayout.findViewById(R.id.gamepad_btn_save);
            gamepad_btn_save.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_UP){
                        //SDLActivity.onSlotNum(0,true);
                        Toast.makeText(context, String.format(context.getString(R.string.slote_save_desc),1),Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            Button gamepad_btn_screenshot = (Button)gamepadRelativeLayout.findViewById(R.id.gamepad_btn_screenshot);
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
}
