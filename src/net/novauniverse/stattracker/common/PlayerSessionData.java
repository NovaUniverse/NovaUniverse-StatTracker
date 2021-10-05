package net.novauniverse.stattracker.common;

import java.util.UUID;

public class PlayerSessionData {
	private UUID uuid;
	private int placement;

	public PlayerSessionData(UUID uuid, int placement) {
		this.uuid = uuid;
		this.placement = placement;
	}

	public UUID getUuid() {
		return uuid;
	}

	public int getPlacement() {
		return placement;
	}
}