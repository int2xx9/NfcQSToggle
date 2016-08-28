package net.int512.nfcqstoggle;

import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView systemAppStatus;
    private TextView systemAppHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        systemAppStatus = (TextView)findViewById(R.id.systemAppStatus);
        systemAppHelp = (TextView)findViewById(R.id.systemAppHelp);
        if (isSystemApp()) {
            systemAppStatus.setText("Yes");
            systemAppStatus.setTextColor(Color.GREEN);
        } else {
            systemAppStatus.setText("No");
            systemAppStatus.setTextColor(Color.RED);
            systemAppHelp.setVisibility(View.VISIBLE);
        }
    }

    public boolean isSystemApp() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}
