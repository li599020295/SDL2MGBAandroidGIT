package lilinhong.dialog;

import android.app.Dialog;
import android.content.Context;
import org.libsdl.app.R;

public class BaseDialog extends Dialog {
    public BaseDialog(Context context, int mdialog) {
        super(context, R.style.mdialog);
    }

    public void show(int width,int height){
        this.show();
        this.getWindow().setLayout(width,height);
    }
}
