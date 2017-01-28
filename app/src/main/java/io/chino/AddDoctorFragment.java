package io.chino;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.chino.api.common.ChinoApiException;
import io.chino.api.common.Field;
import io.chino.api.document.Document;
import io.chino.api.permission.PermissionRule;
import io.chino.api.permission.PermissionRuleCreatedDocument;
import io.chino.api.permission.PermissionValues;
import io.chino.api.schema.Schema;
import io.chino.api.schema.SchemaStructure;
import io.chino.api.user.User;
import com.prempaolo.simplenotification.gcm.ChinoFCM;
import io.chino.utils.Constants;
import io.chino.utils.DoctorItem;
import io.chino.utils.DoctorsAdapter;

public class AddDoctorFragment extends Fragment {

    private List<DoctorItem> doctors;

    private LinearLayout searchLinearLayout;
    private ListView doctorListView;
    private Button addDoctor;
    private Button search;
    private EditText doctorName;
    private EditText doctorLastName;
    private OnFragmentInteractionListener mListener;
    private User doctor;
    private SharedPreferences settings;

    public AddDoctorFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        View view = inflater.inflate(R.layout.fragment_add_doctor, container, false);
        searchLinearLayout = (LinearLayout)view.findViewById(R.id.search_linear_layout);
        doctorListView = (ListView)view.findViewById(R.id.doctor_list_view);
        addDoctor = (Button)view.findViewById(R.id.doctor_info_send_request);
        search = (Button)view.findViewById(R.id.search_button);
        doctorName = (EditText)view.findViewById(R.id.doctor_name);
        doctorLastName = (EditText)view.findViewById(R.id.doctor_last_name);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = doctorName.getText().toString();
                String lastName = doctorLastName.getText().toString();
                new SearchDoctor().execute(name, lastName);
            }
        });
        doctorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DoctorItem item = doctors.get(i);
                doctor = item.getUser();
            }
        });
        addDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendMessage().execute();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onAddDoctorFragmentInteraction();
    }

    private class SearchDoctor extends AsyncTask<String, Void, List<User>> {
        @Override
        protected List<User> doInBackground(String... params) {
            try {
                /*GetUsersResponse users = Constants.chino.users.list(0, Constants.DOCTOR_USER_SCHEMA_ID);
                doctor = null;
                for(User u : users.getUsers()){
                    if(u.getUsername().equals(params[0])){
                        doctor = Constants.chino.users.read(u.getUserId());
                        return doctor.toString();
                    }
                }*/
                return Constants.chino.search.where("name").eq(params[0]).and("last_name").eq(params[1]).sortAscBy("name").searchUsers(Constants.DOCTOR_USER_SCHEMA_ID).getUsers();
            } catch (IOException | ChinoApiException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<User> result){
            if(result!=null){
                searchLinearLayout.setVisibility(View.GONE);
                List<DoctorItem> items = new ArrayList<>();
                for(User u : result){
                    HashMap<String, Object> attributes = u.getAttributesAsHashMap();
                    DoctorItem item = new DoctorItem();
                    item.setEmail(u.getUsername());
                    item.setName((String) attributes.get("name"));
                    item.setLastName((String) attributes.get("last_name"));
                    item.setUser(u);
                    items.add(item);
                }
                doctors = items;
                DoctorsAdapter adapter = new DoctorsAdapter(getActivity(), items);
                doctorListView.setAdapter(adapter);
                addDoctor.setVisibility(View.VISIBLE);
            }

        }
    }


    private class SendMessage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                if(settings.getBoolean(Constants.IS_DOCTOR, false))
                    return sendMessageAux("doctors");
                else
                    return sendMessageAux("patients");
            } catch (IOException | ChinoApiException e) {
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result){
            Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
        }
    }

    protected String sendMessageAux(String role) throws IOException, ChinoApiException {

        if(doctor.getUserId().equals(Constants.user.getUserId())){
            return "You can't add yourself!";
        }
        List<Document> doctorsAdded = Constants.chino.documents.list((String)Constants.user.getAttributesAsHashMap().get("doctors")).getDocuments();
        for(Document d : doctorsAdded){
            if(d.getContentAsHashMap().get("user_id").equals(doctor.getUserId())){
                return "Doctor already added!";
            }
        }

        PermissionRule rule = new PermissionRule();
        rule.setManage("C", "R", "U", "D", "L");

        SchemaStructure structure = new SchemaStructure();
        List<Field> fieldList = new ArrayList<>();
        fieldList.add(new Field("message", "string"));
        fieldList.add(new Field("username", "string", true));
        fieldList.add(new Field("user_id", "string", true));
        fieldList.add(new Field("role", "string"));
        fieldList.add(new Field("date", "date"));
        fieldList.add(new Field("time", "time"));
        structure.setFields(fieldList);

        Schema chat = Constants.chino.schemas.create(Constants.CHAT_REPOSITORY_ID, settings.getString(Constants.USER_ID, "")+":"+doctor.getUserId(), structure);
        Constants.chino.permissions.permissionsOnResourceChildren(PermissionValues.GRANT,
                PermissionValues.SCHEMAS,
                chat.getSchemaId(),
                PermissionValues.DOCUMENTS,
                PermissionValues.USERS,
                doctor.getUserId(),
                rule);
        HashMap<String, Object> content = new HashMap<>();
        content.put("user_id", Constants.user.getUserId());
        content.put("chat_id", chat.getSchemaId());
        content.put("email", Constants.user.getUsername());

        String users_schema_id = (String)doctor.getAttributesAsHashMap().get(role);
        Constants.chino.documents.create(users_schema_id, content);

        content = new HashMap<>();
        content.put("user_id", doctor.getUserId());
        content.put("chat_id", chat.getSchemaId());
        content.put("email", doctor.getUsername());
        users_schema_id = (String)Constants.user.getAttributesAsHashMap().get("doctors");
        Constants.chino.documents.create(users_schema_id, content);

        content = new HashMap<>();
        String roleString;
        if(role.equals("patients")) {
            roleString = "patient";
        } else {
            roleString = "doctor";
        }
        content.put("message", "A new "+roleString+" added you!");
        content.put("document_id", "");
        HashMap<String, HashMap<String, Object>> contentWrapper = new HashMap<>();
        contentWrapper.put("content", content);
        Document doc = Constants.chino.documents.create(Constants.NOTIFICATIONS_SCHEMA_ID, contentWrapper);
        ChinoFCM.getInstance().setCustomNotification(createCustomNotification());
        ChinoFCM.getInstance().shareDocument(doc.getDocumentId(), doctor.getUserId());
        ChinoFCM.getInstance().setCustomMessage("A new "+roleString+" added you!");

        return "Doctor added successfully!";
    }

    private NotificationCompat.Builder createCustomNotification(){
        Intent intent = new Intent(getContext(), Login.class);
        intent.putExtra(Constants.INTENT_ADD_DOCTOR_REQUEST, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        return new NotificationCompat.Builder(getContext())
                .setSmallIcon(com.prempaolo.simplenotification.R.drawable.logo_notification)
                .setContentTitle("ChinoChat")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
    }
}