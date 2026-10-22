package com.example.bluedeskpet;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

public class PetService extends Service {
    private WindowManager wm;
    private FrameLayout bubble;
    private WindowManager.LayoutParams params;
    private ImageView pet;
    private TextView speech;
    private Handler handler = new Handler();
    private int tapCount = 0;
    private float downX, downY;
    private int startX, startY;
    private boolean removed = false;

    @Override public void onCreate() {
        super.onCreate();
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE;
        params = new WindowManager.LayoutParams(
                230, 280, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 30; params.y = 180;

        bubble = new FrameLayout(this);
        pet = new ImageView(this);
        pet.setImageResource(R.drawable.pet);
        pet.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(-1, -1);
        bubble.addView(pet, pp);

        speech = new TextView(this);
        speech.setText("摸摸头？");
        speech.setTextColor(Color.rgb(55,105,175));
        speech.setTextSize(12); speech.setTypeface(null, Typeface.BOLD);
        speech.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.argb(235,255,255,255)); bg.setCornerRadius(28);
        bg.setStroke(2, Color.rgb(151,210,255)); speech.setBackground(bg);
        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(115, 38, Gravity.TOP | Gravity.RIGHT);
        bubble.addView(speech, sp);

        bubble.setOnTouchListener((v,e) -> {
            switch(e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX=e.getRawX(); downY=e.getRawY(); startX=params.x; startY=params.y;
                    return true;
                case MotionEvent.ACTION_UP:
                    float dx=e.getRawX()-downX, dy=e.getRawY()-downY;
                    if (Math.abs(dx)<12 && Math.abs(dy)<12) {
                        tapCount++;
                        String[] lines={"嘿嘿～","摸头成功！","今天也要开心呀！","我会陪着你～"};
                        speech.setText(lines[tapCount % lines.length]);
                        pet.animate().scaleX(1.08f).scaleY(0.94f).setDuration(130)
                           .withEndAction(() -> pet.animate().scaleX(1f).scaleY(1f).setDuration(180).start()).start();
                    } else {
                        params.x = Math.max(0, startX+(int)dx);
                        params.y = Math.max(0, startY+(int)dy);
                        try { wm.updateViewLayout(bubble, params); } catch(Exception ignored) {}
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    params.x = Math.max(0, startX+(int)(e.getRawX()-downX));
                    params.y = Math.max(0, startY+(int)(e.getRawY()-downY));
                    try { wm.updateViewLayout(bubble, params); } catch(Exception ignored) {}
                    return true;
            }
            return false;
        });
        try { wm.addView(bubble, params); } catch(Exception e) { stopSelf(); return; }
        // Gentle floating/bobbing animation.
        ObjectAnimator bob = ObjectAnimator.ofFloat(pet, "translationY", 0f, -10f, 0f);
        bob.setDuration(1800); bob.setRepeatCount(ValueAnimator.INFINITE);
        bob.setInterpolator(new LinearInterpolator()); bob.start();
        // Blink-like brief scale pulse every few seconds.
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (removed || pet == null) return;
                pet.animate().scaleY(0.985f).setDuration(90).withEndAction(
                    () -> pet.animate().scaleY(1f).setDuration(110).start()).start();
                handler.postDelayed(this, 4200);
            }
        }, 4200);
    }
    @Override public int onStartCommand(Intent i, int flags, int id) { return START_STICKY; }
    @Override public void onDestroy() {
        removed=true; handler.removeCallbacksAndMessages(null);
        if (bubble != null) try { wm.removeView(bubble); } catch(Exception ignored) {}
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent i) { return null; }
}
