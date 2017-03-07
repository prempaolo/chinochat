package io.chino;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.chino.java.ChinoAPI;
import io.chino.api.common.ChinoApiException;
import io.chino.api.document.Document;
import io.chino.api.permission.PermissionRule;
import io.chino.api.permission.PermissionRuleCreatedDocument;
import io.chino.api.permission.PermissionValues;
import io.chino.api.repository.Repository;
import io.chino.api.schema.Schema;
import io.chino.api.schema.SchemaRequest;
import io.chino.api.schema.SchemaStructure;
import io.chino.api.user.User;
import io.chino.api.common.Field;
import io.chino.api.userschema.UserSchema;
import io.chino.api.userschema.UserSchemaStructure;
import io.chino.utils.Constants;

public class RegistrationActivity extends AppCompatActivity {

    private final static String HOST = "https://api.test.chino.io/v1";

    //They simulate a server
    private final static String CUSTOMER_ID = "<your-customer-id>";
    private final static String CUSTOMER_KEY = "<your-customer-key>";
    String NAME;
    String LAST_NAME;
    String EMAIL;
    String PASSWORD;
    EditText name;
    EditText lastName;
    EditText email;
    Button addDoctor;
    Button addPatient;
    Button reset;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Constants.chino = new ChinoAPI(HOST);
        toolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        name = (EditText)findViewById(R.id.register_name);
        lastName = (EditText)findViewById(R.id.register_last_name);
        email = (EditText)findViewById(R.id.register_email);
        addDoctor = (Button)findViewById(R.id.add_doctor);
        addPatient = (Button)findViewById(R.id.add_patient);
        reset = (Button)findViewById(R.id.reset);
        addPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUser("patient");
            }
        });
        addDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUser("doctor");
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Reset().execute();
            }
        });
    }

    private void checkUser(String role){
        if(name.getText()==null || name.getText().toString().equals("") ||
                lastName.getText()==null || lastName.getText().toString().equals("") ||
                email.getText()==null || email.getText().toString().equals("")){
            Toast.makeText(RegistrationActivity.this, "Missing data, please fill all the fields!", Toast.LENGTH_SHORT).show();
        } else {
            NAME = name.getText().toString();
            LAST_NAME = lastName.getText().toString();
            EMAIL = email.getText().toString();
            PASSWORD = "Password";
            new CreateUser().execute(role);
        }
    }

    private class CreateUser extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if(params[0].equals("doctor")) {
                try {
                    createUser(NAME, LAST_NAME, EMAIL, PASSWORD, params[0], Constants.DOCTOR_USER_SCHEMA_ID);
                    return Constants.SUCCESS;
                } catch (ChinoApiException | IOException e) {
                    return e.toString();
                }
            } else {
                try {
                    createUser(NAME, LAST_NAME, EMAIL, PASSWORD, params[0], Constants.PATIENT_USER_SCHEMA_ID);
                    return Constants.SUCCESS;
                } catch (ChinoApiException | IOException e) {
                    return e.toString();
                }
            }
        }


        @Override
        protected void onPostExecute(String result){
            if(result.equals(Constants.SUCCESS)) {
                Toast.makeText(RegistrationActivity.this, "User created successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_SHORT).show();
            }

        }

    }

    private class Reset extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                createAllSchemas();
                return "Reset successful!";
            } catch (ChinoApiException | IOException e) {
                return e.toString();
            }
        }


        @Override
        protected void onPostExecute(String result){
            Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_SHORT).show();
        }

    }

    String schemaUsersId;

    public void createUser(String name, String lastName, String email, String password, String role, String userSchemaId) throws IOException, ChinoApiException {
        ChinoAPI chinoTemp = new ChinoAPI(HOST, CUSTOMER_ID, CUSTOMER_KEY);
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("role", role);
        attributes.put("tokenFCM", "");
        attributes.put("name", name);
        attributes.put("last_name", lastName);
        HashMap<String, Object> content = new HashMap<>();
        HashMap<String, String> temp = new HashMap<>();
        temp.put("", "");
        content.put("users", temp);
        //Document doctors = chinoTemp.documents.create(Constants.ROLE_SCHEMA_ID, content);
        SchemaStructure schemaStructure = new SchemaStructure();
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("user_id", "string"));
        fields.add(new Field("chat_id", "string"));
        fields.add(new Field("email", "string"));
        schemaStructure.setFields(fields);

        //TODO: Settare permissions in modo che chi crea lo schema possa fare la list, e gli altri no
        Schema doctors = chinoTemp.schemas.create(Constants.REPOSITORY_ID, "doctorsAddedBy:"+email, schemaStructure);
        schemaUsersId = doctors.getSchemaId();
        attributes.put("doctors", doctors.getSchemaId());


        if(role.equals("doctor")){
            schemaStructure = new SchemaStructure();
            fields = new ArrayList<>();
            fields.add(new Field("user_id", "string"));
            fields.add(new Field("chat_id", "string"));
            schemaStructure.setFields(fields);
            Schema patients = chinoTemp.schemas.create(Constants.REPOSITORY_ID, "patientsAddedBy:"+email, schemaStructure);
            attributes.put("patients", patients.getSchemaId());
        }
        User user = chinoTemp.users.create(email, password, attributes, userSchemaId);
        giveAllPermsToUser(user.getUserId());

    }

    private void giveAllPermsToUser(String userId) throws IOException, ChinoApiException {
        ChinoAPI chinoTemp = new ChinoAPI(HOST, CUSTOMER_ID, CUSTOMER_KEY);
        /*
        List<User> users = chinoTemp.users.list(0, Constants.DOCTOR_USER_SCHEMA_ID).getUsers();
        for(User u : users){
            chinoTemp.users.delete(u.getUserId(), true);
        }
        List<User> users2 = chinoTemp.users.list(0, Constants.PATIENT_USER_SCHEMA_ID).getUsers();
        for(User u : users2){
            chinoTemp.users.delete(u.getUserId(), true);
        }
        */
        PermissionRule rule = new PermissionRule();
        //rule.setAuthorize("R", "L", "A");
        rule.setManage("R", "L");
        //chinoTemp.permissions.permissionsOnResources(PermissionValues.GRANT, PermissionValues.USER_SCHEMAS, PermissionValues.USERS, userId, rule);
        chinoTemp.permissions.permissionsOnResourceChildren(PermissionValues.GRANT, PermissionValues.USER_SCHEMAS, Constants.DOCTOR_USER_SCHEMA_ID, PermissionValues.USERS, PermissionValues.USERS, userId, rule);
        chinoTemp.permissions.permissionsOnResourceChildren(PermissionValues.GRANT, PermissionValues.USER_SCHEMAS, Constants.PATIENT_USER_SCHEMA_ID, PermissionValues.USERS, PermissionValues.USERS, userId, rule);

        rule = new PermissionRule();
        rule.setManage("R", "U", "D");
        chinoTemp.permissions.permissionsOnaResource(PermissionValues.GRANT, PermissionValues.USERS, userId, PermissionValues.USERS, userId, rule);
        //only create
        //chinoTemp.permissions.permissionsOnResourceChildren(PermissionValues.GRANT, PermissionValues.REPOSITORIES, Constants.REPOSITORY_ID, PermissionValues.SCHEMAS, PermissionValues.USERS, userId, rule);

        PermissionRuleCreatedDocument ruleCreatedDocument = new PermissionRuleCreatedDocument();
        rule = new PermissionRule();
        rule.setManage("R", "U", "D");
        ruleCreatedDocument.setManage("C");
        ruleCreatedDocument.setCreatedDocument(rule);
        chinoTemp.permissions.permissionsOnResourceChildren(PermissionValues.GRANT, PermissionValues.SCHEMAS, Constants.NOTIFICATIONS_SCHEMA_ID, PermissionValues.DOCUMENTS, PermissionValues.USERS, userId, ruleCreatedDocument);
        chinoTemp.permissions.permissionsOnResourceChildren(PermissionValues.GRANT, PermissionValues.SCHEMAS, Constants.ROLE_SCHEMA_ID, PermissionValues.DOCUMENTS, PermissionValues.USERS, userId, ruleCreatedDocument);
        //chinoTemp.permissions.permissionsOnResourceChildren(PermissionValues.GRANT, PermissionValues.SCHEMAS, Constants.CHAT_SCHEMA_ID, PermissionValues.DOCUMENTS, PermissionValues.USERS, userId, rule);

        rule = new PermissionRule();
        rule.setManage("C");
        chinoTemp.permissions.permissionsOnResourceChildren(PermissionValues.GRANT, PermissionValues.SCHEMAS, schemaUsersId, PermissionValues.DOCUMENTS, PermissionValues.GROUPS, Constants.USERS_GROUP_ID, rule);
        chinoTemp.permissions.permissionsOnResourceChildren(PermissionValues.GRANT, PermissionValues.SCHEMAS, schemaUsersId, PermissionValues.DOCUMENTS, PermissionValues.GROUPS, Constants.USERS_GROUP_ID, rule);

        rule = new PermissionRule();
        rule.setManage("C", "L", "U", "D", "R");
        rule.setAuthorize("C", "L", "U", "D", "R");
        chinoTemp.permissions.permissionsOnResourceChildren(PermissionValues.GRANT, PermissionValues.REPOSITORIES, Constants.CHAT_REPOSITORY_ID, PermissionValues.SCHEMAS, PermissionValues.USERS, userId, rule);
        chinoTemp.permissions.permissionsOnResourceChildren(PermissionValues.GRANT, PermissionValues.REPOSITORIES, Constants.REPOSITORY_ID, PermissionValues.SCHEMAS, PermissionValues.USERS, userId, rule);

        chinoTemp.permissions.permissionsOnResourceChildren(PermissionValues.GRANT, PermissionValues.SCHEMAS, schemaUsersId, PermissionValues.DOCUMENTS, PermissionValues.USERS, userId, rule);

        rule = new PermissionRule();
        rule.setManage("R");
        chinoTemp.permissions.permissionsOnaResource(PermissionValues.GRANT, PermissionValues.DOCUMENTS, Constants.SERVER_KEY_DOCUMENT_ID, PermissionValues.GROUPS, Constants.USERS_GROUP_ID, rule);
    }

    private void createAllSchemas() throws IOException, ChinoApiException {
        ChinoAPI chinoTemp = new ChinoAPI(HOST, CUSTOMER_ID, CUSTOMER_KEY);
        deleteAll(chinoTemp);
        UserSchemaStructure structure = new UserSchemaStructure();
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("name", "string", true));
        fields.add(new Field("last_name", "string", true));
        fields.add(new Field("role", "string"));
        fields.add(new Field("doctors", "string"));
        fields.add(new Field("tokenFCM", "string"));
        structure.setFields(fields);
        chinoTemp.userSchemas.create("Patients", structure);
        fields.add(new Field("patients", "string"));
        structure.setFields(fields);
        chinoTemp.userSchemas.create("Doctors", structure);
        Repository repo = chinoTemp.repositories.create("ChinoAndroid");
        chinoTemp.repositories.create("ChatRepository");
        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setDescription("RoleSchema");
        schemaRequest.addSchemaField("users", "json");
        chinoTemp.schemas.create(repo.getRepositoryId(), schemaRequest);
        schemaRequest = new SchemaRequest();
        schemaRequest.addSchemaField("content", "json");
        schemaRequest.setDescription("Notifications");
        chinoTemp.schemas.create(repo.getRepositoryId(), schemaRequest);
        //chinoTemp.groups.create("Users", new HashMap());
       /* schemaRequest = new SchemaRequest();
        schemaRequest.addSchemaField("username", "string");
        schemaRequest.addSchemaField("message", "string");
        schemaRequest.addSchemaField("date", "date");
        schemaRequest.addSchemaField("time", "time");
        schemaRequest.addSchemaField("user_id", "string");
        schemaRequest.addSchemaField("role", "string");
        schemaRequest.setDescription("ChatSchema");
        chinoTemp.schemas.create(repo.getRepositoryId(), schemaRequest);*/
    }

    private void deleteAll(ChinoAPI temp) throws IOException, ChinoApiException {
        List<Repository> repositories = temp.repositories.list().getRepositories();
        for(Repository r : repositories){
            List<Schema> schemas = temp.schemas.list(r.getRepositoryId()).getSchemas();
            for(Schema s : schemas){
                List<Document> documents = temp.documents.list(s.getSchemaId()).getDocuments();
                for(Document d : documents){
                    temp.documents.delete(d.getDocumentId(), true);
                }
                temp.schemas.delete(s.getSchemaId(), true);
            }
            temp.repositories.delete(r.getRepositoryId(), true);
        }
        List<UserSchema> userSchemas = temp.userSchemas.list().getUserSchemas();
        for(UserSchema u : userSchemas){
            List<User> users = temp.users.list(u.getUserSchemaId()).getUsers();
            for(User user : users){
                temp.users.delete(user.getUserId(), true);
            }
            temp.userSchemas.delete(u.getUserSchemaId(), true);
        }
    }
}

