package com.example.hider;
public class DestroyActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
		dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);		
		if (!isWorkProfileContext() && hasWorkProfile()) {
            launchWorkProfileDelayed();
		}
		if (!isWorkProfileContext() && !hasWorkProfile()) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
    }
  
}
