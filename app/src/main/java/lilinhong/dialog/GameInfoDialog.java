package lilinhong.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import org.libsdl.app.R;

public class GameInfoDialog extends Dialog {
    private Context context = null;
    private String path = "";
    public GameInfoDialog(Context context,String path) {
        super(context, R.style.mdialog);
        this.context =context;
        this.path = path;
        initUI();
    }

    private void initUI() {
        setContentView(R.layout.game_info_dialog);
        TextView game_info_text_title = findViewById(R.id.game_info_text_title);
        TextView game_info_text_title = findViewById(R.id.game_info_text_title);
        TextView game_info_text_title = findViewById(R.id.game_info_text_title);
        TextView game_info_text_title = findViewById(R.id.game_info_text_title);
        TextView game_info_text_title = findViewById(R.id.game_info_text_title);
        TextView game_info_text_title = findViewById(R.id.game_info_text_title);

    }
}
