package lilinhong.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.libsdl.app.R;
import org.libsdl.app.SDLActivity;

import java.io.File;

import lilinhong.utils.Utils;

public class LoadSlotDialog extends Dialog {
    private Context sdlActivity = null;
    private String slotPath = null;
    public LoadSlotDialog(SDLActivity sdlActivity, String slotPath) {
        super(sdlActivity, R.style.mdialog);
        this.sdlActivity =sdlActivity;
        this.slotPath = slotPath;
        initUI();
    }
    private void initUI() {
        setContentView(R.layout.load_slot_dialog);
        ImageView load_slot_dialog_imageview = (ImageView)findViewById(R.id.load_slot_dialog_imageview);
        Button load_slot_loaddata = (Button)findViewById(R.id.load_slot_loaddata);
        Button load_slot_cancel = (Button)findViewById(R.id.load_slot_cancel);
        load_slot_loaddata.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == KeyEvent.ACTION_UP){
                    SDLActivity.onSlotNum(0,false);
                    Toast.makeText(sdlActivity, String.format(sdlActivity.getString(R.string.slote_load_desc),1),Toast.LENGTH_SHORT).show();
                    dismiss();
                    return true;
                }
                return true;
            }
        });
        load_slot_cancel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == KeyEvent.ACTION_UP){
                    dismiss();
                    return true;
                }
                return true;
            }
        });
        TextView load_slot_save_time = (TextView)findViewById(R.id.load_slot_save_time);
        String fileTime = Utils.getFileLastModifiedTime(new File(slotPath));
        load_slot_save_time.setText(fileTime);
        Bitmap bitmap = Utils.getLoacalBitmap(slotPath); //从本地取图片(在cdcard中获取)  //
        if(bitmap!=null){
            load_slot_dialog_imageview.setImageBitmap(bitmap); //设置Bitmap
        }
    }
}
