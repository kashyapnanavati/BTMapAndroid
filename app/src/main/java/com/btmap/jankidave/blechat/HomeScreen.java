package com.btmap.jankidave.blechat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.btmap.jankidave.blechat.BLEMainActivityNew;
import com.btmap.jankidave.blechat.MainActivity;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class HomeScreen extends AppCompatActivity {
    Button b_connect, ble_connect;
    private static final String TAG = "HomeScreen";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home_screen);

        b_connect = (Button) findViewById(R.id.connect);
        b_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Starting the connection Activity\n");
                Intent Intent = new Intent(view.getContext(), MainActivity.class);
                view.getContext().startActivity(Intent);
            }
        });

        ble_connect = (Button) findViewById(R.id.bleconnect);
        ble_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Starting the BLE connection Activity\n");
                Intent Intent = new Intent(view.getContext(), BLEMainActivityNew.class);
                view.getContext().startActivity(Intent);
            }
        });

    }

}
