package com.RikoKarmenJeanMoe.jonesmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.jonesmap.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

public class Registration extends AppCompatActivity {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference registerUsers = database.getReference("JonesMap");
    private FirebaseAuth mAuth;

    private GetterAndSetters getterAndsetter;
    private EditText Username;
    private EditText Password;
    private EditText confirmPassword;
    private EditText gmail;



    public String tempUsername;
    public String tempPassword;
    public String tempconfirmPassword;
    public String tempgmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();


        getterAndsetter = new GetterAndSetters();

        Username = (EditText) findViewById(R.id.username_input);
        Password = (EditText) findViewById(R.id.password_input);
        confirmPassword = (EditText) findViewById(R.id.confirmPassword_input);
        gmail = (EditText) findViewById(R.id.gmail_input);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Button log;
        Button reg;

        log = (Button) findViewById(R.id.goBackToLogin);

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLoginPage();
            }
        });

        reg = (Button) findViewById(R.id.AddLandmark);

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                tempUsername = Username.getText().toString().trim();
                tempPassword = Password.getText().toString().trim();
                tempconfirmPassword = confirmPassword.getText().toString().trim();
                tempgmail = gmail.getText().toString().trim();


                if(!tempUsername.isEmpty() && !tempPassword.isEmpty() &&
                        !tempconfirmPassword.isEmpty() &&
                        !tempgmail.isEmpty() && tempPassword.equals(tempconfirmPassword) && Patterns.EMAIL_ADDRESS.matcher(tempgmail).matches() && tempPassword.length() > 6) {



                    Intent intent = new Intent(Registration.this,Login.class);

                    tempUsername = Username.getText().toString().trim();
                    tempPassword = Password.getText().toString().trim();
                    tempgmail = gmail.getText().toString().trim();
                    //intent.putExtra("sendUsername",tempUsername);
                    //intent.putExtra("sendPassword",tempPassword);

                    //register.setC_userName(tempUsername);
                    //register.setC_Password_1(tempPassword);
                    //registerUsers.push().setValue(register);



                    //"user "+Integer.toString(ID)

                    //writeNewUser(tempUsername, tempgmail, tempPassword);

                    mAuth.createUserWithEmailAndPassword(tempgmail, tempPassword)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        writeNewUser(tempUsername, tempgmail, tempPassword);
                                    }
                                    else{
                                        Toast.makeText(Registration.this, "User has not been created", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });




                    startActivity(intent);

                }
                else{
                    Toast.makeText(Registration.this, "Please complete all the fields, insert a valid Gmail and make sure password length is more than 6 characters", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }

    public void writeNewUser(String name, String gmail_, String password) {
        User user = new User(name, gmail_, password);

        FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(Registration.this, "User has been registered successfully!", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(Registration.this, "user has not been authenticated", Toast.LENGTH_LONG).show();
                }
            }
        });
        FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Medal_Level")
                .setValue("0").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(Registration.this, "User has been registered successfully!", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(Registration.this, "user has not been authenticated", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void openLoginPage() {
        Intent intent = new Intent(this,Login.class);
        startActivity(intent);
    }

    @IgnoreExtraProperties
    public class User{


        public String username;

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getGmail_() {
            return gmail_;
        }

        public String password;
        public String gmail_;

        public User(){

        }

        public User(String username, String gmail_, String password) {
            this.username = username;
            this.gmail_ = gmail_;
            this.password = password;
        }
    }







/*
    private static final byte[] keyValue =
            new byte[]{'c', 'o', 'd', 'i', 'n', 'g', 'a', 'f', 'f', 'a', 'i', 'r', 's', 'c', 'o', 'm'};


    public static String encrypt(String cleartext)
            throws Exception {
        byte[] rawKey = getRawKey();
        byte[] result = encrypt(rawKey, cleartext.getBytes());
        return toHex(result);
    }

    public static String decrypt(String encrypted)
            throws Exception {

        byte[] enc = toByte(encrypted);
        byte[] result = decrypt(enc);
        return new String(result);
    }

    private static byte[] getRawKey() throws Exception {
        SecretKey key = new SecretKeySpec(keyValue, "AES");
        byte[] raw = key.getEncoded();
        return raw;
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKey skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] encrypted)
            throws Exception {
        SecretKey skeySpec = new SecretKeySpec(keyValue, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }

    String encrypted = "";
    String sourceStr = "This is any source string";//Insert password string into here
        try {
        encrypted = Registration.encrypt(sourceStr);
        Log.d("TEST", "encrypted:" + encrypted);
    } catch {

    }
*/

}