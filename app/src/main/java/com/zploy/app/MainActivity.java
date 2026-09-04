package com.zploy.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class MainActivity extends Activity implements ShizukuBridge.Listener {
    private TextView status;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        ShizukuBridge.get().init(this);
        ShizukuBridge.get().addListener(this);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = dp(24); box.setPadding(p,p,p,p);
        status = new TextView(this); status.setTextSize(18); box.addView(status);

        Button shizuku = new Button(this); shizuku.setText("Povolit Shizuku");
        shizuku.setOnClickListener(v -> ShizukuBridge.get().requestPermission()); box.addView(shizuku);

        Button accessibility = new Button(this); accessibility.setText("Otevřít Zpřístupnění");
        accessibility.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))); box.addView(accessibility);

        TextView help = new TextView(this);
        help.setText("Zapni službu Pen → Touch ve Zpřístupnění. Potom se dotyk hrotu pera převádí 1:1 na dotyk prstu (DOWN / MOVE / UP). Shizuku musí běžet.");
        help.setTextSize(16); help.setPadding(0,dp(18),0,0); box.addView(help);
        setContentView(box); refresh();
    }

    @Override protected void onResume(){ super.onResume(); refresh(); }
    @Override protected void onDestroy(){ ShizukuBridge.get().removeListener(this); super.onDestroy(); }
    @Override public void onShizukuStateChanged(){ runOnUiThread(this::refresh); }

    private void refresh(){
        boolean alive=ShizukuBridge.get().isBinderAlive(), perm=ShizukuBridge.get().hasPermission(), ready=ShizukuBridge.get().isReady();
        status.setText("Pen → Touch\nShizuku: "+(alive?"běží":"neběží")+"\nOprávnění: "+(perm?"ano":"ne")+"\nInjector: "+(ready?"připraven":"čeká"));
        if(perm && !ready) ShizukuBridge.get().bindInputService();
    }
    private int dp(int v){ return Math.round(v*getResources().getDisplayMetrics().density); }
}
