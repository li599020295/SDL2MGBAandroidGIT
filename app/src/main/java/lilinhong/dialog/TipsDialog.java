package lilinhong.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.libsdl.app.R;

public class TipsDialog extends Dialog {
    private TextView tips_dialog_text_title = null;
    private TextView tips_dialog_text_desc = null;
    private Button tips_dialog_button_ok1 = null;
    private Button tips_dialog_button_ok2 = null;
    private Button tips_dialog_button_ok3 = null;
    private View tips_dialog_view_ok1 = null;
    private View tips_dialog_view_ok2 = null;
    private Context context = null;
    public TipsDialog(Context context) {
        super(context, R.style.mdialog);
        this.context = context;
        this.initData();
    }
    private void initData(){
        setContentView(R.layout.tips_dialog);
        tips_dialog_text_title = (TextView)findViewById(R.id.tips_dialog_text_title);
        tips_dialog_text_desc = (TextView)findViewById(R.id.tips_dialog_text_desc);
        tips_dialog_button_ok1 = (Button)findViewById(R.id.tips_dialog_button_ok1);
        tips_dialog_button_ok2 = (Button)findViewById(R.id.tips_dialog_button_ok2);
        tips_dialog_button_ok3 = (Button)findViewById(R.id.tips_dialog_button_ok3);
        tips_dialog_view_ok1 = (View)findViewById(R.id.tips_dialog_view_ok1);
        tips_dialog_view_ok2 = (View)findViewById(R.id.tips_dialog_view_ok2);
        tips_dialog_view_ok1.setVisibility(View.GONE);
        tips_dialog_view_ok2.setVisibility(View.GONE);
        tips_dialog_button_ok1.setVisibility(View.GONE);
        tips_dialog_button_ok2.setVisibility(View.GONE);
        tips_dialog_button_ok3.setVisibility(View.GONE);
    }

    public void setData(String title, String desc ){
        tips_dialog_text_title.setText(title);
        tips_dialog_text_desc.setText(desc);
    }
    public TipsDialog setOnButton1Listener(String button1, View.OnClickListener listener){
        tips_dialog_button_ok1.setText(button1);
        tips_dialog_button_ok1.setVisibility(View.VISIBLE);
        tips_dialog_button_ok1.setOnClickListener(listener);
        return this;
    }
    public TipsDialog setOnButton2Listener(String button1, View.OnClickListener listener){
        tips_dialog_button_ok2.setText(button1);
        tips_dialog_button_ok2.setVisibility(View.VISIBLE);
        tips_dialog_button_ok2.setOnClickListener(listener);
        tips_dialog_view_ok1.setVisibility(View.VISIBLE);
        return this;
    }
    public TipsDialog setOnButton3Listener(String button1, View.OnClickListener listener){
        tips_dialog_button_ok3.setText(button1);
        tips_dialog_button_ok3.setVisibility(View.VISIBLE);
        tips_dialog_button_ok3.setOnClickListener(listener);
        tips_dialog_view_ok2.setVisibility(View.VISIBLE);
        return this;
    }
}
