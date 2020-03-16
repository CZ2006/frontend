package com.IrisBICS.lonelysg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;

public class MainActivity extends AppCompatActivity {
    RelativeLayout loginStuff, passwordSignUpBar;

    private EditText Username;
    private EditText Password;
    private Button SignIn;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            loginStuff.setVisibility(View.VISIBLE);
            passwordSignUpBar.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        loginStuff = (RelativeLayout) findViewById(R.id.loginStuff);
        passwordSignUpBar = (RelativeLayout) findViewById(R.id.passwordSignUpBar);

        ImageView logo = (ImageView)findViewById(R.id.logo);
        logo.animate().alpha(0f).setDuration(1900);

        handler.postDelayed(runnable, 2000); // Timeout for the splash

        Username = (EditText)findViewById(R.id.usernameInput);
        Password = (EditText)findViewById(R.id.passwordInput);
        SignIn = (Button)findViewById(R.id.signInButton);

        SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, LoginPage.class);
                startActivity(intent);
                // validate(Username.getText().toString(), Password.getText().toString());
            }
        });
    }

    // Username and password validation
    private void validate(String userName, String userPassword){
        if ((userName == "") && (userPassword == "")){
            Intent intent = new Intent(MainActivity.this, LoginPage.class);
            startActivity(intent);
        } // Successful login

        else{
            int counter = 0;
            counter++;
            SignIn.setEnabled(false); //Disables the button
        }
    }

}