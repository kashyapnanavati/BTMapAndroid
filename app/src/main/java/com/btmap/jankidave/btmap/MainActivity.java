package com.btmap.jankidave.btmap;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import java.util.Set;
import android.widget.Toast;
import android.widget.ArrayAdapter;

public class MainActivity extends AppCompatActivity {

    Button b_on, b_off, b_list_devices, b_get_visible;
    TextView listItem;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice>pairedDevices;
    ListView lv, lva;
    private static final String TAG = "MainActivity";
    private BluetoothGatt mBluetoothGatt;
    private String mBluetoothDeviceAddress;
    private IntentFilter filter2 = new IntentFilter();
    private String disconnectBT;
    final ArrayList list = new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b_on = (Button) findViewById(R.id.turnon);
        b_off =(Button)findViewById(R.id.turnoff);
        b_list_devices =(Button)findViewById(R.id.listD);
        b_get_visible =(Button)findViewById(R.id.visible);
        listItem =(TextView)findViewById(R.id.ListItem);

        BA = BluetoothAdapter.getDefaultAdapter();
        lv = (ListView)findViewById(R.id.listView);

        Button start = (Button)findViewById(R.id.Start);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Start New Activity \n");
                //Intent Intent = new Intent(view.getContext(), TestMessage.class);
                Intent Intent = new Intent(view.getContext(), Message.class);
                view.getContext().startActivity(Intent);
            }
        });

        filter2.addAction(BluetoothDevice.ACTION_FOUND);
        filter2.addAction(BA.ACTION_DISCOVERY_STARTED);
        filter2.addAction(BA.ACTION_DISCOVERY_FINISHED);
        filter2.addAction(BA.ACTION_SCAN_MODE_CHANGED);
        filter2.addAction(BA.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mReceiver, filter2);

        /* Asking permission at runtime */
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        disconnectGatt(mBluetoothGatt);
        super.onDestroy();
    }

    public void on(View v){
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }

    public void off(View v){
        BA.disable();
        Toast.makeText(getApplicationContext(), "Turned off" ,Toast.LENGTH_LONG).show();
        disconnectGatt(mBluetoothGatt);
    }


    public  void visible(View v){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }


    public void list(View v){
        pairedDevices = BA.getBondedDevices();

        ArrayList list = new ArrayList();
        list.clear();
        BA.cancelDiscovery();
        for(BluetoothDevice bt : pairedDevices) {
            //list.add(bt.getName());
            String devicename = bt.getName();
            String macAddress = bt.getAddress();
            list.add(devicename+"\n"+macAddress);
        }
        Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);
        listItem.setText("Paired Device :");

        //TODO Fix this for connection
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Toast.makeText(getApplicationContext(), "Start Connecting...", Toast.LENGTH_LONG).show();
                Log.v(TAG, "Start connection with new Device \n");
                Log.v(TAG, "id = " + id + "\n");
                Log.v(TAG, "position = " + position + "\n");

                String info = ((TextView) view).getText().toString();
                Log.v(TAG, "device info = " + info + "\n");
                //get the device address when click the device item
                String address = info.substring(info.length() - 17);
                Log.v(TAG, "device Address = " + address + "\n");
                //TODO: Enable Connection Later - Testing is difficult
                /*
                if (!connect(address)) {
                    Log.e(TAG, "Cannot connect to BLE Device\n");
                } else {
                    Log.i(TAG, "Connected to device ---> " + info);
                }
                */

            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                lv.setOnItemLongClickListener(null);
                Toast.makeText(getApplicationContext(), "Long click : Open Dialog to unpair the device", Toast.LENGTH_LONG).show();
                String info = ((TextView) view).getText().toString();
                Log.v(TAG, "device info = " + info + "\n");
                //get the device address when click the device item
                String address = info.substring(info.length() - 17);
                Log.v(TAG, "device Address = " + address + "\n");
                CreateDialog(address);
                return true;
            }
        });


    }

    void CreateDialog(final String btDeviceAddress) {

        Log.v(TAG, "Create Dialog called" +"\n");

        // custom dialog
        //final Dialog dialog = new Dialog(this);
        //dialog.setContentView(R.layout.unpairedialog);
        //dialog.setTitle("Paired Device");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("" + btDeviceAddress);

        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.unpairedialog, null);
        builder.setView(customLayout);

        // set the custom dialog components - text, image and button
        //TextView text = (TextView) dialog.findViewById(R.id.unpairtext);
        //text.setText("Do you want to forgot the device ?");
        TextView text = (TextView) customLayout.findViewById(R.id.unpairtext);
        text.setText("Do you want to remove the device ?");

        final AlertDialog dialog = builder.create();

        Button dialogButton = (Button) customLayout.findViewById(R.id.forget);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Un Pairing the device...");
                if (UnPair(btDeviceAddress) == true) {
                    Log.v(TAG, "Device Unpaired Successfully !");
                } else {
                    Log.e(TAG, "Exception un pairing the device");
                }
                dialog.dismiss();
            }
        });

        //dialog.show();
        // create and show the alert dialog

        dialog.show();
    }


    public void listAvailable(View v) {

        if (!BA.enable()) {
            BA.enable();
        }
        if (BA.isDiscovering()) {
            // Bluetooth is already in modo discovery mode, we cancel to restart it again
            BA.cancelDiscovery();
        }
        listItem.setText("Available Devices :");
        Log.i(TAG,"List all Available Devices");
        // Make sure to clear the list of available devices from last click
        list.clear();
        BA.startDiscovery();

        //TODO Fix this for connection
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Toast.makeText(getApplicationContext(), "Start Pairing...", Toast.LENGTH_LONG).show();
                Log.v(TAG, "Start Pairing with new Device \n");
                Log.v(TAG, "id = " + id + "\n");
                Log.v(TAG, "position = " + position + "\n");
                String info = ((TextView) view).getText().toString();
                Log.v(TAG, "device info = " + info + "\n");
                //get the device address when click the device item
                String address = info.substring(info.length() - 17);
                Log.v(TAG, "device Address = " + address + "\n");
                if (!Pair(address)) {
                    Log.e(TAG, "Cannot Pair to BLE Device\n");
                } else {
                    Log.i(TAG, "Paired to device ---> " + info);
                }
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        //@Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG,"KP Debug onReceive Called Action = " +action+"\n");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceInfo=device.getName() + "\n" + device.getAddress();
                /* Do Not Display Already Bonded Device */
                if (device.getBondState() != BluetoothDevice.BOND_BONDED)
                    list.add(deviceInfo);
                lv.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, list));
                Log.i("In action_found", "A device found"+ deviceInfo + "state ="+device.getBondState());
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i("In discovery started", "Discovery started");
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i("In discovery finished", "Discovery finished");
            }
        }
    };

    public boolean connect(String address) {
        if (BA == null || address == null) {
            Log.e(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            return mBluetoothGatt.connect();
        }

        final BluetoothDevice device = BA.getRemoteDevice(address);
        if (device == null) {
            Log.e(TAG, "Device not found,Unable to connect.");
            return false;
        }
        //Check if bonding is done or not ?

        if(mBluetoothGatt == null) {
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
            Log.i(TAG, "Trying to create a new connection.");
        } else {
            Log.i(TAG, "Already connected");
        }
        mBluetoothDeviceAddress = address;
        return true;
    }

    public boolean Pair(String address) {
        final BluetoothDevice device = BA.getRemoteDevice(address);
        Log.i(TAG, "Pairing Initiated So cancel Discovery ...");
        BA.cancelDiscovery();
        Log.i(TAG, "Bond created");
        boolean result = device.createBond();
        return result;
    }

    public boolean UnPair(String address) {
        try {
            Class<?> btDeviceInstance = Class.forName(BluetoothDevice.class.getCanonicalName());
            Method removeBondMethod = btDeviceInstance.getMethod("removeBond");
            final BluetoothDevice device = BA.getRemoteDevice(address);

            Log.i(TAG, "Un Pairing Initiated...");
            Log.i(TAG, "Bond removed");
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                removeBondMethod.invoke(device);
                Log.i(TAG, "Cleared Pairing");
            }
            return true;
        } catch (Throwable th) {
            Log.e(TAG, "Error pairing", th);
            return false;
        }
    }

    public void disconnectGatt(BluetoothGatt gatt) {
        if(gatt != null) {
            gatt.close();
            gatt.disconnect();
            gatt = null;
        }
    }
    // Various callback methods defined by the BLE API.
    BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);

                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(TAG, "Connected to GATT client. Attempting to start service discovery");
                        gatt.discoverServices();
                    }else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.i(TAG, "Disconnected from GATT client");
                    }
                }
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.i(TAG, "Send Data\n");
                        /*
                        BluetoothGattCharacteristic writeChar = mBluetoothGatt.getService(myServiceUUID)
                                .getCharacteristic(myWriteCharUUID);
                        byte[] data = new byte[10];
                        writeChar.setValue(data);
                        gatt.writeCharacteristic(writeChar);
                        */
                    }
                }
        };



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
