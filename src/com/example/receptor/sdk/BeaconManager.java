package com.example.receptor.sdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

/**
 * Some sources:
 * https://developer.android.com/guide/topics/connectivity/bluetooth-le.html
 * http://makezine.com/2014/01/03/reverse-engineering-the-estimote/
 * http://www.appcoda.com/ios7-programming-ibeacons-tutorial/
 * http://stackoverflow.com/questions/22016224/ble-obtain-uuid-encoded-in-advertising-packet
 * https://www.bluetooth.org/DocMan/handlers/DownloadDoc.ashx?doc_id=282152
**/

public class BeaconManager {
	
	private final static String TAG = "BeaconManager";
	
	private final BluetoothManager mBluetoothManager;
	private final BluetoothAdapter mBluetoothAdapter;
	private static long SCAN_PERIOD = 5000;
	private static long SCAN_SLEEP = 10000;
    private boolean mScanning = false;
    private Handler mHandler = new Handler();
    
	private BeaconManager.ServiceReadyCallback mServiceReadyCallback; // nao implementado
	private BeaconManager.RangingListener mRangingListener;
	private Service mService; // reservado caso queira utilizar o GATT futuramente
	
	public static interface RangingListener {
		public void onBeaconsDiscovered(List<Beacon> beacons);
	};
	
	public static interface ServiceReadyCallback {
		public void onServiceReady();
	};

	public BeaconManager(Service service) {
		Log.d(TAG, "Serviço criado");
		mService = service;
		mBluetoothManager = (BluetoothManager) service.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = mBluetoothManager.getAdapter();
	}
	
	
	public void connect(BeaconManager.ServiceReadyCallback callback) {
		Log.d(TAG, "Serviço connectado");
		mServiceReadyCallback = callback;
		scanDevices(true);
	}
	
	public void setRangingListener(BeaconManager.RangingListener listener) {
		mRangingListener = listener;
	}
	
	public boolean isScanning() {
		return mScanning;
	}
	
	private void scanDevices(final boolean enable) {
		if(enable) {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					Log.i(TAG, "Scan stopped");
					sleepToNextScan();
				}
			}, SCAN_PERIOD);
			Log.i(TAG, "Scan started");
			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			Log.i(TAG, "Scan stopped");
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}
	
	private void sleepToNextScan() {
		Log.i(TAG, "Sleep started");
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				scanDevices(true);
			}
		}, SCAN_SLEEP);
	}
	
	/**
	 * Callback de quando encontrado algum device no scan
	 */
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			Log.i(TAG, "onLenScan");
			Log.d(TAG, "address: " + device.getAddress());
			Log.d(TAG, "name: " + device.getName()); 
			ParcelUuid[] list = device.getUuids();
			if(list == null) {
				Log.d(TAG, "list null");
			} else {
				String value = String.valueOf(list.length);
				Log.d(TAG, "size: " + value);				
			}
			Log.d(TAG, "byte: " + scanRecord.toString());
			Log.d(TAG, "decoded hex: " + getHexString(scanRecord));
			Beacon beacon = generateBeaconScan(device, rssi, scanRecord);
			if(mRangingListener != null) {
				List<Beacon> beacons = new ArrayList<Beacon>();
				beacons.add(beacon);
				mRangingListener.onBeaconsDiscovered(beacons);
			}
		}
	};
	
	/**
	 * Altera o tempo de scan e sleep
	 * @param scanPeriod Scan time
	 * @param scanSleep Sleep time
	 */
	public void setForegroundScanPeriod(long scanPeriod,long scanSleep) {
		BeaconManager.SCAN_PERIOD = scanPeriod;
		BeaconManager.SCAN_SLEEP = scanSleep;
	}

	public void disconnect() {
		Log.i(TAG, "Disconnected. Scan stopped.");
		mScanning = false;
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
	}
	
	/**
	 * Cada scan possui 4 pacote de dados que completam 31 octets (para identificação)
	 * 
	 * https://www.bluetooth.org/DocMan/handlers/DownloadDoc.ashx?doc_id=282152
	 * 
	 * Só precisamos de uma parte do pacote, a segunda.
	 * Exemplo data do segundo pacote: 4C00-02-15-B9407F30F5F8466EAFF925556B57FE6D-0FA0-0FA1-B6
	 * 
	 * Primeiros 2 bytes: Apple Company Identifier (Little Endian) 0x0042
	 * Terceiro byte: especifica datatype, geralmente 2
	 * Quarto byte: especifica tamanho do data
	 * 16 bytes (B9407F30F5F8466EAFF925556B57FE6D): UUID
	 * Proximo 2 Bytes depois do UUID: Major
	 * Proximo 2 Bytes depois do Major: Minor
	 * Final byte: rssi
	 * 
	 * @param records
	 * @param rssi
	 * @return Beacon
	 */
	public Beacon generateBeaconScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
		Log.i(TAG, "generateBeaconScan");
		List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);
		AdRecord record = records.get(1);
		Log.d(TAG, "record length: " + String.valueOf(record.length)); // 26 bytes
		byte[] data = record.data;
		byte[] uuid = Arrays.copyOfRange(data, 4, 20);
		byte[] major = Arrays.copyOfRange(data, 20, 22);
		byte[] minor = Arrays.copyOfRange(data, 22, 24);
//		byte[] rssi = Arrays.copyOfRange(data, 24, 25);
		Log.d(TAG,
				"uuid: " + getHexString(uuid)
				+ ", major: " + getHexString(major)
				+ ", minor: " + getHexString(minor)
			);
		Beacon beacon = new Beacon(
				getHexString(uuid), 
				"", 
				device.getAddress(), 
				getHex(major).intValue(), 
				getHex(minor).intValue(),  
				rssi);
		return beacon;
	}
	
	//4C00 02 15 B9407F30F5F8466EAFF925556B57FE6D 0FA0 0FA1 B6
	//4C00 02 15 B9407F30F5F8466EAFF925556B57FE6D ED4E 8931 B6
	/**
	 * Responsável por organizar o pacote de bytes do ble.
	 */
	public static class AdRecord {
		
		public int length;
		public int type;
		public byte[] data;

	    public AdRecord(int length, int type, byte[] data) {
	        Log.d("DEBUG", "Length: " + length + " Type : " + type + " Data : " + getHexString(data));
	        this.length = length;
	        this.type = type;
	        this.data = data;
	    }

	    public static List<AdRecord> parseScanRecord(byte[] scanRecord) {
	        List<AdRecord> records = new ArrayList<AdRecord>();

	        int index = 0;
	        while (index < scanRecord.length) {
	            int length = scanRecord[index++];
	            //Done once we run out of records
	            if (length == 0) break;

	            int type = scanRecord[index];
	            //Done if our record isn't a valid type
	            if (type == 0) break;

	            byte[] data = Arrays.copyOfRange(scanRecord, index+1, index+length);

	            records.add(new AdRecord(length, type, data));
	            //Advance
	            index += length;
	        }

	        return records;
	    }
	}
	
	static final String HEXES = "0123456789ABCDEF";
	public static String getHexString( byte [] raw ) {
	    if ( raw == null ) {
	        return null;
	    }
	    final StringBuilder hex = new StringBuilder( 2 * raw.length );
	    for ( final byte b : raw ) {
	        hex.append(HEXES.charAt((b & 0xF0) >> 4))
	            .append(HEXES.charAt((b & 0x0F)));
	    }
	    return hex.toString();
	}
	
	public static Long getHex( byte [] raw ) {
		String hexString = getHexString(raw);
	    return Long.parseLong(hexString, 16);
	}
	
}
