package protectedwp.safespace;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class ActionsActivity extends Activity {

    private Map<String, String> labelToClass = new LinkedHashMap<>();
    private static final String CLOSE_APP_LABEL = "CloseApp";
    private static final String RESET_LABEL = "ShowApps&SetUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
        } else {
            getWindow().addFlags(0x00080000); // FLAG_SHOW_WHEN_LOCKED
        }

        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.BLACK);

        ListView listView = new ListView(this);
        root.addView(listView);
        setContentView(root);

        labelToClass.put(CLOSE_APP_LABEL, "ACTION_CLOSE");
        loadActivities();

        List<String> labels = new ArrayList<>(labelToClass.keySet());
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels));

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String label = labels.get(position);
            if (label.equals(CLOSE_APP_LABEL)) {
                startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } else if (label.equals(RESET_LABEL)) {
                unlock();
            } else {
                try {
                    Intent i = new Intent().setComponent(new ComponentName(getPackageName(), labelToClass.get(label)));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } catch (Exception ignored) {}
            }
        });
    }

    private void unlock() {
        this.createDeviceProtectedStorageContext()
            .getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("isDone", false)
            .commit();

        LauncherApps la = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
        UserManager um = (UserManager) getSystemService(Context.USER_SERVICE);

        UserHandle mainUser = null;
        List<UserHandle> profiles = um.getUserProfiles();
        for (UserHandle u : profiles) {
            if (um.getSerialNumberForUser(u) == 0) {
                mainUser = u;
                break;
            }
        }

        if (mainUser != null) {
            // Пробиваем пароль через кросс-профильный запуск
            la.startMainActivity(new ComponentName(getPackageName(), getPackageName() + ".MainActivity"), mainUser, null, null);
        } else {
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private void loadActivities() {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            for (ActivityInfo info : pi.activities) {
                if (info.name.equals(this.getClass().getName())) continue;
                String label = info.name.endsWith("MainActivity") ? RESET_LABEL : info.loadLabel(pm).toString();
                labelToClass.put(label, info.name);
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }
}
