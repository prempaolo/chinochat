package io.chino;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.prempaolo.simplenotification.gcm.ChinoFCM;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.chino.api.common.ChinoApiException;
import io.chino.api.document.Document;
import io.chino.api.user.User;
import io.chino.utils.Constants;

public class ChinoBaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ChatFragment.OnFragmentInteractionListener,
        AddDoctorFragment.OnFragmentInteractionListener{

    protected static final int GROUP_DOCTORS = 1;
    protected static final int GROUP_PATIENTS = 2;
    protected static final String PATIENTS = "patients";
    protected static final String DOCTORS = "doctors";

    protected SharedPreferences settings;
    protected DrawerLayout mDrawerLayout;
    protected NavigationView mDrawerView;
    protected Toolbar toolbar;
    protected Menu mMenu;

    protected List<User> doctorsMenuList;
    protected List<User> patientsMenuList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerView = (NavigationView) findViewById(R.id.left_drawer);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);

        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mDrawerView.setNavigationItemSelectedListener(this);

        if(settings.getBoolean(Constants.IS_DOCTOR, false)) {
            mDrawerView.inflateMenu(R.menu.activity_doctor_main_drawer);
        } else {
            mDrawerView.inflateMenu(R.menu.activity_patient_main_drawer);
        }
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mMenu = mDrawerView.getMenu();
        new GetUsers().execute(DOCTORS);
        if(settings.getBoolean(Constants.IS_DOCTOR, false)) {
            new GetUsers().execute(PATIENTS);
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_log_out:
                settings.edit().putBoolean(Constants.IS_LOGGED, false).apply();
                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch(item.getItemId()){
            case R.id.nav_add_doctor:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, new AddDoctorFragment()).commit();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    protected void closeDrawer(){
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onAddDoctorFragmentInteraction() {

    }

    @Override
    public void onChatFragmentInteraction() {

    }

    protected class ReadDocument extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                Document d = Constants.chino.documents.read(params[0]);
                return (String)d.getContentAsHashMap().get("message");
            } catch (IOException | ChinoApiException e) {
                e.printStackTrace();
                return e.toString();
            }
        }
    }

    //Just one class to get doctors and patients to add in the lateral drawer menu
    private class GetUsers extends AsyncTask<String, Void, List<User>> {
        private boolean doctor;
        @Override
        protected List<User> doInBackground(String... params) {
            try {
                doctor = params[0].equals(DOCTORS);
                Document d = Constants.chino.documents.read((String)Constants.user.getAttributesAsHashMap().get(params[0]));
                HashMap<String, String> users = (HashMap<String, String>) d.getContentAsHashMap().get(Constants.FROM_USER_TO_CHAT);
                List<User> usersToAdd = new ArrayList<>();
                for(String s : users.keySet()){
                    if(!s.equals(""))
                        usersToAdd.add(Constants.chino.users.read(s));
                }
                if(doctor)
                    doctorsMenuList = usersToAdd;
                else
                    patientsMenuList = usersToAdd;
                return usersToAdd;
            } catch (IOException | ChinoApiException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<User> result){
            if(result!=null){
                MenuItem usersMenu;
                if(doctor)
                    usersMenu = mMenu.findItem(R.id.doctors);
                else
                    usersMenu = mMenu.findItem(R.id.patients);
                usersMenu.getSubMenu().clear();
                for(int i=0; i<result.size(); i++){
                    if(doctor) {
                        usersMenu.getSubMenu().add(GROUP_DOCTORS, i, i, result.get(i).getUsername());
                    } else {
                        usersMenu.getSubMenu().add(GROUP_PATIENTS, i, i, result.get(i).getUsername());
                    }
                }
            }
        }
    }
}
