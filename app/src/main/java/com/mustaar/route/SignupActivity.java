package com.mustaar.route;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mustaar.route.SQLConnection.ConnectionClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SignupActivity extends AppCompatActivity {

    EditText phoneNumber, password;
    Button signUpBtn, loginBtn;
    Connection con;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        phoneNumber=(EditText)findViewById(R.id.phone);
        password=(EditText)findViewById(R.id.password);
        signUpBtn=(Button)findViewById(R.id.signupbtn);
        loginBtn=(Button)findViewById(R.id.loginbtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phoneNumber.getText().length()==0||password.getText().length()==0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SignupActivity.this,"Error: Fields cannot be empty !",Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    new SignupActivity.SignupUser().execute("");
                }
            }
        });
    }

    public class SignupUser extends AsyncTask<String, String, String>{

        String msg="";
        Boolean success=false;

        @Override
        protected void onPreExecute(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SignupActivity.this,"Processing . . .",Toast.LENGTH_LONG).show();
                }
            });
        }
        @Override
        protected void onPostExecute(String s){
            success=true;
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                con=connectionClass(ConnectionClass.ip, ConnectionClass.port, ConnectionClass.username, ConnectionClass.password, ConnectionClass.db);
                if(con==null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SignupActivity.this,"Error: Check Internet Connection & try again",Toast.LENGTH_LONG).show();
                        }
                    });
                    return null;
                }
                else{
                    String insert = "INSERT INTO [User] ([PhoneNumber], [Password]) VALUES ('"+ phoneNumber.getText()+"','" + password.getText() +"')";
                    Statement statement=con.createStatement();
                    statement.executeUpdate(insert);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SignupActivity.this,"Registration Successful ! ! !",Toast.LENGTH_LONG).show();
                        }
                    });
                    phoneNumber.setText("");
                    password.setText("");
                }
            }catch (Exception e) {
                success=false;
                Log.e("Registration Error", e.getMessage());
                Log.e("Cause", String.valueOf(e.getCause()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SignupActivity.this,"Error: Phone Number already exists",Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }

    }

    @SuppressLint("NewApi")
    public Connection connectionClass(String ip, String port, String un, String pwd, String db) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String url = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            url="jdbc:jtds:sqlserver://"+ip+":"+port+";databaseName="+db+";user="+un+";password="+pwd+";";
            connection= DriverManager.getConnection(url);

        } catch (Exception e) {
            Log.e("Sql Connection Error", e.getMessage());
            Log.e("Cause at Signup", String.valueOf(e.getCause()));
        }
        return connection;
    }
}