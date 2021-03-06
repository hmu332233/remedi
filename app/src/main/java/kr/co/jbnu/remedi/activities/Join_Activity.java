package kr.co.jbnu.remedi.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.util.TextUtils;

import java.io.IOException;
import java.util.ArrayList;

import kr.co.jbnu.remedi.R;
import kr.co.jbnu.remedi.Utils.ProgressBarDialog;
import kr.co.jbnu.remedi.gcm.PreferenceUtil;
import kr.co.jbnu.remedi.models.Answer;
import kr.co.jbnu.remedi.models.Board;
import kr.co.jbnu.remedi.models.Reply;
import kr.co.jbnu.remedi.models.User;
import kr.co.jbnu.remedi.serverIDO.ServerConnectionManager;
import kr.co.jbnu.remedi.serverIDO.ServerConnectionService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Join_Activity extends AppCompatActivity {

    EditText email_input;
    EditText password_input;
    EditText name_input;

    ImageButton normal_user_img_btn;
    ImageButton pharmist_user_img_btn;

    ImageButton sign_up_btn;



    String type = null;
    private Boolean is_exist = false;
    Context context;
    private  ProgressBarDialog progressBarDialog;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "701682787336";

    private GoogleCloudMessaging _gcm;
    private String registration_id = null;

    private String email;
    private String pw;
    private String name;



    /*
    * Intent로 일반 인지 아닌지 받아줘야함
     * 만약 화면을 바꿀경우 다른 엑티비티로 만들예정
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_);
        context = getApplicationContext();
        initComponent();
        initEvent();



    }

    private void initComponent(){
        email_input = (EditText)findViewById(R.id.email_input);
        password_input = (EditText)findViewById(R.id.password_input);
        name_input = (EditText)findViewById(R.id.name_input);
        normal_user_img_btn = (ImageButton)findViewById(R.id.normal_user_join_btn);
        pharmist_user_img_btn = (ImageButton)findViewById(R.id.pharm_user_join_btn);
        sign_up_btn = (ImageButton)findViewById(R.id.sign_up_button);
    }

    private void initEvent(){

        normal_user_img_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    normal_user_img_btn.setImageResource(R.drawable.signup_common_btn_on);
                    pharmist_user_img_btn.setImageResource(R.drawable.signup_pharmacist_btn_off);
                    type = "normal";
                }

                return false;
            }
        });

        pharmist_user_img_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    pharmist_user_img_btn.setImageResource(R.drawable.signup_pharmacist_btn_on);
                    normal_user_img_btn.setImageResource(R.drawable.signup_common_btn_off);
                    type = "pharm";
                }

                return false;
            }
        });



        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarDialog = new ProgressBarDialog(Join_Activity.this);
                progressBarDialog.show();
                if(type==null) {
                    progressBarDialog.dismiss();
                    Toast.makeText(context, "회원님의 종류를 선택해주세요", Toast.LENGTH_SHORT).show();
                }
                else checkExistUser();

            }
        });
    }

    private void JoinUserAftergetGcmRegisterId(){
        if (checkPlayServices())
        {
            _gcm = GoogleCloudMessaging.getInstance(this);
            registration_id = getRegistrationId();
            System.out.println("reg 확인 : "+registration_id);

            if (registration_id==null) registerInBackground();
            else {
                System.out.println("이미 등록된 기기입니다.");
                joinUser();
            }

        }
        else
        {
            System.out.println("구글 서비스를 이용 할 수 없습니다");
        }
    }

    private void joinUser(){
        System.out.println("일반 유저 회원가입 시도");

        ServerConnectionManager serverConnectionManager = ServerConnectionManager.getInstance();
        ServerConnectionService serverConnectionService = serverConnectionManager.getServerConnection();

        email =email_input.getText().toString();
        pw = password_input.getText().toString();
        name = name_input.getText().toString();
        System.out.println(registration_id+"id 확인");
        Call<Void> user_join =  serverConnectionService.user_join(email,pw,name,type,registration_id);


        user_join.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                User  user = new User(email,name,type);
                User.setUser(user);
                System.out.println("존재 하는 값 ");
                if(User.getInstance().getUser_type().equals("normal")) get_normaluser_boardlist();
                else get_pharmacist_boardlist();
                Toast.makeText(context, "회원가입 완료", Toast.LENGTH_SHORT).show();

            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                //final TextView textView = (TextView) findViewById(R.id.textView);
                //textView.setText("Something went wrong: " + t.getMessage());
                progressBarDialog.dismiss();
                Log.w("서버 통신 실패",t.getMessage());
            }
        });
    }



    private void checkExistUser(){
        System.out.println("유저 존재 확인 시도");

        ServerConnectionManager serverConnectionManager = ServerConnectionManager.getInstance();
        ServerConnectionService serverConnectionService = serverConnectionManager.getServerConnection();

        Call<Boolean> is_exsist = serverConnectionService.check_exist_user(email_input.getText().toString());



        is_exsist.enqueue(new Callback<Boolean>() {

            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                is_exist = response.body();
                System.out.println("존재 하는 값 확인"+is_exist.toString());
                if(is_exist==true){
                    progressBarDialog.dismiss();
                    Toast.makeText(context, "존재하는 아이디 입니다 다른 아이디로 바꿔주세요", Toast.LENGTH_SHORT).show();
                }else{
                    JoinUserAftergetGcmRegisterId();
                }

            }
            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                //final TextView textView = (TextView) findViewById(R.id.textView);
                //textView.setText("Something went wrong: " + t.getMessage());
                progressBarDialog.dismiss();
                Log.w("서버 통신 실패",t.getMessage());
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        // display received msg
        String msg = intent.getStringExtra("msg");

        if (!TextUtils.isEmpty(msg));

    }

    // google play service가 사용가능한가
    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {

                finish();
            }
            return false;
        }
        return true;
    }

    // registration  id를 가져온다.
    private String getRegistrationId()
    {
        String registrationId = PreferenceUtil.instance(getApplicationContext()).regId();
        if (TextUtils.isEmpty(registrationId))
        {

            return null;
        }
        int registeredVersion = PreferenceUtil.instance(getApplicationContext()).appVersion();
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion)
        {

            return null;
        }
        return registrationId;
    }

    // app version을 가져온다. 뭐에 쓰는건지는 모르겠다.
    private int getAppVersion()
    {
        try
        {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    // gcm 서버에 접속해서 registration id를 발급받는다.
    private void registerInBackground()
    {
        new AsyncTask<Void, Void, String>()
        {

            @Override
            protected String doInBackground(Void... params)
            {
                String reg_id = "";
                String msg = "";
                try
                {
                    if (_gcm == null)
                    {
                        _gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    reg_id = _gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + reg_id;
                    System.out.println("gcm async 안"+reg_id);
                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(reg_id);
                }
                catch (IOException ex)
                {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }

                return reg_id;
            }

            @Override
            protected void onPostExecute(String reg_id)
            {
                    registration_id = reg_id;
                    joinUser();
            }
        }.execute(null, null, null);
    }

    // registraion id를 preference에 저장한다.
    private void storeRegistrationId(String regId)
    {
        int appVersion = getAppVersion();

        PreferenceUtil.instance(getApplicationContext()).putRedId(regId);
        PreferenceUtil.instance(getApplicationContext()).putAppVersion(appVersion);
    }

    private void get_normaluser_boardlist(){
        System.out.println("일반 유저 보드 데이터 요청");

        ServerConnectionManager serverConnectionManager = ServerConnectionManager.getInstance();
        ServerConnectionService serverConnectionService = serverConnectionManager.getServerConnection();



        final Call<ArrayList<Board>> board = serverConnectionService.get_normaluser_board(User.getInstance().getEmail());
        board.enqueue(new Callback<ArrayList<Board>>() {

            @Override
            public void onResponse(Call<ArrayList<Board>> call, Response<ArrayList<Board>> response) {

                ArrayList<Board> boardlist = response.body();
                User.getInstance().setUserBoardList(boardlist);
                progressBarDialog.dismiss();
                for(int i=0;i<boardlist.size();i++){
                    System.out.println("board 아이디 : "+boardlist.get(i).getId());
                    if(boardlist.get(i).getAnswer()!=null){
                        Answer answer = boardlist.get(i).getAnswer();
                        System.out.println("답글 내용"+answer.getMedi_name()+answer.getMedi_company()+answer.getMedi_effect());
                        ArrayList<Reply> replies = answer.getRepliesList();
                        if(replies!=null){
                            for(int j=0;j<replies.size();j++)
                                System.out.println("댓글 내용 : "+replies.get(j).getContent());
                        }
                    }
                }
                Intent intent = new Intent(Join_Activity.this, MainActivity.class);
                startActivity(intent);
                finish();

            }
            @Override
            public void onFailure(Call<ArrayList<Board>> call, Throwable t) {
                //final TextView textView = (TextView) findViewById(R.id.textView);
                //textView.setText("Something went wrong: " + t.getMessage());
                //progressBarDialog.dismiss();
                Log.w("서버 통신 실패",t.getMessage());
            }
        });
    }

    private void get_pharmacist_boardlist(){
        System.out.println("약사 보드 데이터 요청");

        ServerConnectionManager serverConnectionManager = ServerConnectionManager.getInstance();
        ServerConnectionService serverConnectionService = serverConnectionManager.getServerConnection();

        final Call<ArrayList<Board>> board = serverConnectionService.get_pharmacist_normalboard(User.getInstance().getEmail());
        board.enqueue(new Callback<ArrayList<Board>>() {

            @Override
            public void onResponse(Call<ArrayList<Board>> call, Response<ArrayList<Board>> response) {

                ArrayList<Board> boardlist = response.body();
                User.getInstance().setUserBoardList(boardlist);
                progressBarDialog.dismiss();

                if(boardlist!=null){
                    for(int i=0;i<boardlist.size();i++){
                        System.out.println("board 아이디 : "+boardlist.get(i).getId());
                        if(boardlist.get(i).getAnswer()!=null){
                            Answer answer = boardlist.get(i).getAnswer();
                            System.out.println("답글 내용"+answer.getMedi_name()+answer.getMedi_company()+answer.getMedi_effect());
                            ArrayList<Reply> replies = answer.getRepliesList();
                            if(replies!=null){
                                for(int j=0;j<replies.size();j++)
                                    System.out.println("댓글 내용 : "+replies.get(j).getContent());
                            }
                        }
                    }
                }

                Intent intent = new Intent(Join_Activity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            @Override
            public void onFailure(Call<ArrayList<Board>> call, Throwable t) {
                //final TextView textView = (TextView) findViewById(R.id.textView);
                //textView.setText("Something went wrong: " + t.getMessage());
                //progressBarDialog.dismiss();
                Log.w("서버 통신 실패",t.getMessage());
            }
        });
    }


}
