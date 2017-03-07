package io.chino;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class PatientMainActivity extends ChinoBaseActivity {

    private TextView console;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_patient_main, null, false);
        mDrawerLayout.addView(contentView, 0);
        console = (TextView)findViewById(R.id.console);
        /*Intent intent = getIntent();
        if(intent.hasExtra("document")){
            DOCUMENT_ID = intent.getStringExtra("document");
            new ReadDocument().execute();
        }*/
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if(!super.onNavigationItemSelected(item)) {
            switch (item.getItemId()) {
                default:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame_layout, ChatFragment.newInstance(   doctorsMenuList.get(item.getItemId()).getUser_id(),
                                    doctorsMenuList.get(item.getItemId()).getChat_id(),
                                    doctorsMenuList.get(item.getItemId()).getEmail(),
                                    true)).commit();
                    break;
            }
        }
            closeDrawer();
        return true;
    }
}

