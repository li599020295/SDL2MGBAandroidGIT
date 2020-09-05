package lilinhong.dialog;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.libsdl.app.R;
import java.io.File;
import lilinhong.model.GameRom;
import lilinhong.utils.GlobalConfig;
import lilinhong.utils.PreferencesData;

public class ShowScreenCaptureDialog extends Dialog {
    private TextView screen_capture_text_path = null;
    private ImageView screen_capture_imageview = null;
    private Context context = null;
    private String path = null;
    private GameRom gameRom = null;
    private PreferencesData preferencesData = null;
    public ShowScreenCaptureDialog(Context context) {
        super(context, R.style.mdialog);
        this.context = context;
        this.init();
    }
    private void init(){
        preferencesData = PreferencesData.getInstance(context);
        setContentView(R.layout.screen_capture_dialog);
        screen_capture_imageview = (ImageView)findViewById(R.id.screen_capture_imageview);
        Button screen_capture_btn_close = (Button)findViewById(R.id.screen_capture_btn_close);
        screen_capture_btn_close.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    dismiss();
                }
                return true;
            }
        });
        Button screen_capture_btn_delete = (Button)findViewById(R.id.screen_capture_btn_delete);
        screen_capture_btn_delete.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    File file = new File(path);
                    if(file.exists()){
                        file.delete();
                        Toast.makeText(context,context.getString(R.string.screen_capture_delet), Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }
                return true;
            }
        });
        Button screen_capture_dialog_gamecover = (Button)findViewById(R.id.screen_capture_dialog_gamecover);
        screen_capture_dialog_gamecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameRom.setImage(path);
                preferencesData.setSaveRomData(gameRom);
                GlobalConfig.GAME_COVER = path;
                Toast.makeText(context,context.getString(R.string.successful),Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
        screen_capture_text_path = (TextView)findViewById(R.id.screen_capture_text_path);
    }
    public void setImage(String path, GameRom gameRom){
        this.path = path;
        this.gameRom = gameRom;
        this.screen_capture_text_path.setText(String.format(context.getString(R.string.screen_capture),path));
        this.screen_capture_imageview.setImageURI(Uri.fromFile(new File(path)));
    }
}
