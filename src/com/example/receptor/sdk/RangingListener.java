package com.example.receptor.sdk;

import java.util.List;

public interface RangingListener {
	public void onBeaconsDiscovered(Region region, List<Beacon> beacons);
}
