package protectedwp.safespace;

import android.app.Activity;
import android.content.SharedPreferences;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.UserManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;

public class UltraProtectModeActivity extends Activity {

	private android.app.AlertDialog errorDialog;

	private boolean hasSeparatePassword() {
    DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
    ComponentName adminName = new ComponentName(this, MyDeviceAdminReceiver.class);
    
    boolean isSeparate = false;
    try {
        isSeparate = !dpm.isUsingUnifiedPassword(adminName);
    } catch (Throwable t) {
        isSeparate = false;
    }

    if (!isSeparate) {
        if (errorDialog == null) {
            errorDialog = new android.app.AlertDialog.Builder(this)
                .setMessage("Option not available: Firstly set separate password for work profile.")
                .setCancelable(false)
                .setPositiveButton("CLOSE", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .create();
			android.view.Window window = errorDialog.getWindow();
            if (window != null) {
                android.view.WindowManager.LayoutParams lp = window.getAttributes();
                lp.gravity = android.view.Gravity.CENTER;
                lp.y = 0;
                window.setAttributes(lp);
            }
        }
        if (!errorDialog.isShowing()) {
            errorDialog.show();
        }
    }
		return isSeparate;
	}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		if (!hasSeparatePassword()) return;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(64, 64, 64, 64);
        layout.setGravity(android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL);

        Switch securitySwitch = new Switch(this);
        securitySwitch.setText("Enable UltraProtectMode? If you enable this, limit of failed password attempts after screen off or reboot will be set to 1. And if you or hacker open work profile bypassing this app and enter more than 4 symbols of incorrect password, data wil be wiped. Only if you firstly enter ShowApps password, limit will be 3. After disabling this option if now enabled, limit will be 3 too. To change it, open SecuritySettings. Warning: if you pause work profile, after enabling it again, system may ask to unlock it bypassing this app — and if this option enabled and activated (after reboot or screen off), you have only 1 attempt!");
        securitySwitch.setTextSize(16);

        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int topMargin = (int) (screenHeight * 0.20);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = topMargin;
        securitySwitch.setLayoutParams(params);
        
        layout.addView(securitySwitch);
        setContentView(layout);

        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminName = new ComponentName(this, MyDeviceAdminReceiver.class);
        UserManager um = (UserManager) getSystemService(Context.USER_SERVICE);
        
		final SharedPreferences prefsDH = createDeviceProtectedStorageContext().getSharedPreferences("UPM", MODE_PRIVATE);
        
        securitySwitch.setChecked(prefsDH.getBoolean("UPM", false));

        securitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsDH.edit().putBoolean("UPM", isChecked).commit();
            dpm.setMaximumFailedPasswordsForWipe(adminName, 3);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

	    @Override
    protected void onDestroy() {
        if (errorDialog != null) {
            if (errorDialog.isShowing()) {
                errorDialog.dismiss();
            }
            errorDialog = null;
        }
        super.onDestroy();
    }
}
