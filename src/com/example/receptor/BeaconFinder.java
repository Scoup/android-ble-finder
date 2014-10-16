package com.example.receptor;

import java.util.List;

import com.example.receptor.sdk.Beacon;
import com.example.receptor.sdk.BeaconManager;
import com.example.receptor.sdk.BeaconManager.RangingListener;
import com.example.receptor.sdk.BeaconManager.ServiceReadyCallback;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BeaconFinder extends Service{
	
	private static final String TAG = "BeaconFinder";
	
	private BeaconManager beaconManager;
	
	public BeaconFinder() {
		Log.i(TAG, "BeaconFinder Constructor");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate, configure beaconManager");
		setBluetooth(true);
	    beaconManager = new BeaconManager(this);
	    beaconManager.setRangingListener(new RangingListener() {
			
			@Override
			public void onBeaconsDiscovered(List<Beacon> beacons) {
				Log.d(TAG, "found beacons: " + beacons.size());
				//broadcast beacons
			}
		});
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(TAG, "onStart, connect beaconManager, start scan");

//		beaconManager.setForegroundScanPeriod(5000, 25000); // alterar tempod e scan/sleep
	    beaconManager.connect(new ServiceReadyCallback() {
			
			@Override
			public void onServiceReady() {
				// não implementado
			}
		});
	}
	
	@Override
	public void onDestroy() {
		beaconManager.disconnect(); // desligar o ble!
		super.onDestroy();
	}

	public static boolean setBluetooth(boolean enable) {
	    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	    boolean isEnabled = bluetoothAdapter.isEnabled();
	    if (enable && !isEnabled) {
	        return bluetoothAdapter.enable(); 
	    }
	    else if(!enable && isEnabled) {
	        return bluetoothAdapter.disable();
	    }
	    // No need to change bluetooth state
	    return true;
	}
}