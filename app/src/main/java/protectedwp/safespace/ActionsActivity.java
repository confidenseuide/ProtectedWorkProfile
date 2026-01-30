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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
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
        Intent intent = new Intent(ActionsActivity.this, SecurityActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		startActivity(intent);
    }

    private void loadActivities() {
    try {
        PackageManager pm = getPackageManager();
        // Получаем список всех активити из манифеста
        PackageInfo pi = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
        
        for (ActivityInfo info : pi.activities) {
            // 1. Пропускаем саму себя
            if (info.name.equals(this.getClass().getName())) continue;

            // 2. Если это MainActivity — добавляем её под вашим спец-лейблом
            if (info.name.endsWith(".MainActivity")) {
                labelToClass.put(RESET_LABEL, info.name);
                continue; // Переходим к следующей, чтобы не зайти в общую логику
            }

            // 3. Получаем реальный лейбл из манифеста
            String label = info.loadLabel(pm).toString();

            // 4. ФИЛЬТРАЦИЯ: 
            // Если лейбл совпадает с названием класса, или он пустой, 
            // или это стандартное название "ProtectedWorkProfile" — ИГНОРИРУЕМ.
            if (label.equals(info.name) || 
                label.isEmpty() || 
                label.equalsIgnoreCase("ProtectedWorkProfile") ||
                label.contains("⁢")) { // Проверка на скрытый символ, если он есть в строке
                continue; 
            }

            // 5. Если прошли все проверки — добавляем в список
            labelToClass.put(label, info.name);
        }
    } catch (Exception ignored) {}
	}
	

    @Override
    protected void onResume() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_FULLSCREEN
			| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }
}
