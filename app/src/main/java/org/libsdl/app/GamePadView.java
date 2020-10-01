package org.libsdl.app;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.appcompat.widget.AppCompatImageButton;

import lilinhong.utils.Utils;

public class GamePadView extends AppCompatImageButton {
    private int directionId = 0;
    private Bitmap[] direction = null;
    private Paint paint = null;
    //存储获取上次使用的位置
    private int preUseKey = -1;
    // Constants
    private final double RAD = 57.2957795;
    public final static int FRONT = 3;
    public final static int FRONT_RIGHT = 2;
    public final static int RIGHT = 1;
    public final static int RIGHT_BOTTOM = 8;
    public final static int BOTTOM = 7;
    public final static int BOTTOM_LEFT = 6;
    public final static int LEFT = 5;
    public final static int LEFT_FRONT = 4;
    public final static int UPKEY_AND_CANCEL = 0;

    // Variables
    private OnJoystickMoveListener onJoystickMoveListener; // Listener
    private int xPosition = 0; // Touch x position
    private int yPosition = 0; // Touch y position
    private double centerX = 0; // Center view x position
    private double centerY = 0; // Center view y position
    private int joystickRadius;
    private int lastAngle = 0;

    public GamePadView(Context context) {
        super(context);
        initJoystickView();
    }

    public GamePadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initJoystickView();
    }

    public GamePadView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        initJoystickView();
    }

    protected void initJoystickView() {
       // getBackground().setAlpha(150);
        setBackgroundColor(Utils.getColor(this.getContext(),R.color.trans));
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setScaleType(ScaleType.FIT_XY);
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        direction = new Bitmap[9];
        direction[0] = BitmapFactory.decodeResource(getResources(), R.mipmap.analog_0);
        direction[1] = BitmapFactory.decodeResource(getResources(), R.mipmap.analog_u);
        direction[2] = BitmapFactory.decodeResource(getResources(), R.mipmap.analog_d);
        direction[3] = BitmapFactory.decodeResource(getResources(), R.mipmap.analog_l);
        direction[4] = BitmapFactory.decodeResource(getResources(), R.mipmap.analog_r);
        direction[5] = BitmapFactory.decodeResource(getResources(), R.mipmap.analog_ul);
        direction[6] = BitmapFactory.decodeResource(getResources(), R.mipmap.analog_ur);
        direction[7] = BitmapFactory.decodeResource(getResources(), R.mipmap.analog_dr);
        direction[8] = BitmapFactory.decodeResource(getResources(), R.mipmap.analog_dl);
        setImageBitmap(direction[0]);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        // before measure, get the center of view
        xPosition = (int) getWidth() / 2;
        yPosition = (int) getWidth() / 2;
        int d = Math.min(xNew, yNew);
        joystickRadius = (int) (d / 2 );
        centerX = (getWidth()) / 2;
        centerY = (getHeight()) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawBitmap(direction[directionId], 0, 0, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(onJoystickMoveListener == null){
            return true;
        }

        xPosition = (int) event.getX();
        yPosition = (int) event.getY();
        double abs = Math.sqrt((xPosition - centerX) * (xPosition - centerX) + (yPosition - centerY) * (yPosition - centerY));

        if (abs > joystickRadius)
        {
            xPosition = (int) ((xPosition - centerX) * joystickRadius / abs + centerX);
            yPosition = (int) ((yPosition - centerY) * joystickRadius / abs + centerY);
        }

        int getAngle = getAngle();
        int direction = getDirection();
        directionId = direction;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //按钮按下
            onJoystickMoveListener.onValueChanged(getAngle, getPower(), direction);
        }else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
            //按钮取消获取抬起
            xPosition = (int) centerX;
            yPosition = (int) centerY;
            onJoystickMoveListener.onValueChanged(getAngle, getPower(), UPKEY_AND_CANCEL);
        }else{
            //触摸点移动
            onJoystickMoveListener.onValueChanged(getAngle, getPower(), direction);
        }
        //invalidate();
        //多点触控
//        if(event.getAction() == MotionEvent.ACTION_MASK){
//
//        }
        return true;
    }

    private int getAngle() {
        if (xPosition > centerX) {
            if (yPosition < centerY) {
                return lastAngle = (int) (Math.atan((yPosition - centerY) / (xPosition - centerX)) * RAD + 90);
            } else if (yPosition > centerY) {
                return lastAngle = (int) (Math.atan((yPosition - centerY) / (xPosition - centerX)) * RAD) + 90;
            } else {
                return lastAngle = 90;
            }
        } else if (xPosition < centerX) {
            if (yPosition < centerY) {
                return lastAngle = (int) (Math.atan((yPosition - centerY) / (xPosition - centerX)) * RAD - 90);
            } else if (yPosition > centerY) {
                return lastAngle = (int) (Math.atan((yPosition - centerY) / (xPosition - centerX)) * RAD) - 90;
            } else {
                return lastAngle = -90;
            }
        } else {
            if (yPosition <= centerY) {
                return lastAngle = 0;
            } else {
                if (lastAngle < 0) {
                    return lastAngle = -180;
                } else {
                    return lastAngle = 180;
                }
            }
        }
    }

    private int getPower() {
        return (int) (100 * Math.sqrt((xPosition - centerX)
                * (xPosition - centerX) + (yPosition - centerY)
                * (yPosition - centerY)) / joystickRadius);
    }

    private int getDirection() {
//        if (lastPower == 0 && lastAngle == 0) {
//            return 0;
//        }
        int a = 0;
        if (lastAngle <= 0) {
            a = (lastAngle * -1) + 90;
        } else if (lastAngle > 0) {
            if (lastAngle <= 90) {
                a = 90 - lastAngle;
            } else {
                a = 360 - (lastAngle - 90);
            }
        }

        int direction = (int) (((a + 22) / 45) + 1);

        if (direction > 8) {
            direction = 1;
        }

        return direction;
    }

    public void setOnJoystickMoveListener(OnJoystickMoveListener listener) {
        this.onJoystickMoveListener = listener;
    }

    public int getPreUseKey() {
        return preUseKey;
    }

    public void setPreUseKey(int preUseKey) {
        this.preUseKey = preUseKey;
    }

    public interface OnJoystickMoveListener {
        public void onValueChanged(int angle, int power, int direction);
    }

    //设置方向的背景图片
    public void setGamePadOriBG(int ori){
        if(ori == UPKEY_AND_CANCEL){
            setImageBitmap(direction[0]);
            return;
        }
        if(ori == FRONT){
            setImageBitmap(direction[1]);
        }else if(ori == BOTTOM){
            setImageBitmap(direction[2]);
        }else if(ori == LEFT){
            setImageBitmap(direction[3]);
        }else if(ori == RIGHT){
            setImageBitmap(direction[4]);
        }else if(ori == LEFT_FRONT){
            setImageBitmap(direction[5]);
        }else if(ori == FRONT_RIGHT){
            setImageBitmap(direction[6]);
        }else if(ori == BOTTOM_LEFT){
            setImageBitmap(direction[7]);
        }else if(ori == RIGHT_BOTTOM){
            setImageBitmap(direction[8]);
        }
    }
}
