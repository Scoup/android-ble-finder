package com.example.receptor.sdk;

public class Region {

	private Integer major;
	private Integer minor;
	private String proximityUUID;
	private String identifier;
	
	public Region(String identifier, String proximityUUID, Integer major, Integer minor) {
		this.identifier = identifier;
		this.proximityUUID = proximityUUID;
		this.major = major;
		this.minor = minor;
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

	public String getIdentifier() {
		return identifier;
	}
	
}
