package protectedwp.safespace;

import android.app.*;
import android.app.admin.*;
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
        // 1. ФЛАГИ СУКА ДО SUPER
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Магия пробития локскрина через флаги окна
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            // FLAG_SHOW_WHEN_LOCKED | FLAG_DISMISS_KEYGUARD | FLAG_TURN_SCREEN_ON
            getWindow().addFlags(0x00080000 | 0x00400000 | 0x00200000); 
        }

        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.BLACK);

        LinearLayout contentBox = new LinearLayout(this);
        contentBox.setOrientation(LinearLayout.VERTICAL);
        contentBox.setGravity(Gravity.CENTER_HORIZONTAL);
        contentBox.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        ((LinearLayout.LayoutParams)contentBox.getLayoutParams()).setMargins(60, 0, 60, 0);

        TextView title = new TextView(this);
        title.setText("What to do?");
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.WHITE);
        title.setPadding(0, 0, 0, 40);
        title.setGravity(Gravity.CENTER);
        contentBox.addView(title);

        ListView listView = new ListView(this);
        listView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        contentBox.addView(listView);

        root.addView(contentBox);
        setContentView(root);

        labelToClass.put(CLOSE_APP_LABEL, "ACTION_CLOSE");
        loadActivities();

        List<String> labels = new ArrayList<>(labelToClass.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String label = labels.get(position);
            String className = labelToClass.get(label);

            if (label.equals(CLOSE_APP_LABEL)) {
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory(Intent.CATEGORY_HOME);
                home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(home);
            } else if (label.equals(RESET_LABEL)) {
                unlock();
            } else {
                try {
                    Intent i = new Intent();
                    i.setComponent(new ComponentName(getPackageName(), className));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } catch (Exception ignored) {}
            }
        });
    }

    private void unlock() {
        UserManager um = (UserManager) getSystemService(Context.USER_SERVICE);
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        
        // Явное указание android.os.Process, чтобы компилятор не ныл
        UserHandle myUserHandle = android.os.Process.myUserHandle();

        // Если профиль в "тихом режиме" (залочен на уровне юзера)
        if (Build.VERSION.SDK_INT >= 24 && um.isQuietModeEnabled(myUserHandle)) {
            // Посылаем запрос на отключение тихого режима — это триггерит системный локскрин
            // В Work Profile это самый верный способ заставить систему показать ввод пароля
            Intent intent = new Intent(Intent.ACTION_RUN); // Заглушка, система перехватит
            // Но лучше просто доверять KeyguardManager, если профиль активен
        }

        if (um.isUserUnlocked()) {
            savePrefsAndRestart();
            return;
        }

        // ОСНОВНОЙ ПИНОК СИСТЕМЫ
        if (Build.VERSION.SDK_INT >= 26) {
            km.requestDismissKeyguard(this, new KeyguardManager.KeyguardDismissCallback() {
                @Override
                public void onDismissSucceeded() {
                    savePrefsAndRestart();
                }
