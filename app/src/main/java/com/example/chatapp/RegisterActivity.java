package com.example.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity

{
    private Button CreateAccountButton;
    private EditText UserEmail,UserPassword;
    private TextView AlreadyHaveAccountLink;
    private FirebaseAuth  mAuth;
    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        initializeFields();

        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CreateNewAccount();
            }
        });
    }




    private void CreateNewAccount()
    {
        String email =UserEmail.getText().toString();
        String password =UserPassword.getText().toString();
        if (TextUtils.isEmpty(email))
        {

            Toast.makeText(this,"please enter email..",Toast.LENGTH_SHORT).show();

        }

        if (TextUtils.isEmpty(password))
        {

            Toast.makeText(this,"please enter password..",Toast.LENGTH_SHORT).show();

        }
        else
        {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("please wait,while we are creating new account for you...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener( (task) ->  {
                        {
                            if (task.isSuccessful())
                            {
                                String deviceToken  = FirebaseInstanceId.getInstance().getToken();


                               String currentUserID = mAuth.getCurrentUser().getUid();
                                RootRef.child("users").child(currentUserID).setValue("");

                              RootRef.child("users").child(currentUserID).child("device_token")
                                      .setValue(deviceToken);


                                sendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this,"Account Created successfully..",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error  :" + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }
                        }
                    });
        }
    }


    private void initializeFields()
    {
        CreateAccountButton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        AlreadyHaveAccountLink =(TextView) findViewById(R.id.Already_have_account_link);
        loadingBar = new ProgressDialog(this);
    }


    private void sendUserToLoginActivity()
    {
        Intent LoginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(LoginIntent);
    }



    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}