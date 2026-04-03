package protectedwp.safespace;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.*;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;

public class HighEfficiencyModeActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().getDecorView().setKeepScreenOn(true);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        
        super.onCreate(savedInstanceState);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        int screenWidth = dm.widthPixels;

        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        int topMargin = (int) (screenHeight * 0.07);
        int sidePadding = (int) (16 * dm.density);
        layout.setPadding(sidePadding, topMargin, sidePadding, sidePadding);

        final Switch toggle = new Switch(this);
        
        toggle.setText("Enable high-efficiency mode? In this mode, the application will use alarms and receivers for constant restart and maintaining high process priority to make it harder for aggressive firmwares to kill it, and to ensure it restart after death. Also, the application will launch 10 short WakeLocks that will be constantly updated to prevent the phone from entering Doze Mode, which is necessary for the application's fast reaction to threats, as broadcasts for USB events may arrive slowly in Doze mode. Also, the application will, during the restart of the main service and not only in the screen-off receiver, check whether the screen is in an off or locked state while the profile is not encrypted. Simply put, checking whether the application missed the screen turning off past the receiver by failing to evict the keys, for example, because it was killed and then restarted. And if so, it evicts them at the moment of launch through the same DevicePolicyManager.lockNow(FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY) mechanism.\n\n" +
                "The state of the high-efficiency mode is saved in the application memory, but changes to this state occur only after its process is restarted. To restart the application process, it is recommended to simply restart the phone.\n\n" +
                "This mode is useful if you have a weak phone or a phone with aggressive power-saving mode and you are running some heavy game, or you just want the application to quickly respond to USB events even in Doze mode.\n\n"+
                "If you see a snowflake​ ❄ in the application notification, it is the normal mode. If you see a flame 🔥, it is the high-efficiency mode.\n\n" +
                "High-efficiency mode may consume more battery power.");

        if (HighEfficiencyModeActivity.this.createDeviceProtectedStorageContext().getSharedPreferences("prefs", Context.MODE_PRIVATE).getBoolean("isHighEfficiencyModeEnabled", false)) {
            toggle.setChecked(true);
        } else {
            toggle.setChecked(false);
        }

        toggle.setOnClickListener(v -> {
            boolean currentlyChecked = toggle.isChecked();
             
            if (!currentlyChecked) {
                toggle.setChecked(true);

                AlertDialog dialog = new AlertDialog.Builder(HighEfficiencyModeActivity.this)
                        .setTitle("Are You Sure?")
                        .setMessage("Want to disable?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                HighEfficiencyModeActivity.this.createDeviceProtectedStorageContext().getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putBoolean("isHighEfficiencyModeEnabled", false).apply();
                                toggle.setChecked(false);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();

                dialog.show();
                
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = (int) (screenWidth * 0.9);
                lp.gravity = Gravity.CENTER;
                lp.x = 0;
                lp.y = 0; 
                dialog.getWindow().setAttributes(lp);

            } else {
                HighEfficiencyModeActivity.this.createDeviceProtectedStorageContext().getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putBoolean("isHighEfficiencyModeEnabled", true).apply();
                background.work.around.Start.RunService(HighEfficiencyModeActivity.this);
                try {
                Intent intentRider = new Intent(HighEfficiencyModeActivity.this, background.work.around.RiderService.class);
                startForegroundService(intentRider);
                } catch (Throwable t) {}
            }
        });

        layout.addView(toggle);
        scrollView.addView(layout);
        setContentView(scrollView);
    }
}
