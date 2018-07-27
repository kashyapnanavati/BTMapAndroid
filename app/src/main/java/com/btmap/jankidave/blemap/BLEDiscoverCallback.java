package com.btmap.jankidave.blemap;

import android.bluetooth.BluetoothDevice;

public interface BLEDiscoverCallback {
    void onInitSuccess();
    void onInitFailure(String message);
    void onScanResult(BluetoothDevice device, int rssi);
    void onScanFailed(String message);
}