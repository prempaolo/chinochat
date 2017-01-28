package io.chino;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class DoctorMainActivity extends ChinoBaseActivity {

    TextView tv;
    String DOCUMENT_ID = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_doctor_main, null, false);
        mDrawerLayout.addView(contentView, 0);

        tv = (TextView)findViewById(R.id.console);
        Intent intent = getIntent();
        if(intent.hasExtra("document")){
            DOCUMENT_ID = intent.getStringExtra("document");
            new ReadDocument(){
                @Override
                protected void onPostExecute(String result){
                    tv.setText(result);
                }
            }.execute(DOCUMENT_ID);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if(!super.onNavigationItemSelected(item)) {
            switch (item.getItemId()) {
                default:
                    if (item.getGroupId() == GROUP_DOCTORS) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.frame_layout, ChatFragment.newInstance(   doctorsMenuList.get(item.getItemId()).getUser_id(),
                                                                                        doctorsMenuList.get(item.getItemId()).getChat_id(),
                                                                                        doctorsMenuList.get(item.getItemId()).getEmail(),
                                                                                        true)).commit();
                    } else if (item.getGroupId() == GROUP_PATIENTS) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.frame_layout, ChatFragment.newInstance(   patientsMenuList.get(item.getItemId()).getUser_id(),
                                                                                        patientsMenuList.get(item.getItemId()).getChat_id(),
                                                                                        patientsMenuList.get(item.getItemId()).getEmail(),
                                                                                        false)).commit();
                    }
                    break;
            }
        }
        closeDrawer();
        return true;
    }
}

