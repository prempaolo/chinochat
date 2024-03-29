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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.prempaolo.chino_notifications.fcm.ChinoFCM;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import io.chino.api.common.ChinoApiException;
import io.chino.api.document.Document;
import io.chino.models.Message;
import io.chino.utils.Constants;
import io.chino.utils.MyMessageAdapter;


public class ChatFragment extends Fragment {
    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_CHAT_ID = "chat_id";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_DOCTOR = "doctor";

    private String USER_ID;
    private String CHAT_ID;
    private String EMAIL;
    private boolean DOCTOR;
    private SharedPreferences settings;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MyMessageAdapter mAdapter;
    private ProgressBar mProgressBar;
    private Button sendButton;
    private EditText messageEditText;

    private OnFragmentInteractionListener mListener;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(String user_id, String chat_id, String email, boolean doctor) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, user_id);
        args.putString(ARG_CHAT_ID, chat_id);
        args.putString(ARG_EMAIL, email);
        args.putBoolean(ARG_DOCTOR, doctor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            USER_ID = getArguments().getString(ARG_USER_ID);
            CHAT_ID = getArguments().getString(ARG_CHAT_ID);
            EMAIL = getArguments().getString(ARG_EMAIL);
            DOCTOR = getArguments().getBoolean(ARG_DOCTOR);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        messageEditText = (EditText) view.findViewById(R.id.messageEditText);
        sendButton = (Button) view.findViewById(R.id.sendButton);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.messageRecyclerView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageEditText.getText().toString();
                if(!message.equals("")) {
                    messageEditText.setText("");
                    View v = getActivity().getCurrentFocus();
                    if (v != null) {
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    new SendMessage().execute(message);
                }
            }
        });

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(EMAIL);
        new RetrieveChat().execute();

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
        // TODO: Update argument type and name
        void onChatFragmentInteraction();
    }

    private class RetrieveChat extends AsyncTask<Void, Void, List<Message>> {
        @Override
        protected List<Message> doInBackground(Void... params) {
            try {
                List<Document> chat = Constants.chino.documents.list(CHAT_ID, true).getDocuments();
                List<Message> chatMessages = new ArrayList<>();
                for(Document d : chat){
                    Message message = new Message();
                    message.setName((String) d.getContentAsHashMap().get(Constants.MESSAGE_USERNAME));
                    message.setText((String) d.getContentAsHashMap().get(Constants.MESSAGE_CONTENT));
                    message.setTime((String) d.getContentAsHashMap().get(Constants.MESSAGE_TIME));
                    message.setDate((String) d.getContentAsHashMap().get(Constants.MESSAGE_DATE));
                    chatMessages.add(message);
                }
                return chatMessages;
            } catch (ChinoApiException | IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Message> result){
            if(result!=null){
                mAdapter = new MyMessageAdapter(result);
                mRecyclerView.setAdapter(mAdapter);
            }
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }

    private class SendMessage extends AsyncTask<String, Void, String> {
        private String message;
        private String date;
        private String time;

        @Override
        protected String doInBackground(String... params) {
            try {
                message = params[0];
                HashMap<String, Object> content = new HashMap<>();
                content.put(Constants.MESSAGE_USERNAME, Constants.user.getUsername());
                content.put(Constants.MESSAGE_CONTENT, message);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                date = df.format(Calendar.getInstance().getTime());
                content.put(Constants.MESSAGE_DATE, date);
                df = new SimpleDateFormat("HH:mm:ss");
                time = df.format(Calendar.getInstance().getTime());
                content.put(Constants.MESSAGE_TIME, time);
                content.put(Constants.MESSAGE_USER_ID, USER_ID);
                if(DOCTOR){
                    content.put(Constants.MESSAGE_ROLE, "doctor");
                } else {
                    content.put(Constants.MESSAGE_ROLE, "patient");
                }
                return Constants.chino.documents.create(CHAT_ID, content).getDocumentId();
            } catch (ChinoApiException | IOException e) {
                return Constants.FAIL;
            }
        }

        @Override
        protected void onPostExecute(String result){
            if(!result.equals(Constants.FAIL)){
                ChinoFCM chinoFCM = ChinoFCM.getInstance();
                chinoFCM.setCustomNotification(createCustomNotification());
                chinoFCM.shareDocument(result, USER_ID);
                Message m = new Message();
                m.setText(message);
                m.setName(Constants.user.getUsername());
                m.setDate(date);
                m.setTime(time);
                mAdapter.addItem(m);
                //Toast.makeText(getActivity(), "Message sent successfully!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private NotificationCompat.Builder createCustomNotification(){
        Intent intent = new Intent(getContext(), Login.class);
        intent.putExtra(Constants.INTENT_NEW_MESSAGE, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        return new NotificationCompat.Builder(getContext())
                .setSmallIcon(com.prempaolo.chino_notifications.R.drawable.logo_notification)
                .setContentTitle("ChinoChat")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
    }
}
