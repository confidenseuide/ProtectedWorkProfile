package protectedwp.safespace;

import android.app.*;
import android.app.admin.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import java.util.*;

public class MyDeviceAdminReceiver extends DeviceAdminReceiver {

	@Override
	public void onPasswordFailed(Context context, Intent intent, UserHandle user) {
      super.onPasswordFailed(context, intent, user);

	  if (context.createDeviceProtectedStorageContext().getSharedPreferences("prefs", Context.MODE_PRIVATE).getBoolean("x1337", false)) {    
		  if (!android.os.Process.myUserHandle().equals(user)) {		  
		  UserManager um0 = (UserManager) context.getSystemService(Context.USER_SERVICE);   		  
		  KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);    	  
		  if (!um0.isUserUnlocked(android.os.Process.myUserHandle()) || km.isKeyguardLocked()) {wipe.wipe(context);}
	  } }
	
	  if (!android.os.Process.myUserHandle().equals(user)) return;
	  DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
      if (!dpm.isProfileOwnerApp(context.getPackageName())) return;	  
            
         UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);            
         if (!um.isUserUnlocked(android.os.Process.myUserHandle())) {    
             ComponentName admin = new ComponentName(context, MyDeviceAdminReceiver.class);                            
             SharedPreferences prefsDH = context.createDeviceProtectedStorageContext().getSharedPreferences("UPM", Context.MODE_PRIVATE);
             if (prefsDH.getBoolean("UPM", false) && prefsDH.getBoolean("UPM1", true)) {						
                    wipe.wipe(context);
             }
        }	
	}
	
    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(context, MyDeviceAdminReceiver.class);    
  
        dpm.setProfileEnabled(admin);
        dpm.setProfileName(admin, "Protected WP");

		LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        List<UserHandle> profiles = userManager.getUserProfiles();

        for (UserHandle profile : profiles) {
        long userId = userManager.getSerialNumberForUser(profile);
    
         if (userId != 0) { 
			try {
			launcherApps.startMainActivity(new ComponentName(context.getPackageName(), MainActivity.class.getName()), profile, null, null);
			} 
			catch (Throwable t2) {}  
		 background.work.around.Start.RunService(context);
		 }
		}
	}
}
