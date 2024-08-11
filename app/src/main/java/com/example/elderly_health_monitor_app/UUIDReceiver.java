package com.example.elderly_health_monitor_app;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.os.Parcelable;

import java.util.UUID;

public class UUIDReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_UUID.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
            if (uuidExtra != null) {
                for (Parcelable p : uuidExtra) {
                    UUID uuid = ((ParcelUuid) p).getUuid();
                    System.out.println("UUID: " + uuid.toString());
                }
            }
        }
    }
}
