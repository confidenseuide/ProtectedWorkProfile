package com.example.hider;

import android.app.admin.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import java.util.*;

public class MyDeviceAdminReceiver extends DeviceAdminReceiver {


	@Override
public void onReceive(final Context context, Intent intent) {
    if ("ACTION_REBIRTH_STAGE_2".equals(intent.getAction())) {
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                
                // Самый важный сон: ждем, пока все системные "залупные окна" 
                // от провижнинга окончательно схлопнутся.
                android.os.SystemClock.sleep(500); 

                Intent launch = new Intent(context, MainActivity.class);
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                              | Intent.FLAG_ACTIVITY_CLEAR_TASK 
                              | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                launch.putExtra("restarted", true);
                
                try {
                    context.startActivity(launch);
                } catch (Exception e) {
                    // Если даже эстафета не помогла, значит Knox держит процесс на мушке
                }
            }
        }).start();
    }
}

	
    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(context, MyDeviceAdminReceiver.class);    
  
        dpm.setProfileEnabled(admin);
        dpm.setProfileName(admin, "Ephemeral WP");
        dpm.enableSystemApp(admin, context.getPackageName());

		LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        List<UserHandle> profiles = userManager.getUserProfiles();

        for (UserHandle profile : profiles) {
    // Получаем ID профиля. 0 — это всегда основной владелец (Main User)
        long userId = userManager.getSerialNumberForUser(profile);
    
         if (userId != 0) { 
        launcherApps.startMainActivity(
            new ComponentName(context.getPackageName(), MainActivity.class.getName()), 
            profile, null, null
        );
    }
}

    }
}
