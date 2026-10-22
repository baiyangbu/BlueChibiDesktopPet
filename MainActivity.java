package com.example.bluedeskpet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;

public class MainActivity extends Activity {
    private static final int OVERLAY_REQ = 101;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(28, 36, 28, 36);
        root.setBackgroundColor(Color.rgb(239,247,255));
        TextView title = new TextView(this);
        title.setText("蓝色桌宠");
        title.setTextSize(28); title.setTextColor(Color.rgb(48,105,180));
        title.setGravity(Gravity.CENTER);
        TextView desc = new TextView(this);
        desc.setText("让蓝发小伙伴陪你使用手机\n支持悬浮、轻轻走动、眨眼和摸头互动");
        desc.setTextSize(16); desc.setTextColor(Color.rgb(70,90,120));
        desc.setGravity(Gravity.CENTER); desc.setPadding(0,24,0,30);
        Button start = new Button(this); start.setText("开启桌面宠物");
        start.setOnClickListener(v -> startPet());
        Button stop = new Button(this); stop.setText("收起宠物");
        stop.setOnClickListener(v -> stopService(new Intent(this, PetService.class)));
        root.addView(title); root.addView(desc); root.addView(start); root.addView(stop);
        setContentView(root);
    }
    private void startPet() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(i, OVERLAY_REQ);
            return;
        }
        startService(new Intent(this, PetService.class));
        finish();
    }
    @Override protected void onActivityResult(int r, int c, Intent d) {
        super.onActivityResult(r,c,d);
        if (r == OVERLAY_REQ && (Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this))) startPet();
    }
}
