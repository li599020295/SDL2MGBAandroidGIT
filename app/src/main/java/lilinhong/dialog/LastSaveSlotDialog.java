package lilinhong.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.libsdl.app.R;
import org.libsdl.app.SDLActivity;

import java.io.File;

import lilinhong.utils.Utils;

public class LastSaveSlotDialog extends BaseDialog {
    private Bitmap bitmap = null;
    private int slot = -1;
    private String slotPath = "";
    public LastSaveSlotDialog(Context context,String slotPath ,int slot) {
        super(context, R.style.mdialog);
        this.slot = slot;
        this.slotPath = slotPath;
        initUI();
    }
    private void initUI(){
        setContentView(R.layout.last_save_slot_dialog);
        bitmap = Utils.getLoacalBitmap(slotPath);
        ImageView last_save_slot_imageview = findViewById(R.id.last_save_slot_imageview);
        Button last_save_slot_btn_load = findViewById(R.id.last_save_slot_btn_load);
        Button last_save_slot_btn_cancel = findViewById(R.id.last_save_slot_btn_cancel);

        last_save_slot_imageview.setImageBitmap(bitmap);
        last_save_slot_btn_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SDLActivity.onSlotNum(slot,false);
                File file =new File(slotPath);
                if(file.exists()){
                    file.delete();
                }
                dismiss();
            }
        });
        last_save_slot_btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file =new File(slotPath);
                if(file.exists()){
                    file.delete();
                }
                dismiss();
            }
        });
        setCancelable(false);
    }
    public void onStop(){
        super.onStop();
        if(bitmap!=null){
            bitmap.recycle();
            bitmap = null;
        }
    }
}
