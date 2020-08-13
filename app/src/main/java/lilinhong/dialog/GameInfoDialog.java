package lilinhong.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.libsdl.app.R;

import java.io.File;
import java.util.List;

import lilinhong.model.GameRom;
import lilinhong.utils.Utils;

public class GameInfoDialog extends Dialog {
    private Context context = null;
    private boolean isRefresh = false;
    private GameRom gameRom = null;
    public GameInfoDialog(Context context, GameRom gameRom) {
        super(context, R.style.mdialog);
        this.context =context;
        this.gameRom = gameRom;
        initUI();
    }

    private void initUI() {
        setContentView(R.layout.game_info_dialog);
        TextView game_info_text_title = findViewById(R.id.game_info_text_title);
        ImageButton game_info_imgbtn_close = findViewById(R.id.game_info_imgbtn_close);
        ImageView game_info_image_cover = findViewById(R.id.game_info_image_cover);
        TextView game_info_text_filepath = findViewById(R.id.game_info_text_filepath);
        TextView game_info_text_filesize = findViewById(R.id.game_info_text_filesize);
        TextView game_info_text_saveslot = findViewById(R.id.game_info_text_saveslot);
        TextView game_info_text_lasttime = findViewById(R.id.game_info_text_lasttime);
        TextView game_info_text_palytime = findViewById(R.id.game_info_text_palytime);
        Button game_info_btn_delete = findViewById(R.id.game_info_btn_delete);

        File file = new File(gameRom.getPath());
        String fileSizeStr = Utils.getFileSizeForM(file.length());
        game_info_text_filesize.setText(fileSizeStr);

        long playTime = gameRom.getPlayTime();
        game_info_text_palytime.setText(playTime+" "+context.getString(R.string.hour));

        long lastTime = gameRom.getPreUseTime();
        if(lastTime == 0){
            game_info_text_lasttime.setText(context.getString(R.string.no));
        }else{
            game_info_text_lasttime.setText(Utils.getUSTime(lastTime));
        }

        game_info_text_title.setText(gameRom.getName());

        Bitmap bitmap = Utils.getLoacalBitmap(gameRom.getCover());
        if(bitmap == null){
            game_info_image_cover.setImageResource(R.mipmap.ic_launcher);
        }else{
            game_info_image_cover.setImageBitmap(bitmap);
        }

        game_info_text_filepath.setText(Utils.getPathDeduplication(context,gameRom.getPath()));

        game_info_imgbtn_close.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    dismiss();
                }
                return true;
            }
        });

        game_info_btn_delete.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    File file = new File(gameRom.getPath());
                    if(file.exists()){
                        file.delete();
                    }
                    isRefresh = true;
                }
                return true;
            }
        });

    }
    public boolean getIsRefresh(){
        return isRefresh;
    }
    public GameRom getGameRom(){
        return gameRom;
    }
}
