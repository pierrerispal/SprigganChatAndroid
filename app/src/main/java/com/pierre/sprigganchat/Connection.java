package com.pierre.sprigganchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Connection extends AppCompatActivity {
    private Socket socket;
    {
        try{
            socket = IO.socket("http://spriggan.fr:3000/");
        }catch(URISyntaxException e){
            //@TODO: if no connection we should block the rest of the app
            Log.i("channel","noconnection");
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        final ArrayList<String> channels = new ArrayList<>();
        final ListView listView = (ListView) findViewById(R.id.listView_channels);

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            }
        }).on("channel list", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                channels.add(args[0].toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Connection.this,
                                android.R.layout.simple_list_item_1, channels);
                        listView.setAdapter(adapter);
                    }
                });

            }
        });
        socket.connect();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String tempChannel = ((TextView) view).getText().toString();
                TextView channel = (TextView) findViewById(R.id.editText_channel);
                Log.i("channel", tempChannel);
                if (tempChannel.equals("none")) {
                    channel.setText("");
                } else {
                    channel.setText(tempChannel);
                }
            }
        });

        Button btn=(Button)findViewById(R.id.button_connection);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //@TODO: need to check for empty fields
                TextView nickname = (TextView) findViewById(R.id.editText_nickname);
                TextView channel = (TextView) findViewById(R.id.editText_channel);
                //Log.i("informations",nickname.getText());
                Intent i = new Intent(getApplicationContext(), Messages.class);
                i.putExtra("nickname", nickname.getText().toString().trim());
                i.putExtra("channel", channel.getText().toString().trim());
                startActivity(i);
            }
        });

    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        this.finish();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        socket.disconnect();
    }
}
