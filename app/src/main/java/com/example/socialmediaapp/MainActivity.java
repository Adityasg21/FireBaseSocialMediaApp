package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private EditText edtEmail,edtPassword,edtUsername;
    private Button btnSignUP,btnSignIN;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtEmail=findViewById(R.id.edtEmail);
        edtPassword=findViewById(R.id.edtPassword);
        edtUsername=findViewById(R.id.edtUsername);

        btnSignIN=findViewById(R.id.btnSignIn);
        btnSignUP=findViewById(R.id.btnSignUp);
        mAuth = FirebaseAuth.getInstance();

        btnSignUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signUp();
            }
        });
        btnSignIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser= mAuth.getCurrentUser();
        if(currentUser!=null){
            transitionToSocialMedia();
        }
    }

    public void signUp(){
        mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this,"signing up successful",Toast.LENGTH_LONG).show();
                            FirebaseDatabase.getInstance().getReference()
                                    .child("my_users").child(task.getResult().getUser().getUid())
                                    .child("username").setValue(edtUsername.getText().toString());

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(edtUsername.getText().toString())
                                    .build();

                            FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(MainActivity.this,"display name updated",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            transitionToSocialMedia();
                        }else {

                            Toast.makeText(MainActivity.this,"signing up failed",Toast.LENGTH_LONG).show();
                        }

                    }
                });

    }

    public void signIn(){
        mAuth.signInWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this,"signing in success",Toast.LENGTH_LONG).show();
                            transitionToSocialMedia();
                        }else {
                            Toast.makeText(MainActivity.this,"signing in failed",Toast.LENGTH_LONG).show();

                        }
                    }
                });

    }
    public void transitionToSocialMedia(){
        Intent intent=new Intent(MainActivity.this,socialMediaActivity.class);
        startActivity(intent);
    }
}