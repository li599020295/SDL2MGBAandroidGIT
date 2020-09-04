package lilinhong.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import org.libsdl.app.R;
import org.libsdl.app.SDLActivity;

public class SaveSlotDialog extends Dialog {
    private Context sdlActivity = null;
    private Bitmap iconBitmap = null;
    private ImageView save_slot_dialog_imageview = null;
    public SaveSlotDialog(SDLActivity sdlActivity) {
        super(sdlActivity, R.style.mdialog);
        this.sdlActivity =sdlActivity;
        initUI();
    }
    private void initUI() {
        setContentView(R.layout.save_slot_dialog);
        save_slot_dialog_imageview = (ImageView)findViewById(R.id.save_slot_dialog_imageview);
        Button save_slot_loaddata = (Button)findViewById(R.id.save_slot_loaddata);
        Button save_slot_cancel = (Button)findViewById(R.id.save_slot_cancel);
        save_slot_loaddata.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == KeyEvent.ACTION_UP){
                    SDLActivity.onSlotNum(0,true);
                    Toast.makeText(sdlActivity, String.format(sdlActivity.getString(R.string.slote_save_desc),1),Toast.LENGTH_SHORT).show();
                    dismiss();
                    return true;
                }
                return true;
            }
        });
        save_slot_cancel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == KeyEvent.ACTION_UP){
                    dismiss();
                    return true;
                }
                return true;
            }
        });
    }

    public void setIconBitmap(Bitmap iconBitmap) {
        this.iconBitmap = iconBitmap;
        if(save_slot_dialog_imageview!=null){
            save_slot_dialog_imageview.setImageBitmap(iconBitmap);
        }
    }
}
