package com.mustaar.route;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mustaar.route.SQLConnection.ConnectionClass;
import com.mustaar.route.Session.SessionClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class LoginActivity extends AppCompatActivity {

    EditText phonenumber, password;
    Button signupbtn, loginbtn;
    Connection con;
    SessionClass sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sessionManager=new SessionClass(getApplicationContext());

        phonenumber=(EditText)findViewById(R.id.loginphone);
        password=(EditText)findViewById(R.id.loginpassword);

        signupbtn=(Button)findViewById(R.id.signupbtn1);
        loginbtn=(Button)findViewById(R.id.loginbtn1);

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoginActivity.LoginUser().execute("");
            }
        });
        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public class LoginUser extends AsyncTask<String,String,String>{
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(String s) {

        }

        @Override
        protected String doInBackground(String... strings) {
            String pno=phonenumber.getText().toString(), pass=password.getText().toString();
            con=connectionClass(ConnectionClass.ip,ConnectionClass.port,ConnectionClass.username,ConnectionClass.password,ConnectionClass.db);
            if(con!=null){
                try{
                    String query="SELECT * FROM [User] WHERE [PhoneNumber]='"+phonenumber.getText()+"' AND [Password]='"+password.getText()+"'";
                    Statement statement = con.createStatement();
                    ResultSet rs=statement.executeQuery(query);
                    if(rs.next()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this,"Login Success",Toast.LENGTH_LONG).show();
                            }
                        });
                        sessionManager.createLoginSession(pno,pass);
                        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this,"Error: Incorrect Phone number or Password",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch(Exception e){
                    Log.e("Login Error", e.getMessage());
                    Log.e("Cause", String.valueOf(e.getCause()));
                }
            }
            else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this,"Error: Check Internet Connection & try again",Toast.LENGTH_LONG).show();
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
            Log.e("Cause at Login", String.valueOf(e.getCause()));
        }
        return connection;
    }
}