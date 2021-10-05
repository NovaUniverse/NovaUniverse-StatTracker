package net.novauniverse.stattracker.common;

import java.util.ArrayList;
import java.util.List;

public class SessionData {
	private int totalPlaces;
	private List<PlayerSessionData> playerSessionData;
	private String metadata;

	public SessionData(int totalPlaces) {
		this.totalPlaces = totalPlaces;
		this.playerSessionData = new ArrayList<>();
		this.metadata = null;
	}

	public List<PlayerSessionData> getPlayerSessionData() {
		return playerSessionData;
	}

	public int getTotalPlaces() {
		return totalPlaces;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
}