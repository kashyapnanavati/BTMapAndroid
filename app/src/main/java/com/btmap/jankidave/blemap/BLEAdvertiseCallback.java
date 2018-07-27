package com.btmap.jankidave.blemap;

import android.bluetooth.BluetoothDevice;

public interface BLEAdvertiseCallback {
    void onInitSuccess();
    void onInitFailure(String message);
    void onClientConnect(BluetoothDevice device);
    void onInfo(String info);
    void onError(String error);
}
