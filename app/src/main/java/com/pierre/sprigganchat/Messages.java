package com.pierre.sprigganchat;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Messages extends AppCompatActivity {
    private Socket socket;
    {
        try{
            socket = IO.socket("http://spriggan.fr:3000/");
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
         * =========== INITIALIZATION ===========
         */
        final ListView listView = (ListView) findViewById(R.id.listView_chat);
        Bundle extras = getIntent().getExtras();
        String nickname="";
        String channel="";
        if (extras != null) {
            nickname = extras.getString("nickname");
            channel = extras.getString("channel");
        }
        final String finalNickname = nickname;
        final String finalChannel = channel;
        //final ArrayList<HashMap<String,String>> list_messages = new ArrayList<HashMap<String, String>>();
        final ArrayList<String> messages = new ArrayList<>();

         /*
         * =========== SOCKET LISTENING ===========
         */
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("nickname", finalNickname);
                    obj.put("channel", finalChannel);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("connect user", obj);
            }
        }).on("chat message", new Emitter.Listener() {
            //@TODO: check the channel
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                //Log.i("json", obj.toString());
                //ListView listView=(ListView)findViewById(R.id.listView_messages);
                /*HashMap<String, String> ligne = new HashMap<String, String>();
                try{
                    ligne.put("nickname",obj.getString("pseudo"));
                    ligne.put("message",obj.getString("msg"));
                }catch (JSONException e){
                    e.printStackTrace();
                }*/
                try {
                    Log.i("channel",obj.getString("channel").trim()+ " : "+finalChannel.trim());
                    if(obj.getString("channel").equals(finalChannel)){
                        messages.add(obj.getString("nickname") + " : " + obj.getString("msg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Messages.this,
                                android.R.layout.simple_list_item_1, messages);
                        listView.setAdapter(adapter);
                        listView.setSelection(adapter.getCount() - 1);
                    }
                });
            }
        }).on("connect user", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject)args[0];
                try {
                    if(obj.getString("channel").equals(finalChannel)){
                        messages.add(obj.getString("nickname") + " just joined");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Messages.this,
                                android.R.layout.simple_list_item_1, messages);
                        listView.setAdapter(adapter);
                        listView.setSelection(adapter.getCount() -1);
                    }
                });
            }
        }).on("disconnect user", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject)args[0];
                try {
                    if(obj.getString("channel").equals(finalChannel)){
                        messages.add(obj.getString("nickname") + " just left");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Messages.this,
                                android.R.layout.simple_list_item_1, messages);
                        listView.setAdapter(adapter);
                        listView.setSelection(adapter.getCount() -1);
                    }
                });
            }
        });
        socket.connect();

        /*
         * =========== SOCKET EMMITING ===========
         */
        ImageButton send = (ImageButton) findViewById(R.id.imageButton_send);
        send.setOnClickListener(new View.OnClickListener()

                                {
                                    @Override
                                    public void onClick(View v) {
                                        TextView message = (TextView) findViewById(R.id.editText_message);
                                        JSONObject send = new JSONObject();
                                        try {
                                            send.put("channel", finalChannel);
                                            send.put("msg", message.getText().toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        Log.i("json", send.toString());
                                        socket.emit("chat message", send);
                                        message.setText("");
                                    }
                                }

        );
        }

    }
