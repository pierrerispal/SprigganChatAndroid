package com.pierre.sprigganchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class Connection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        Button btn=(Button)findViewById(R.id.button_connection);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //@TODO: need to check for empty fields
                TextView nickname=(TextView)findViewById(R.id.editText_nickname);
                TextView channel = (TextView)findViewById(R.id.editText_channel);
                //Log.i("informations",nickname.getText());
                Intent i=new Intent(getApplicationContext(),Messages.class);
                i.putExtra("nickname",nickname.getText().toString());
                i.putExtra("channel",channel.getText().toString());
                startActivity(i);
            }
        });

    }
}
