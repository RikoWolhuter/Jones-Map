package com.RikoKarmenJeanMoe.jonesmap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.jonesmap.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference registerUsers = database.getReference("JonesMap");
    private FirebaseAuth mAuth;

    private GetterAndSetters getterAndsetter;
    private EditText Gmail;
    private EditText Password;
    private Button log;
    private Button reg;
    private Registration.User storedClass;

    private TextView forgotPassword;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getterAndsetter = new GetterAndSetters();

        mAuth = FirebaseAuth.getInstance();

        forgotPassword = (TextView) findViewById(R.id.RecoverPassID);


        Gmail = (EditText) findViewById(R.id.Username_input_Login);
        Password = (EditText) findViewById(R.id.Password_input_Login);


        log = findViewById(R.id.loginbtn);

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tempGmail = Gmail.getText().toString().trim();
                String tempPassword = Password.getText().toString().trim();


                if(!tempGmail.isEmpty() && !tempPassword.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(tempGmail).matches() && tempPassword.length() > 6) {
                    mAuth.signInWithEmailAndPassword(tempGmail, tempPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                if(user.isEmailVerified()){
                                    //redirect to user profile
                                    openMainPage();
                                }else{
                                    user.sendEmailVerification();
                                    Toast.makeText(Login.this, "Check your email to verify your account!", Toast.LENGTH_LONG).show();
                                }

                            }else{
                                Toast.makeText(Login.this, "Failed to login! Please check your credentials", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
                else{
                    Toast.makeText(Login.this, "Please complete all the fields", Toast.LENGTH_SHORT).show();
                }





            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                Intent intent = new Intent(Login.this,ForgotPassword.class);
                startActivity(intent);
            }
        });

        reg = findViewById(R.id.reg);

        reg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View view){
                Intent intent = new Intent(Login.this,Registration.class);
                startActivity(intent);
            }
        });


    }

    public void openMainPage() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
    }








/*
    String encrypted = "ANY_ENCRYPTED_STRING_HERE";//Insert encrypted string here
    String decrypted = "";
    try {
        decrypted = Registration.decrypt(encrypted);
        Log.d("TEST", "decrypted:" + decrypted);
    } catch (Exception e) {
        e.printStackTrace();
    }
*/

}