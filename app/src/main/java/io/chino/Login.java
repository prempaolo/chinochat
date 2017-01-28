package io.chino;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;

import io.chino.java.ChinoAPI;
import io.chino.api.auth.LoggedUser;
import io.chino.api.common.ChinoApiException;
import io.chino.api.document.Document;
import io.chino.api.permission.PermissionRule;
import io.chino.api.permission.PermissionValues;
import io.chino.api.repository.Repository;
import io.chino.api.schema.SchemaRequest;
import io.chino.api.user.User;
import com.prempaolo.simplenotification.gcm.ChinoFCM;
import com.prempaolo.simplenotification.gcm.ConstantsFCM;

import io.chino.utils.Constants;

public class Login extends AppCompatActivity {

    private final static String HOST = "https://api.test.chino.io/v1";
    EditText username;
    EditText password;
    TextView console;
    String USERNAME;
    String PASSWORD;
    String USER_ID;
    Button login;
    ProgressBar progressBar;
    Button loginPatient;
    Button loginDoctor;
    Button register;

    Toolbar toolbar;
    ChinoFCM chinoFCM;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        if(intent.hasExtra(Constants.INTENT_NEW_MESSAGE)){
            if(intent.getBooleanExtra(Constants.INTENT_NEW_MESSAGE, false)){
                Document d = ChinoFCM.getInstance().getDocumentShared();
                String user_id = (String)d.getContentAsHashMap().get(Constants.MESSAGE_USER_ID);
                if(((String)d.getContentAsHashMap().get(Constants.MESSAGE_ROLE)).equals("doctor")){
                    intent = new Intent(Login.this, DoctorMainActivity.class);
                    intent.putExtra(Constants.INTENT_NEW_MESSAGE, user_id);
                } else {
                    intent = new Intent(Login.this, PatientMainActivity.class);
                }
                startActivity(intent);
            }
        } else if (intent.hasExtra(Constants.INTENT_ADD_DOCTOR_REQUEST)){
            //TODO: gestire la richiesta di aggiungere un dottore
            if(intent.getBooleanExtra(Constants.INTENT_ADD_DOCTOR_REQUEST, false)){
                Document d = ChinoFCM.getInstance().getDocumentShared();
                HashMap<String, Object> content = (HashMap<String, Object>)d.getContentAsHashMap().get("content");
                String userId = (String)content.get("userId");
                String role = (String)content.get("role");
                Toast.makeText(Login.this, userId + " " + role, Toast.LENGTH_SHORT).show();
            }
        }

        Constants.chino = new ChinoAPI(HOST);
        toolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar)findViewById(R.id.login_progress_bar);
        console = (TextView)findViewById(R.id.console);
        login = (Button)findViewById(R.id.login_user);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        loginPatient = (Button)findViewById(R.id.login_patient_button);
        loginDoctor = (Button)findViewById(R.id.login_doctor_button);
        register = (Button)findViewById(R.id.register_button);
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (settings.getBoolean(Constants.IS_LOGGED, false)){
            loginUser(settings.getString(Constants.USERNAME, ""), settings.getString(Constants.PASSWORD, ""));
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View v = getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                loginUser(username.getText().toString(), password.getText().toString());
            }
        });
        loginPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser("Giacomo", "Password");
            }
        });
        loginDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser("Giovanni", "Password");
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });
    }

    public void loginUser(String usernameValue, String passwordValue){
        login.setVisibility(View.INVISIBLE);
        username.setVisibility(View.INVISIBLE);
        password.setVisibility(View.INVISIBLE);
        loginPatient.setVisibility(View.INVISIBLE);
        loginDoctor.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        console.setText("Checking user credential!");
        USERNAME = usernameValue;
        PASSWORD = passwordValue;
        new LoginUser().execute();
    }


    @Override
    public void onBackPressed() {
    }


    private class LoginUser extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {

                LoggedUser loggedUser = Constants.chino.auth.loginWithPassword(USERNAME, PASSWORD, Constants.APPLICATION_ID, Constants.APPLICATION_SECRET);

                Constants.user = Constants.chino.auth.checkUserStatus();

                if(Constants.user.getAttributesAsHashMap().get("role").equals("doctor")){
                    USER_ID = Constants.user.getUserId();
                    return "doctor";
                } else if(Constants.user.getAttributesAsHashMap().get("role").equals("patient")){
                    USER_ID = Constants.user.getUserId();
                    return "patient";
                } else {
                    return "error";
                }
            } catch (IOException | ChinoApiException e) {
                return e.toString();
            }
        }


        @Override
        protected void onPostExecute(String msg){
            if(msg.equals("patient") | msg.equals("doctor")) {
                chinoFCM = ChinoFCM.getInstance();
                chinoFCM.init(Constants.chino, Login.this, Constants.SERVER_KEY_DOCUMENT_ID);
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = settings.edit();
                //I use sharedPreference because the values last even if the app is closed instead of global variables that will be lost
                editor.putString(Constants.USERNAME, USERNAME);
                editor.putString(Constants.PASSWORD, PASSWORD);
                editor.putString(Constants.USER_ID, USER_ID);

                Intent intent;
                if(msg.equals("patient")) {
                    editor.putBoolean(Constants.IS_DOCTOR, false);
                    intent = new Intent(Login.this, PatientMainActivity.class);
                } else {
                    editor.putBoolean(Constants.IS_DOCTOR, true);
                    intent = new Intent(Login.this, DoctorMainActivity.class);
                }
                editor.putBoolean(Constants.IS_LOGGED, true);
                editor.apply();
                startActivity(intent);
            } else {
                login.setVisibility(View.VISIBLE);
                username.setVisibility(View.VISIBLE);
                password.setVisibility(View.VISIBLE);
                loginPatient.setVisibility(View.VISIBLE);
                loginDoctor.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                console.setText(msg);
            }
        }
    }


    /*private class AcceptRequest extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {

            } catch (IOException | ChinoApiException e) {
                return e.toString();
            }
        }


        @Override
        protected void onPostExecute(String msg){

        }
    }*/
}

