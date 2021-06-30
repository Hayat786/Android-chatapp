package com.example.chatapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupchatActivity extends AppCompatActivity
{
    private Toolbar  mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollview;
    private TextView displayTextMessage;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef,GroupNameRef,GroupMessageKeyRef;
    private String currentGroupName,currentUserID,currentUserName,currentDate,currentTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupchat);

        currentGroupName =getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupchatActivity.this, "currentGroupName", Toast.LENGTH_SHORT).show();


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        InitializeFields();
        GetUserInfo();
        SendMessageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            SaveMessageInfoToDatabase();

            userMessageInput.setText("");


                mScrollview.fullScroll(ScrollView.FOCUS_DOWN);


            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
            if (snapshot.exists())
            {
           DisplayMessage(snapshot);

            }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists())
                {
                    DisplayMessage(snapshot);

                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void InitializeFields()
    {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);
        SendMessageButton =(ImageButton) findViewById(R.id.send_message_button);
        userMessageInput =(EditText) findViewById(R.id.input_group_message);
        displayTextMessage =(TextView)findViewById(R.id.group_chat_text_display);
        mScrollview = (ScrollView) findViewById(R.id.my_scroll_view);
    }
    private void GetUserInfo()
    {
    UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot)
        {
            if (snapshot.exists())
            {
                currentUserName = snapshot.child("name").getValue().toString();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error)
        {

        }
    });
    }

    private void SaveMessageInfoToDatabase()
    {
    String message = userMessageInput.getText().toString();
    String messageKEY = GroupNameRef.push().getKey();

    if(TextUtils.isEmpty(message))
    {
        Toast.makeText(this, "please write message first", Toast.LENGTH_SHORT).show();
    }
    else
    {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy");
        currentDate =currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime =currentTimeFormat.format(calForTime.getTime());
        HashMap<String,Object> groupMessageKey = new HashMap<>();
        GroupNameRef.updateChildren(groupMessageKey);
        GroupMessageKeyRef = GroupNameRef.child(messageKEY);

        HashMap<String,Object>messageInfoMap =new HashMap<>();

        messageInfoMap.put("name",currentUserName);
        messageInfoMap.put("message",message);
        messageInfoMap.put("date",currentDate);
        messageInfoMap.put("time",currentTime);

        GroupMessageKeyRef.updateChildren(messageInfoMap);

    }
    }
    private void DisplayMessage(DataSnapshot snapshot)
    {
        Iterator itertor = snapshot.getChildren().iterator();
        while(itertor.hasNext())
        {
            String chatDate = (String) ((DataSnapshot)itertor.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)itertor.next()).getValue();
            String chatName = (String) ((DataSnapshot)itertor.next()).getValue();
            String chatTime  = (String) ((DataSnapshot)itertor.next()).getValue();

            displayTextMessage.append(chatName + " :\n " + chatMessage + " \n" + chatTime + "  " + chatDate + "\n\n");

            mScrollview.fullScroll(ScrollView.FOCUS_DOWN);

        }

    }
}