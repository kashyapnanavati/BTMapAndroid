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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
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
import android.os.Message;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    Button b_on, b_off, b_list_devices, b_get_visible, start, test_no_key;
    TextView listItem;
    BluetoothAdapter BA;
    private Set<BluetoothDevice>pairedDevices;
    ListView lv, lva;
    private static final String TAG = "MainActivity";
    private BluetoothGatt mBluetoothGatt;
    private String mBluetoothDeviceAddress;
    private IntentFilter filter2 = new IntentFilter();
    final ArrayList list = new ArrayList();
    private int mState;

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private BluetoothService mChatService; //Member object for the chat services
    private String mConnectedDeviceName; //Name of the connected device
    private String mConnectedDeviceAdd; //Address of the connected device

    private ArrayList listDevices;
    private ArrayAdapter adapter;

    /*Messages layout related defines */
    private TextInputLayout inputLayout;
    private ArrayAdapter<String> chatAdapter;
    private ArrayList<String> chatMessages;
    private ListView msglistview;

    /* Regarding Motion of the device */
    private SensorManager sensorMan;
    private Sensor accelerometer;

    private float[] mGravity;
    private double mAccel;
    private double mAccelCurrent;
    private double mAccelLast;

    private boolean sensorRegistered = false;
    private int hitCount = 0;
    private double hitSum = 0;
    private double hitResult = 0;

    private final int SAMPLE_SIZE = 50; // change this sample size as you want, higher is more precise but slow measure.
    private final double THRESHOLD = 0.2; // change this threshold as you want, higher is more spike movement

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b_on = (Button) findViewById(R.id.turnon);
        b_off =(Button)findViewById(R.id.turnoff);
        b_list_devices =(Button)findViewById(R.id.listD);
        b_get_visible =(Button)findViewById(R.id.visible);
        listItem =(TextView)findViewById(R.id.ListItem);
        test_no_key =(Button) findViewById(R.id.testnokey);

        BA = BluetoothAdapter.getDefaultAdapter();
        lv = (ListView)findViewById(R.id.listView);

        filter2.addAction(BluetoothDevice.ACTION_FOUND);
        filter2.addAction(BA.ACTION_DISCOVERY_STARTED);
        filter2.addAction(BA.ACTION_DISCOVERY_FINISHED);
        filter2.addAction(BA.ACTION_SCAN_MODE_CHANGED);
        filter2.addAction(BA.ACTION_SCAN_MODE_CHANGED);
        filter2.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(mReceiver, filter2);

        mChatService = new BluetoothService(this, mHandler);
        /*Testing on Saturday */
        if (mChatService.getState() == mChatService.STATE_NONE) {
            mChatService.start();
        }

        /*Testing Send message functionality */
        start =(Button) findViewById(R.id.Start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start new activity where we can test send/receive data messages for now
                //Intent Intent = new Intent(getApplicationContext(), Messages.class);
                //startActivity(Intent);
                //sendMessage("Hello KP How are you ?");
                setContentView(R.layout.activity_messages);
                findViewsByIdsMessages();
                //set chat adapter
                chatMessages = new ArrayList<String>();
                chatAdapter = new ArrayAdapter<String>(
                        MainActivity.this,
                        android.R.layout.simple_list_item_1,
                        chatMessages);
                msglistview.setAdapter(chatAdapter);

            }
        });

        /*Testing pairing without key or user permissions */
        test_no_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Test pairing without use permission\n");

            }
        });

        /* Asking permission at runtime */
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        /* Regarding Motion of the device  - TODO : Need to review and understand properly */

        sensorMan = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        sensorMan.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorRegistered = true;


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
        //disconnectGatt(mBluetoothGatt);
    }


    public  void visible(View v){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        // sets the device to be discoverable for 3 minutes (180 seconds)
        getVisible.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 180);
        startActivityForResult(getVisible, 0);
    }

    private final Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case Constants.MESSAGE_STATE_CHANGE:
                        Log.v(TAG, "mHandler --> MESSAGE_STATE_CHANGE\n");
                        switch (msg.arg1) {
                            case BluetoothService.STATE_CONNECTED:
                                Log.v(TAG, "BluetoothService.STATE_CONNECTED\n");
                                listDevices.clear();
                                break;
                            case BluetoothService.STATE_CONNECTING:
                                Log.v(TAG, "BluetoothService.STATE_CONNECTING\n");
                                break;
                            case BluetoothService.STATE_LISTEN:
                                Log.v(TAG, "BluetoothService.STATE_LISTEN\n");
                            case BluetoothService.STATE_NONE:
                                Log.v(TAG, "BluetoothService.STATE_NONE\n");
                                break;
                        }
                        break;
                    case Constants.MESSAGE_DEVICE_NAME:
                        // save the connected device's name
                        mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                        if (null != getApplicationContext()) {
                            Toast.makeText(getApplicationContext(), "Connected to "
                                    + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                        }
                        Log.v(TAG, "mHandler --> MESSAGE_DEVICE_NAME = \n" + mConnectedDeviceName);
                        listDevices.add(mConnectedDeviceName);
                        listItem.setText("Connected Device :");
                        adapter.notifyDataSetChanged();
                        break;
                    case Constants.MESSAGE_DEVICE_ADDRESS:
                        /* Temporary WA to start connect thread from client / Listen mode to establish two communication */
                        //BA.getRemoteDevice(BA.getAddress(mConnectedDeviceName));
                        mConnectedDeviceAdd = msg.getData().getString(Constants.DEVICE_ADDRESS);
                        Log.v(TAG, "mHandler --> MESSAGE_DEVICE_ADDRESS = \n" + mConnectedDeviceAdd);
                        if (mChatService != null && (mChatService.getState() != BluetoothService.STATE_CONNECTED)) {
                            //  Log.v(TAG, "Start connectThread Class -->" + "\n");
                            // Start the Bluetooth chat services - Not working ? Figure out how to fix this ?
                            mChatService.connect(BA.getRemoteDevice(mConnectedDeviceAdd), false);
                        } else {
                            Log.v(TAG, "mChatService is NULL " + "\n");
                        }
                        break;
                    case Constants.MESSAGE_READ:
                        Log.v(TAG, "mHandler --> MESSAGE_READ\n");
                        byte[] readBuf = (byte[]) msg.obj;

                        String readMessage = new String(readBuf, 0, msg.arg1);
                        chatMessages.add(mConnectedDeviceName.getBytes() + ":  " + readMessage);
                        chatAdapter.notifyDataSetChanged();
                        break;
                    case Constants.MESSAGE_WRITE:
                        Log.v(TAG, "mHandler --> MESSAGE_WRITE\n");
                        byte[] writeBuf = (byte[]) msg.obj;

                        String writeMessage = new String(writeBuf);
                        chatMessages.add("Me: " + writeMessage);
                        chatAdapter.notifyDataSetChanged();
                        break;
                    case Constants.MESSAGE_TOAST:
                        Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
    };

    public void list(View v){
        pairedDevices = BA.getBondedDevices();

        listDevices = new ArrayList();
        listDevices.clear();
        BA.cancelDiscovery();
        for(BluetoothDevice bt : pairedDevices) {
            //list.add(bt.getName());
            String devicename = bt.getName();
            String macAddress = bt.getAddress();
            listDevices.add(devicename+"\n"+macAddress);
        }
        Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();

        adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, listDevices);

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
                final BluetoothDevice device = BA.getRemoteDevice(address);
                //mChatService = new BluetoothService(getApplicationContext(), mHandler, device, BA);
                // Only if the state is STATE_NONE, do we know that we haven't started already
                if (mChatService != null) {
                  //  Log.v(TAG, "Start connectThread Class -->" + "\n");
                    // Start the Bluetooth chat services
                    mChatService.connect(device, false);
                } else {
                    Log.v(TAG, "mChatService is NULL " + "\n");
                }

            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                lv.setOnItemLongClickListener(null);
                Toast.makeText(getApplicationContext(), "Long click : Forget device", Toast.LENGTH_LONG).show();
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
            int int_action = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
            Log.i(TAG,"KP Debug onReceive Called Action = " +action+"\n");
            Log.i(TAG,"KP Debug onReceive Called IntAction = " +int_action+"\n");
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
            else if (BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE == int_action) {
                Log.i("SCN_MD_CONNECT_DISCVRBL", "The device is in discoverable mode");
                Toast.makeText(getApplicationContext(), "The device is in discoverable mode",Toast.LENGTH_LONG).show();
            }
            else if (BluetoothAdapter.SCAN_MODE_CONNECTABLE == int_action) {
                Log.i("SCAN_MODE_CONNECTABLE", "The device isn't in discoverable mode but can still receive connections");
                Toast.makeText(getApplicationContext(), "The device isn't in discoverable mode but can still receive connections",Toast.LENGTH_LONG).show();

            } else if (BluetoothAdapter.SCAN_MODE_NONE == int_action)
            {
                Log.i("SCAN_MODE_NONE", "The device isn't in discoverable mode and cannot receive connections");
                Toast.makeText(getApplicationContext(), "The device isn't in discoverable mode and cannot receive connections",Toast.LENGTH_LONG).show();
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                Log.i("ACTION_PAIRING_REQUEST", "Begin Automatic pairing");
                /* If we need Automatic pairing - convert this to system app
                try {
                    BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    bluetoothDevice.setPin(Constants.BLE_AUTO_PIN.getBytes());
                    Log.e(TAG,"Auto-entering pin: " + Constants.BLE_AUTO_PIN);
                    //setPairing confirmation if neeeded
                    bluetoothDevice.setPairingConfirmation(true);
                    Log.e(TAG,"pin entered and request sent...");
                } catch (Exception e) {
                    Log.e(TAG, "Error occurs when trying to auto pair");
                    e.printStackTrace();
                }
                */
            }
        }
    };

        /*
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
    */

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


    public void sendMessage(String message) {
        Log.d(TAG, "sendMessage...");
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Log.d(TAG, "Not able to send data Status = " + mChatService.getState());
            Toast.makeText(this, "Connection was lost! ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            Log.d(TAG, "Trying to send Data...");
            byte[] send = message.getBytes();
            mChatService.write(send);
        }
    }

    /*
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

                        //BluetoothGattCharacteristic writeChar = mBluetoothGatt.getService(myServiceUUID)
                        //        .getCharacteristic(myWriteCharUUID);
                        //byte[] data = new byte[10];
                        //writeChar.setValue(data);
                        //gatt.writeCharacteristic(writeChar);

                    }
                }
        }; */


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            /* Below code is copied from ->
             * https://stackoverflow.com/questions/14574879/how-to-detect-movement-of-an-android-device
             *
             */
            mGravity = event.values.clone();
            // Shake detection
            double x = mGravity[0];
            double y = mGravity[1];
            double z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = Math.sqrt(x * x + y * y + z * z);
            double delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            if (hitCount <= SAMPLE_SIZE) {
                hitCount++;
                hitSum += Math.abs(mAccel);
            } else {
                hitResult = hitSum / SAMPLE_SIZE;

                Log.d(TAG, String.valueOf(hitResult));

                if (hitResult > THRESHOLD) {
                    Log.d(TAG, "Walking");
                    if (mChatService.getState() == BluetoothService.STATE_CONNECTED) {
                        Log.d(TAG, "Trying to send Data while walking...");
                        sendMessage("Connected device is walking");
                    }
                } else {
                    Log.d(TAG, "Stop Walking");
                }

                hitCount = 0;
                hitSum = 0;
                hitResult = 0;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

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

    @Override
    public void onStart() {
        super.onStart();
        if (!BA.isEnabled()) {
            Log.w(TAG, "On Start : Bluetooth Adapter is not enabled !");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            mChatService = new BluetoothService(this, mHandler);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mChatService != null) {
            Log.w(TAG, "On Resume : mChatService object is NULL Status : "+ mChatService.getState());
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null)
            mChatService.stop();
    }

    /*Code Related to messges */
    private void findViewsByIdsMessages() {
        msglistview = (ListView) findViewById(R.id.msglist);
        inputLayout = (TextInputLayout) findViewById(R.id.input_layout);
        View btnSend = findViewById(R.id.btn_send);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Send button clicked");
                if (inputLayout.getEditText().getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, "Please input some texts", Toast.LENGTH_SHORT).show();
                } else {
                    //TODO: here
                    sendMessage(inputLayout.getEditText().getText().toString());
                    inputLayout.getEditText().setText("");
                }
            }
        });
    }
}
