package lilinhong.dialog;

import android.content.Context;
import android.view.KeyEvent;

import org.libsdl.app.R;

public class MapKeyCodeDialog extends BaseDialog {
    private int keyCode = -1;
    private int mapKeyCode = -1;
    public MapKeyCodeDialog(Context context,int keyCode,int mapKeyCode) {
        super(context, R.style.mdialog);
        this.keyCode = keyCode;
        this.mapKeyCode = mapKeyCode;
        initData();
    }

    private void initData() {
        setContentView(R.layout.map_keycode_dialog);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        setMapKeyCode(keyCode);
        dismiss();
        return true;
    }

    public void setMapKeyCode(int mapKeyCode) {
        this.mapKeyCode = mapKeyCode;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public int getMapKeyCode() {
        return mapKeyCode;
    }
}
