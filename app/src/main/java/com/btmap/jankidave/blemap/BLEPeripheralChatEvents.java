package com.btmap.jankidave.blemap;

import android.bluetooth.BluetoothDevice;

/**
 * Created by jgomez on 4/05/16.
 * Updated by Kashyap
 */
public interface BLEPeripheralChatEvents extends BLEChatEvents {
    void onClientDisconnect(BluetoothDevice device);
    void onInitRfcommSocket();
    void onConnectRfcommSocket();
    void onClientConnectDevicename(String data);
}