package com.example.receptor.sdk;

public class Beacon {

	private int major;
	private int minor;
	private String proximityUUID;
	private String identifier;
	private String name;
	private String macAddress;
	private int rssi;
	
	public Beacon(String proximityUUID, String name, String macAddress, int major, int minor, int rssi) {
		this.proximityUUID = proximityUUID;
		this.name = name;
		this.macAddress = macAddress;
		this.major = major;
		this.minor = minor;
		this.rssi = rssi;
	}
	
	public Integer getMajor() {
		return major;
	}

	public Integer getMinor() {
		return minor;
	}

	public String getProximityUUID() {
		return proximityUUID;
	}

	public String getName() {
		return name;
	}
	
	public String getMacAddress() {
		return macAddress;
	}
	
	public int getRssi() {
		return rssi;
	}
	
	public String getIdentifier() {
		return identifier;
	}
}
