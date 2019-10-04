package cal.sonar2.tistory.com;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.CalendarScopes;

import com.google.api.services.calendar.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static cal.sonar2.tistory.com.R.*;


public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {


    /**
     * Google Calendar API에 접근하기 위해 사용되는 구글 캘린더 API 서비스 객체
     */

    private com.google.api.services.calendar.Calendar mService = null;

    /**
     * Google Calendar API 호출 관련 메커니즘 및 AsyncTask을 재사용하기 위해 사용
     */
    private  int mID = 0;


    GoogleAccountCredential mCredential;
    //private TextView mStatusText;
    // private TextView mResultText;
    private Button mGetEventButton;
    //private Button mAddEventButton;
    //private Button mAddCalendarButton;
    //ProgressDialog mProgress;
    private Timer mTimer;

    private TextView[][] mDate = new TextView[6][7];
    private  LinearLayout mL0;
    private  LinearLayout mL5;
    private  LinearLayout mL1;
    private  LinearLayout mLmain;
    private  TextClock textClock;
    private float dpWidth;
    private float dpHeight;
    private float dpSizeFactor;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    //원장실 모니터 글자 크기
    private  int cDateSize;
    private  int nDateSize;


    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //켜짐유지

        setContentView(layout.activity_main);

        //mAddCalendarButton = (Button) findViewById(R.id.button_main_add_calendar);
        //mAddEventButton = (Button) findViewById(R.id.button_main_add_event);
        mGetEventButton = (Button) new Button(this); // findViewById(id.button_main_get_event);

        //mStatusText = (TextView) findViewById(R.id.textview_main_status);
        //mResultText = (TextView) findViewById(R.id.textview_main_result);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density  = getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels / density;
        dpWidth  = outMetrics.widthPixels / density;

        dpSizeFactor = Math.min(dpWidth, dpHeight*1866.66f/1104.0f)/1866.66f;

        cDateSize = (int)(dpSizeFactor * 39.0f);
        nDateSize = (int)(dpSizeFactor * 30.0f);

        ((TextView)findViewById(id.title_sun)).setTextSize(dpSizeFactor*50.0f);
        ((TextView)findViewById(id.title_mon)).setTextSize(dpSizeFactor*50.0f);
        ((TextView)findViewById(id.title_tue)).setTextSize(dpSizeFactor*50.0f);
        ((TextView)findViewById(id.title_wed)).setTextSize(dpSizeFactor*50.0f);
        ((TextView)findViewById(id.title_thu)).setTextSize(dpSizeFactor*50.0f);
        ((TextView)findViewById(id.title_fri)).setTextSize(dpSizeFactor*50.0f);
        ((TextView)findViewById(id.title_sat)).setTextSize(dpSizeFactor*50.0f);

        Log.d("눂이",String.valueOf(dpHeight));

        mL0 = (LinearLayout) findViewById(id.ll0);
        mL1 = (LinearLayout) findViewById(id.ll1);
        mL5 = (LinearLayout) findViewById(id.ll5);
        mLmain = (LinearLayout) findViewById(id.activity_main);
        textClock = new TextClock(this); //시계

        mDate[0][0] = (TextView) findViewById(id.d00);
        mDate[0][5] = (TextView) findViewById(id.d05);
        mDate[0][6] = (TextView) findViewById(id.d06);

        mDate[1][0] = (TextView) findViewById(id.d10);
        mDate[1][1] = (TextView) findViewById(id.d11);
        mDate[1][2] = (TextView) findViewById(id.d12);
        mDate[1][3] = (TextView) findViewById(id.d13);
        mDate[1][4] = (TextView) findViewById(id.d14);
        mDate[1][5] = (TextView) findViewById(id.d15);
        mDate[1][6] = (TextView) findViewById(id.d16);

        mDate[2][0] = (TextView) findViewById(id.d20);
        mDate[2][1] = (TextView) findViewById(id.d21);
        mDate[2][2] = (TextView) findViewById(id.d22);
        mDate[2][3] = (TextView) findViewById(id.d23);
        mDate[2][4] = (TextView) findViewById(id.d24);
        mDate[2][5] = (TextView) findViewById(id.d25);
        mDate[2][6] = (TextView) findViewById(id.d26);

        mDate[3][0] = (TextView) findViewById(id.d30);
        mDate[3][1] = (TextView) findViewById(id.d31);
        mDate[3][2] = (TextView) findViewById(id.d32);
        mDate[3][3] = (TextView) findViewById(id.d33);
        mDate[3][4] = (TextView) findViewById(id.d34);
        mDate[3][5] = (TextView) findViewById(id.d35);
        mDate[3][6] = (TextView) findViewById(id.d36);

        mDate[4][0] = (TextView) findViewById(id.d40);
        mDate[4][1] = (TextView) findViewById(id.d41);
        mDate[4][2] = (TextView) findViewById(id.d42);
        mDate[4][3] = (TextView) findViewById(id.d43);
        mDate[4][4] = (TextView) findViewById(id.d44);
        mDate[4][5] = (TextView) findViewById(id.d45);
        mDate[4][6] = (TextView) findViewById(id.d46);

        mDate[5][0] = (TextView) findViewById(id.d50);
        mDate[5][1] = (TextView) findViewById(id.d51);
        mDate[5][2] = (TextView) findViewById(id.d52);
        mDate[5][3] = (TextView) findViewById(id.d53);
        mDate[5][4] = (TextView) findViewById(id.d54);
        mDate[5][5] = (TextView) findViewById(id.d55);
        mDate[5][6] = (TextView) findViewById(id.d56);


        mGetEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetEventButton.setEnabled(false);
                //mStatusText.setText("");
                mID = 3;        //이벤트 가져오기
                getResultsFromApi();
                mGetEventButton.setEnabled(true);
            }
        });


        // Google Calendar API 사용하기 위해 필요한 인증 초기화( 자격 증명 credentials, 서비스 객체 )
        // OAuth 2.0를 사용하여 구글 계정 선택 및 인증하기 위한 준비
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES)
        ).setBackOff(new ExponentialBackOff()); // I/O 예외 상황을 대비해서 백오프 정책 사용

        //mGetEventButton.callOnClick();
        final Handler handler = new Handler(){
            public void handleMessage(Message msg){
                mGetEventButton.performClick();
                Log.d("timer","event");
            }
        };

        final TimerTask mTask = new TimerTask() {
            @Override
            public void run() {

                Message msg = handler.obtainMessage();
                handler.sendMessage(msg);
            }
        };

        if(mTimer==null) {
            mTimer = new Timer();
            mTimer.schedule(mTask, 1000L, 300000L);  //1초후 5분마다
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        //Toast.makeText(getApplicationContext(),"onDestroy()호출", Toast.LENGTH_SHORT).show();
    }

    public void perform_action(View v)
    {
        TextView tv= (TextView) findViewById(R.id.title_sun);
        mID = 3;
        tv.setTextColor(Color.WHITE);
        getResultsFromApi();
    }


    /**
     * 다음 사전 조건을 모두 만족해야 Google Calendar API를 사용할 수 있다.
     *
     * 사전 조건
     *     - Google Play Services 설치
     *     - 유효한 구글 계정 선택
     *     - 안드로이드 디바이스에서 인터넷 사용 가능
     *
     * 하나라도 만족하지 않으면 해당 사항을 사용자에게 알림.
     */
    private String getResultsFromApi() {

        if (!isGooglePlayServicesAvailable()) { // Google Play Services를 사용할 수 없는 경우

            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) { // 유효한 Google 계정이 선택되어 있지 않은 경우
            chooseAccount();
        } else if (!isDeviceOnline()) {    // 인터넷을 사용할 수 없는 경우
            //mStatusText.setText("No network connection available.");
        } else {

            // Google Calendar API 호출
            new MakeRequestTask(this, mCredential).execute();
        }
        return null;
    }



    /**
     * 안드로이드 디바이스에 최신 버전의 Google Play Services가 설치되어 있는지 확인
     */
    private boolean isGooglePlayServicesAvailable() {

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }



    /*
     * Google Play Services 업데이트로 해결가능하다면 사용자가 최신 버전으로 업데이트하도록 유도하기위해
     * 대화상자를 보여줌.
     */
    private void acquireGooglePlayServices() {

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {

            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }



    /*
     * 안드로이드 디바이스에 Google Play Services가 설치 안되어 있거나 오래된 버전인 경우 보여주는 대화상자
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode
    ) {

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES
        );
        dialog.show();
    }



    /*
     * Google Calendar API의 자격 증명( credentials ) 에 사용할 구글 계정을 설정한다.
     *
     * 전에 사용자가 구글 계정을 선택한 적이 없다면 다이얼로그에서 사용자를 선택하도록 한다.
     * GET_ACCOUNTS 퍼미션이 필요하다.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        //Toast.makeText(getApplicationContext(),"chooseAccount", Toast.LENGTH_SHORT).show();
        // GET_ACCOUNTS 권한을 가지고 있다면
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            // SharedPreferences에서 저장된 Google 계정 이름을 가져온다.
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            //Toast.makeText(getApplicationContext(),accountName, Toast.LENGTH_SHORT).show();
            if (accountName != null) {

                // 선택된 구글 계정 이름으로 설정한다.
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                //Toast.makeText(getApplicationContext(),"else", Toast.LENGTH_SHORT).show();

                // 사용자가 구글 계정을 선택할 수 있는 다이얼로그를 보여준다.
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }



            // GET_ACCOUNTS 권한을 가지고 있지 않다면
        } else {

            //Toast.makeText(getApplicationContext(),"ELSE", Toast.LENGTH_SHORT).show();
            // 사용자에게 GET_ACCOUNTS 권한을 요구하는 다이얼로그를 보여준다.(주소록 권한 요청함)
            EasyPermissions.requestPermissions(
                    (Activity)this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }



    /*
     * 구글 플레이 서비스 업데이트 다이얼로그, 구글 계정 선택 다이얼로그, 인증 다이얼로그에서 되돌아올때 호출된다.
     */

    @Override
    protected void onActivityResult(
            int requestCode,  // onActivityResult가 호출되었을 때 요청 코드로 요청을 구분
            int resultCode,   // 요청에 대한 결과 코드
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode) {

            case REQUEST_GOOGLE_PLAY_SERVICES:

                if (resultCode != RESULT_OK) {

                    //mStatusText.setText( " 앱을 실행시키려면 구글 플레이 서비스가 필요합니다."
                    //        + "구글 플레이 서비스를 설치 후 다시 실행하세요." );
                } else {

                    getResultsFromApi();
                }
                break;


            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;


            case REQUEST_AUTHORIZATION:

                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }


    /*
     * Android 6.0 (API 23) 이상에서 런타임 권한 요청시 결과를 리턴받음
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,  //requestPermissions(android.app.Activity, String, int, String[])에서 전달된 요청 코드
            @NonNull String[] permissions, // 요청한 퍼미션
            @NonNull int[] grantResults    // 퍼미션 처리 결과. PERMISSION_GRANTED 또는 PERMISSION_DENIED
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    /*
     * EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 승인한 경우 호출된다.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> requestPermissionList) {

        // 아무일도 하지 않음
    }


    /*
     * EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 거부한 경우 호출된다.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> requestPermissionList) {

        // 아무일도 하지 않음
    }


    /*
     * 안드로이드 디바이스가 인터넷 연결되어 있는지 확인한다. 연결되어 있다면 True 리턴, 아니면 False 리턴
     */
    private boolean isDeviceOnline() {

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }


    /*
     * 캘린더 이름에 대응하는 캘린더 ID를 리턴
     */
    private String getCalendarID(String calendarTitle){

        String id = null;

        // Iterate through entries in calendar list
        String pageToken = null;
        do {
            CalendarList calendarList = null;
            try {
                calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            }catch (IOException e) {
                e.printStackTrace();
            }
            List<CalendarListEntry> items = calendarList.getItems();


            for (CalendarListEntry calendarListEntry : items) {

                if ( calendarListEntry.getSummary().toString().equals(calendarTitle)) {

                    id = calendarListEntry.getId().toString();
                }
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);

        return id;
    }


    /*
     * 비동기적으로 Google Calendar API 호출
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, String> {

        int cYear;
        int cMonth,pMonth,nMonth;
        int startRow;
        int row,col;
        int startDay;
        int lastDay;
        int preDays, nextDays;
        int preStartDay, preEndDay;
        int today;

        String[][] sDat = new String[6][7];
        boolean[] hyu = new boolean[32];
        boolean[] hyuPrev = new boolean[32];
        boolean[] hyuNext = new boolean[8];


        private Exception mLastError = null;
        private MainActivity mActivity;
        List<String> eventStrings = new ArrayList<String>();


        public MakeRequestTask(MainActivity activity, GoogleAccountCredential credential) {

            mActivity = activity;

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            mService = new com.google.api.services.calendar.Calendar
                    .Builder(transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }


        @Override
        protected void onPreExecute() {
            ((TextView)findViewById(id.title_sun)).setTextColor(Color.rgb(0,255,255));
        }


        /*
         * 백그라운드에서 Google Calendar API 호출 처리
         */
        @Override
        protected String doInBackground(Void... params) {
            try {
                if (mID == 3) {

                    return getEvent();
                }


            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }

            return null;
        }


        /*
         * CalendarTitle 이름의 캘린더에서 이벤트를 가져와 리턴
         */
        private String getEvent() throws IOException, ParseException {
            int weeks;
            long i, is,ie;
            DateTime start, end;
            Events events;
            List<Event> items;

            Calendar mCal = Calendar.getInstance();
            Calendar nCal = Calendar.getInstance();
            Calendar tCal = Calendar.getInstance();
            Calendar pCal = Calendar.getInstance(); //전월


            DateTime now = new DateTime(System.currentTimeMillis());

            today = mCal.get(Calendar.DATE);
            mCal.set(Calendar.DATE, 1);
            //mCal.set(Calendar.MONTH, 11-1); /////////////////////////////////////////////////////////////////////////////////////////////////////////////for test
            nCal.set(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH), 1,0,0,0);  //1일
            pCal.set(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH), 1,0,0,0);  //1일
            nCal.add(Calendar.MONTH,1);
            pCal.add(Calendar.MONTH, -1);

            pMonth = pCal.get(Calendar.MONTH)+1;
            nMonth = nCal.get(Calendar.MONTH)+1;

            startDay = mCal.get(Calendar.DAY_OF_WEEK);
            lastDay = mCal.getActualMaximum(Calendar.DATE);

            weeks = ((startDay-1)+(lastDay)+6)/7; //올림을 위해 6더함.
            cYear = mCal.get(Calendar.YEAR);
            cMonth = mCal.get(Calendar.MONTH)+1;

            if(weeks <= 5) { //5주이내
                startRow = 1;
                preDays = startDay-1;
                nextDays = 36-(startDay+lastDay);
            }
            else { //6주인경우
                startRow = 0;
                preDays = startDay-6;
                nextDays = 43-(startDay+lastDay);
            }

            //pCal.add(Calendar.DATE, -1*nextDays);
            //preStartDay = pCal.get(Calendar.DATE);
            preEndDay = pCal.getActualMaximum(Calendar.DATE);
            preStartDay = preEndDay-preDays+1;


            Log.d("startDay",String.valueOf(startDay));
            Log.d("lastDay",String.valueOf(lastDay));
            Log.d("weeks",String.valueOf(weeks));
            Log.d("preDays",String.valueOf(preDays));
            Log.d("preStartDay",String.valueOf(preStartDay));
            Log.d("preEndDay",String.valueOf(preEndDay));
            Log.d("nextDays",String.valueOf(nextDays));




            DateTime sdate = new DateTime(mCal.getTimeInMillis()-preDays*86400000L);
            DateTime edate = new DateTime(nCal.getTimeInMillis()+nextDays*86400000L);

            //String calIDbusan = getCalendarID("부산직업능력개발원");
            //String calIDhyu = getCalendarID("대한민국의 휴일");
            String calIDbusan = "qpt36c54qi30i2buqnl8u2rff0@group.calendar.google.com";
            String calIDhyu = "ct171m3icbujnimhtp1j97e47savh8a6@import.calendar.google.com";

            Log.d("calIDbusan", calIDbusan);

            /////공휴일
            if ( calIDhyu != null ) {
                events = mService.events().list(calIDhyu)
                        .setMaxResults(50)
                        .setTimeMin(sdate)
                        .setTimeMax(edate)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                items = events.getItems();


                for (Event event2 : items) {

                    start = event2.getStart().getDate();
                    end = event2.getEnd().getDate();
                    if (start == null) {
                        start = event2.getStart().getDateTime();
                        end = event2.getEnd().getDateTime();
                    }

                    tCal.setTimeInMillis(start.getValue());
                    is = tCal.get(Calendar.DATE) + 100L*tCal.get(Calendar.MONTH) + 10000L*tCal.get(Calendar.YEAR);
                    tCal.setTimeInMillis(end.getValue());
                    ie = tCal.get(Calendar.DATE) + 100L*tCal.get(Calendar.MONTH) + 10000L*tCal.get(Calendar.YEAR);

                    i = is;
                    do {
                        //Log.d("i",String.valueOf(i));
                        if(i< 1 + 100L*mCal.get(Calendar.MONTH) + 10000L*mCal.get(Calendar.YEAR))  {
                            hyuPrev[(int)(i%100)] = true;
                        }
                        else if(i > lastDay + 100L*mCal.get(Calendar.MONTH) + 10000L*mCal.get(Calendar.YEAR)) {
                            row = startRow + ((startDay - 1) + lastDay + (int)(i%100) - 1) / 7;
                            col = ((startDay - 1) + lastDay + (int)(i%100) - 1) % 7;
                            //if (sDat[row][col] == null) sDat[row][col] = event2.getSummary();
                            //else sDat[row][col] += ("\n"+event2.getSummary());
                            hyuNext[(int)(i%100)] = true;
                            //Log.d("hyunext", String.valueOf(hyuNext[(int) (i % 100)]));
                        }
                        else {
                            row = startRow + ((startDay - 1) + (int)(i%100) - 1) / 7;
                            col = ((startDay - 1) + (int)(i%100) - 1) % 7;
                            //if (sDat[row][col] == null) sDat[row][col] = event2.getSummary();
                            //else sDat[row][col] += ("\n"+event2.getSummary());
                            hyu[(int) (i % 100)] = true;
                        }
                        if (sDat[row][col] == null) sDat[row][col] = event2.getSummary();
                        else sDat[row][col] += ("\n"+event2.getSummary());
                        i++;
                    } while (i < ie);
                }
            }

            //부산직능원 공유달력
            if ( calIDbusan != null ) {
                events = mService.events().list(calIDbusan)
                        .setMaxResults(100)
                        .setTimeMin(sdate)
                        .setTimeMax(edate)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                items = events.getItems();


                for (Event event : items) {

                    start = event.getStart().getDate();
                    end = event.getEnd().getDate();
                    if (start == null) { //시간이 있으면
                        start = event.getStart().getDateTime();
                        end = event.getEnd().getDateTime();
                        tCal.setTimeInMillis(end.getValue());
                        ie = tCal.get(Calendar.DATE) + 100L*tCal.get(Calendar.MONTH) + 10000L*tCal.get(Calendar.YEAR);
                    }
                    else { //날짜만 있으면...
                        tCal.setTimeInMillis(end.getValue()-86400000L);
                        ie = tCal.get(Calendar.DATE) + 100L*tCal.get(Calendar.MONTH) + 10000L*tCal.get(Calendar.YEAR);
                    }

                    tCal.setTimeInMillis(start.getValue());
                    is = tCal.get(Calendar.DATE) + 100L*tCal.get(Calendar.MONTH) + 10000L*tCal.get(Calendar.YEAR);

                    //sDat에 넣어라
                    i = is;
                    do {
                        //전달 스케쥴
                        if(i< 1 + 100L*mCal.get(Calendar.MONTH) + 10000L*mCal.get(Calendar.YEAR)) {
                            if((i%100<preStartDay) || (preDays==0)) { //전월 이전 스케쥴이거나 전월공간이 없으면 스킵
                                Log.d("전월이전자료",event.getSummary());  //이 로그는 지우면 안됨.
                                if(i%100 == preEndDay)
                                    i = 1 + 100L*mCal.get(Calendar.MONTH) + 10000L*mCal.get(Calendar.YEAR);
                                else
                                    i++;
                                continue;
                            }
                            Log.d("전월i",String.valueOf(i));
                            row = startRow;
                            if(row==0) {
                                col = (int)(i%100)-preStartDay + 5;
                            }
                            else {
                                col = (int)(i%100) - preStartDay;
                            }
                            if(i%100 == preEndDay)
                                i = 1 + 100L*mCal.get(Calendar.MONTH) + 10000L*mCal.get(Calendar.YEAR);
                            else
                                i++;

                        }
                        //다음달 스케쥴
                        else if(i > lastDay + 100L*mCal.get(Calendar.MONTH) + 10000L*mCal.get(Calendar.YEAR)) {
                            if(i%100>nextDays) break; //다음달 이후 스케줄이면 바로 중단
                            Log.d("후월i",String.valueOf(i));
                            row = startRow + ((startDay - 1) + lastDay -1 + (int) (i % 100) - 1) / 7;
                            col = ((startDay - 1) + lastDay + (int) (i % 100) - 1) % 7;
                            i++;
                        }
                        //현재달 스케쥴
                        else {
                            Log.d("현월i",String.valueOf(i));
                            row = startRow + ((startDay - 1) + (int) (i % 100) - 1) / 7;
                            col = ((startDay - 1) + (int) (i % 100) - 1) % 7;
                            if((i%100)==lastDay)
                                i = 1 + 100L*nCal.get(Calendar.MONTH) + 10000L*nCal.get(Calendar.YEAR);
                            else
                                i++;
                        }
                        Log.d("행",String.valueOf(row));
                        Log.d("열",String.valueOf(col));
                        if (sDat[row][col] == null) sDat[row][col] = event.getSummary();
                        else sDat[row][col] += ("<br />\n" + event.getSummary());


                    } while (i <= ie);
                }
            }

            //eventStrings.add(String.format("%s \n (%s~%s)", event.getSummary(), start, end));
            return eventStrings.size() + "개의 데이터를 가져왔습니다.";
        }



        @Override
        protected void onPostExecute(String output) {

            //mProgress.hide();
            ((TextView)findViewById(id.title_sun)).setTextColor(Color.rgb(255,0,0));
            //mStatusText.setText(output);

            //if ( mID == 3 )   mResultText.setText(TextUtils.join("\n", eventStrings));
            if(startRow==1) {  //첫주가 비었음. 둘째주부터 시작..대부분
                mL0.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0));
                mLmain.setWeightSum(50f);
            }
            else { //첫주가 있을 경우
                mL0.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1, 10f));
                mLmain.setWeightSum(60f);
            }
            mDate[0][0].setText(cYear + "년 " + cMonth + "월  일정표");
            for(int i=preStartDay; i<=preEndDay;i++) {  //이전달
                row = startRow;
                if(row==0) {
                    col = i-preStartDay + 5;
                }
                else {
                    col = i - preStartDay;
                }
                if(sDat[row][col]==null) mDate[row][col].setText(" " + String.valueOf(pMonth) + "/" + String.valueOf(i));
                else mDate[row][col].setText(" " + String.valueOf(pMonth) + "/" + String.valueOf(i) + "\n" + sDat[row][col]);
                if(hyuPrev[i]) {
                    mDate[row][col].setTextColor(getResources().getColor(R.color.calTextSun));
                }
                else {
                    switch (col) {
                        case 0:
                            mDate[row][col].setTextColor(getResources().getColor(R.color.calTextSun));
                            break;
                        case 6:
                            mDate[row][col].setTextColor(getResources().getColor(R.color.calTextSat));
                            break;
                        default:
                            mDate[row][col].setTextColor(getResources().getColor(R.color.calTextMonfri));
                    }
                }
                mDate[row][col].setTextSize(nDateSize);
                mDate[row][col].setHorizontallyScrolling(true);
            }
            for(int i=1;i<=lastDay;i++) {  //현재달
                row = startRow + ((startDay-1) + i - 1) / 7;
                col = ((startDay-1) + i - 1) % 7;
                if(sDat[row][col]==null) mDate[row][col].setText(Html.fromHtml("<u><b><big>&nbsp;" + String.valueOf(i) + "&nbsp;</big></b></u><br />"));
                else mDate[row][col].setText(Html.fromHtml("<u><b><big>&nbsp;" + String.valueOf(i)  + "&nbsp;</big></b></u><br />\n" + sDat[row][col]));
                if(hyu[i]) {
                    mDate[row][col].setTextColor(getResources().getColor(R.color.calTextSun));
                }
                else {
                    switch (col) {
                        case 0:
                            mDate[row][col].setTextColor(getResources().getColor(R.color.calTextSun));
                            break;
                        case 6:
                            mDate[row][col].setTextColor(getResources().getColor(R.color.calTextSat));
                            break;
                        default:
                            mDate[row][col].setTextColor(getResources().getColor(R.color.calTextMonfri));
                    }
                }
                //오늘이면,
                if(i == today) {
                    mDate[row][col].setBackgroundResource(R.drawable.today_date);
                    mDate[row][col].setTextColor(getResources().getColor(color.calTextToday));
                }
                else {
                    mDate[row][col].setBackgroundResource(R.drawable.yoil_date);
                }

                mDate[row][col].setTextSize(cDateSize);
                mDate[row][col].setHorizontallyScrolling(true);
            }
            for(int i=1;i<=nextDays;i++) {  //다음달
                //Log.d("이후",String.valueOf(i));
                row = startRow + ((startDay-1) + lastDay + i - 1) / 7;
                col = ((startDay-1) + lastDay + i - 1) % 7;
                if(sDat[row][col]==null) {
                    mDate[row][col].setText(" " + String.valueOf(nMonth) + "/" + String.valueOf(i));
                }
                else {
                    mDate[row][col].setText(" " + String.valueOf(nMonth) + "/" + String.valueOf(i) + "\n" + sDat[row][col]);
                }
                if(hyuNext[i]) {
                    mDate[row][col].setTextColor(getResources().getColor(R.color.calTextSun));
                }
                else {
                    switch (col) {
                        case 0:
                            mDate[row][col].setTextColor(getResources().getColor(R.color.calTextSun));
                            break;
                        case 6:
                            mDate[row][col].setTextColor(getResources().getColor(R.color.calTextSat));
                            break;
                        default:
                            mDate[row][col].setTextColor(getResources().getColor(R.color.calTextMonfri));
                    }
                }
                mDate[row][col].setTextSize(nDateSize);
                mDate[row][col].setHorizontallyScrolling(true);
            }

            //여기부터 시계표시
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT, 10);
            textClock.setLayoutParams(layoutParams);
            textClock.setTextSize(dpSizeFactor*100.0f);
            textClock.setFormat24Hour("HH:mm");
            textClock.setFormat12Hour("h:mm");
            textClock.setPadding(0,0,0,0);
            textClock.setBackgroundResource(R.drawable.yoil_date);
            textClock.setTextColor(Color.YELLOW);
            textClock.setGravity(Gravity.CENTER);

            if(textClock.getParent() != null) ((LinearLayout)(textClock.getParent())).removeView(textClock);
            if(mDate[1][0].getParent() == null) mL1.addView(mDate[1][0],0);
            if(mDate[5][6].getParent() == null) mL5.addView(mDate[5][6]);

            if(nextDays>0) {  //다음달 표시 일수가 1일 이상이면 마지막 줄에 시계표시
                mL5.removeView(mDate[5][6]);
                mL5.addView(textClock);
            }
            else { //다음달 표시 일수가 없으면, 즉, 빈칸이 없으면 둘째 줄에 시계표시 ....
                mL1.removeView(mDate[1][0]);
                mL1.addView(textClock, 0);
            }
        }


        @Override
        protected void onCancelled() {
            //mProgress.hide();
            ((TextView)findViewById(id.title_sun)).setTextColor(Color.rgb(255,0,0));
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    //mStatusText.setText("MakeRequestTask The following error occurred:\n" + mLastError.getMessage());
                }
            } else {
                //mStatusText.setText("요청 취소됨.");
            }
        }
    }
}