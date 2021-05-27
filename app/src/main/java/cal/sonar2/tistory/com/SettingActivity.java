package cal.sonar2.tistory.com;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Map;

public class SettingActivity extends AppCompatActivity {

    static private String SHARE_NAME = "SHARE_CAL";
    static SharedPreferences sharedPreferences = null;
    static SharedPreferences.Editor editor = null;
    String hyuCalID, moonCalID, userCalID, weekSize;
    EditText editTextHyuCalID, editTextMoonCalID, editTextUserCalID, editWeekSize;
    TextView tvWeekSizeDefault;
    final String strKeyHyuCalID = "keyHyuCalID";
    final String strKeyMoonCalID = "keyMoonCalID";
    final String strKeyUserCalID = "keyUserCalID";
    final String strKeyWeekSize = "keyWeekSize";

    float dpWidth,dpHeight;


    @SuppressLint({"DefaultLocale", "CommitPrefEdits"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sharedPreferences = getSharedPreferences(SHARE_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        editTextHyuCalID = findViewById(R.id.etHyuCalID);
        editTextMoonCalID = findViewById(R.id.etMoonCalID);
        editTextUserCalID = findViewById(R.id.etUserCalID);
        tvWeekSizeDefault = findViewById(R.id.tvWeekSizeDefalut);
        editWeekSize = findViewById(R.id.etWeekSize);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels / density;
        dpWidth = outMetrics.widthPixels / density;
        tvWeekSizeDefault.setText(String.format("  추천 : %d", Math.round(Math.min(dpWidth, dpHeight * 1866.66f / 1104.0f) / 1866.66f * 50.0f)));

        loadData();
    }

    @SuppressLint("DefaultLocale")
    public void loadData() {
        hyuCalID = sharedPreferences.getString(strKeyHyuCalID,"ltm0jrlsamv8mlhrg0bpcgu6ps@group.calendar.google.com");
        moonCalID = sharedPreferences.getString(strKeyMoonCalID,"pd4kptmef56cqpc5mcs1g30lhc@group.calendar.google.com");
        userCalID = sharedPreferences.getString(strKeyUserCalID,"");
        weekSize = sharedPreferences.getString(strKeyWeekSize,String.format("%d", Math.round(Math.min(dpWidth, dpHeight * 1866.66f / 1104.0f) / 1866.66f * 50.0f)));
        editTextHyuCalID.setText(hyuCalID);
        editTextMoonCalID.setText(moonCalID);
        editTextUserCalID.setText(userCalID);
        editWeekSize.setText(weekSize);
    }

    public void saveData() {
        hyuCalID = editTextHyuCalID.getText().toString();
        moonCalID = editTextMoonCalID.getText().toString();
        userCalID = editTextUserCalID.getText().toString();
        weekSize = editWeekSize.getText().toString();

        editor.putString(strKeyHyuCalID,hyuCalID);
        editor.putString(strKeyMoonCalID,moonCalID);
        editor.putString(strKeyUserCalID, userCalID);
        editor.putString(strKeyWeekSize,weekSize);

        editor.apply();
    }

    public void gotoMain(View view) {
        saveData();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void cancelReset(View view) {
        editTextHyuCalID.setText(hyuCalID);
        editTextMoonCalID.setText(moonCalID);
        editTextUserCalID.setText(userCalID);
        editWeekSize.setText(weekSize);
    }
}