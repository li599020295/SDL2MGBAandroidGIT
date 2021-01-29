package lilinhong.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import lilinhong.utils.PreferencesData;

public class BootActivity extends AppCompatActivity {
    private PreferencesData preferencesData = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initData();
    }

    private void initData() {
        preferencesData = PreferencesData.getInstance(BootActivity.this);
        Intent intent = new Intent(BootActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
