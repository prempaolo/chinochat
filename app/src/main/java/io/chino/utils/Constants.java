package io.chino.utils;

import io.chino.java.ChinoAPI;
import io.chino.api.user.User;

public class Constants {

    //Fill with the ids of the Resources created

    public static String APPLICATION_ID = "";
    public static String APPLICATION_SECRET = "";
    public static String USERNAME = "username";
    public static String PASSWORD = "password";
    public static String USER_ID = "user_id";
    public static String IS_DOCTOR = "is_doctor";
    public static String IS_LOGGED = "is_logged";
    public static String SUCCESS = "success";
    public static String FAIL = "fail";
    public static ChinoAPI chino;
    public static User user;
    public static String DOCTOR_USER_SCHEMA_ID = "";
    public static String PATIENT_USER_SCHEMA_ID = "";
    public static String CHAT_REPOSITORY_ID = "";
    public static String REPOSITORY_ID = "";
    public static String ROLE_SCHEMA_ID = "";
    public static String NOTIFICATIONS_SCHEMA_ID = "";
    public static String USERS_GROUP_ID = "";
    //public static String CHAT_SCHEMA_ID = "";
    public static String DOCTORS = "doctors";
    public static String PATIENTS = "patients";
    public static String FROM_USER_TO_CHAT = "users";
    public static String MESSAGE_USERNAME = "username";
    public static String MESSAGE_CONTENT = "message";
    public static String MESSAGE_TIME= "time";
    public static String MESSAGE_DATE= "date";
    public static String MESSAGE_USER_ID = "user_id";
    public static String MESSAGE_ROLE = "role";
    public static String INTENT_ADD_DOCTOR_REQUEST = "io.chino.intent_added_doctor";
    public static String INTENT_NEW_MESSAGE = "io.chino.intent_new_message";
    public static String INTENT_OPEN_CHAT = "io.chino.intent_open_chat";
    public static String SERVER_KEY_DOCUMENT_ID = "";
}
