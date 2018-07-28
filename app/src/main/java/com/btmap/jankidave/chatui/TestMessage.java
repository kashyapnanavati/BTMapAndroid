package com.btmap.jankidave.chatui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class TestMessage extends AppCompatActivity {

    //private TextView myText = null;

    /** Items entered by the user is stored in this ArrayList variable */
    ArrayList<String> mMessageList = new ArrayList<String>();

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_test_message);

        final LinearLayout lView = (LinearLayout)findViewById(R.id.LinearLayout);
        final EditText myInput = (EditText)findViewById(R.id.TextInput);
        final TextView myText = new TextView(this);

        Button send = (Button)findViewById(R.id.Send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                String input = myInput.getText().toString();
                mMessageList.add(input);

                StringBuilder sb = new StringBuilder();
                for (int counter = 0; counter < mMessageList.size(); counter++) {
                    System.out.println(mMessageList.get(counter));
                    sb.append(mMessageList.get(counter));
                    sb.append("\n");
                }

                lView.removeView(myText);
                myText.setText(sb);
                lView.addView(myText);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
