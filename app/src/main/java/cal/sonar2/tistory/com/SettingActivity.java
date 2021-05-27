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

public class SettingActivity extends AppCompatActivity {

    static SharedPreferences sharedPreferences = null;
    static SharedPreferences.Editor editor = null;
    String hyuCalID, moonCalID, userCalID, weekSize, titleSize, cDateSize, nDateSize, clockSize;
    EditText editTextHyuCalID, editTextMoonCalID, editTextUserCalID, editWeekSize, editTitleSize, editCDateSize, editNDateSize, editClockSize;
    TextView tvWeekSizeDefault;
    final String strKeyHyuCalID = "keyHyuCalID";
    final String strKeyMoonCalID = "keyMoonCalID";
    final String strKeyUserCalID = "keyUserCalID";
    final String strKeyWeekSize = "keyWeekSize";
    final String strKeyTitleSize = "keyTitleSize";
    final String strKeyCDateSize = "keyCDateSize";
    final String strKeyNDateSize = "keyNDateSize";
    final String strKeyClockSize = "keyClockSize";

    float dpWidth,dpHeight;


    @SuppressLint({"DefaultLocale", "CommitPrefEdits"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        String SHARE_NAME = "SHARE_CAL";
        sharedPreferences = getSharedPreferences(SHARE_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        editTextHyuCalID = findViewById(R.id.etHyuCalID);
        editTextMoonCalID = findViewById(R.id.etMoonCalID);
        editTextUserCalID = findViewById(R.id.etUserCalID);
        tvWeekSizeDefault = findViewById(R.id.tvWeekSizeDefalut);
        editWeekSize = findViewById(R.id.etWeekSize);
        editTitleSize = findViewById(R.id.etTitleSize);
        editCDateSize = findViewById(R.id.etCDateSize);
        editNDateSize = findViewById(R.id.etNDateSize);
        editClockSize = findViewById(R.id.etClockSize);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels / density;
        dpWidth = outMetrics.widthPixels / density;
        tvWeekSizeDefault.setText(String.format("추천 : %d , ", Math.round(Math.min(dpWidth, dpHeight * 1866.66f / 1104.0f) / 1866.66f * 50.0f)));

        loadData();
    }

    @SuppressLint("DefaultLocale")
    public void loadData() {
        hyuCalID = sharedPreferences.getString(strKeyHyuCalID,"ltm0jrlsamv8mlhrg0bpcgu6ps@group.calendar.google.com");
        moonCalID = sharedPreferences.getString(strKeyMoonCalID,"pd4kptmef56cqpc5mcs1g30lhc@group.calendar.google.com");
        userCalID = sharedPreferences.getString(strKeyUserCalID,"");
        weekSize = sharedPreferences.getString(strKeyWeekSize,String.format("%d", Math.round(Math.min(dpWidth, dpHeight * 1866.66f / 1104.0f) / 1866.66f * 50.0f)));
        titleSize = sharedPreferences.getString(strKeyTitleSize,String.format("%d", Math.round(Math.min(dpWidth, dpHeight * 1866.66f / 1104.0f) / 1866.66f * 90.0f)));
        cDateSize = sharedPreferences.getString(strKeyCDateSize,String.format("%d", Math.round(Math.min(dpWidth, dpHeight * 1866.66f / 1104.0f) / 1866.66f * 39.0f)));
        nDateSize = sharedPreferences.getString(strKeyNDateSize,String.format("%d", Math.round(Math.min(dpWidth, dpHeight * 1866.66f / 1104.0f) / 1866.66f * 30.0f)));
        clockSize = sharedPreferences.getString(strKeyClockSize,String.format("%d", Math.round(Math.min(dpWidth, dpHeight * 1866.66f / 1104.0f) / 1866.66f * 100.0f)));

        editTextHyuCalID.setText(hyuCalID);
        editTextMoonCalID.setText(moonCalID);
        editTextUserCalID.setText(userCalID);
        editWeekSize.setText(weekSize);
        editTitleSize.setText(titleSize);
        editCDateSize.setText(cDateSize);
        editNDateSize.setText(nDateSize);
        editClockSize.setText(clockSize);
    }

    public void saveData() {
        hyuCalID = editTextHyuCalID.getText().toString();
        moonCalID = editTextMoonCalID.getText().toString();
        userCalID = editTextUserCalID.getText().toString();
        weekSize = editWeekSize.getText().toString();
        titleSize = editTitleSize.getText().toString();
        cDateSize = editCDateSize.getText().toString();
        nDateSize = editNDateSize.getText().toString();
        clockSize = editClockSize.getText().toString();

        editor.putString(strKeyHyuCalID,hyuCalID);
        editor.putString(strKeyMoonCalID,moonCalID);
        editor.putString(strKeyUserCalID, userCalID);
        editor.putString(strKeyWeekSize,weekSize);
        editor.putString(strKeyTitleSize,titleSize);
        editor.putString(strKeyCDateSize,cDateSize);
        editor.putString(strKeyNDateSize,nDateSize);
        editor.putString(strKeyClockSize,clockSize);

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
        editTitleSize.setText(titleSize);
        editCDateSize.setText(cDateSize);
        editNDateSize.setText(nDateSize);
        editClockSize.setText(clockSize);
    }
}