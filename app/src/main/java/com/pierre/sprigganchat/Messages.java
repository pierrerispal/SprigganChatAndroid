package com.pierre.sprigganchat;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
            //socket = IO.socket("http://192.168.0.40:3000/");
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
        //final ListView listView = (ListView) findViewById(R.id.listView_chat);
        Bundle extras = getIntent().getExtras();
        String nickname="";
        String channel="";
        if (extras != null) {
            nickname = extras.getString("nickname");
            channel = extras.getString("channel");
        }
        final String finalNickname = nickname;
        final String finalChannel = channel;
        this.setTitle("#"+finalChannel);
        //final ArrayList<HashMap<String,String>> list_messages = new ArrayList<HashMap<String, String>>();
        final ArrayList<String> messages = new ArrayList<>();
        final TextView chat=(TextView)findViewById(R.id.textView_chat);
        TextView message = (TextView) findViewById(R.id.editText_message);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(message, InputMethodManager.SHOW_IMPLICIT);
        chat.setMovementMethod(new ScrollingMovementMethod());
        final String[] lastMessage = {""};

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
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    Log.i("channel",obj.getString("channel").trim()+ " : "+finalChannel.trim());
                    if(obj.getString("channel").equals(finalChannel)){
                        //messages.add(obj.getString("nickname") + " : " + obj.getString("msg"));
                        lastMessage[0] ="<"+obj.getString("nickname")+">" + " : " + obj.getString("msg")+"\n";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                writeMessage(chat,lastMessage[0]);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).on("connect user", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    if (obj.getString("channel").equals(finalChannel)) {
                        //messages.add(obj.getString("nickname") + " just joined");
                        lastMessage[0] = obj.getString("nickname") + " just joined" + "\n";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                writeMessage(chat,lastMessage[0]);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).on("disconnect user", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject)args[0];
                try {
                    if(obj.getString("channel").equals(finalChannel)){
                        //messages.add(obj.getString("nickname") + " just left");
                        lastMessage[0] = obj.getString("nickname") + " just left" + "\n";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                writeMessage(chat, lastMessage[0]);
                            }
                        });
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
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
    public void writeMessage(TextView chat,String lastMessage){
        chat.append(lastMessage);
        final int scrollAmount = chat.getLayout().getLineTop(chat.getLineCount()) - chat.getHeight();
        // if there is no need to scroll, scrollAmount will be <=0
        if (scrollAmount > 0)
            chat.scrollTo(0, scrollAmount);
        else
            chat.scrollTo(0, 0);
    }
}
