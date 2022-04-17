package net.novauniverse.stattracker.game.trackers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.novauniverse.games.missilewars.game.event.MissileWarsGameEndEvent;
import net.novauniverse.games.missilewars.game.event.MissileWarsGameStartEvent;
import net.novauniverse.games.missilewars.game.team.TeamColor;
import net.novauniverse.stattracker.NovaStatTracker;
import net.novauniverse.stattracker.common.PlayerSessionData;
import net.novauniverse.stattracker.common.SessionData;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameEndReason;
import net.zeeraa.novacore.spigot.module.NovaModule;

public class MissileWarsTracker extends NovaModule implements Listener {
	private SessionData session;
	private Map<UUID, TeamColor> participants;

	public MissileWarsTracker() {
		super("StatTrack.MissileWarsTracker");
	}

	@Override
	public void onEnable() {
		participants = new HashMap<UUID, TeamColor>();
		session = null;
	}

	@Override
	public void onDisable() {
		participants.clear();
		session = null;
	}

	@EventHandler
	public void onMissileWarsGameStart(MissileWarsGameStartEvent e) {
		int red = 0;
		int green = 0;

		for (UUID uuid : e.getPlayerTeams().keySet()) {
			TeamColor color = e.getPlayerTeams().get(uuid);

			participants.put(uuid, color);

			switch (color) {
			case RED:
				red++;
				break;

			case GREEN:
				green++;
				break;

			default:
				break;
			}
		}

		session = new SessionData(2);
		session.setMetadata(red + "," + green);
	}

	@EventHandler
	public void onMissileWarsGameEnd(MissileWarsGameEndEvent e) {
		if (session == null) {
			return;
		}

		if (e.getWinningTeam() != null) {
			session.setMetadata(session.getMetadata() + "," + e.getWinningTeam().name());
		}

		participants.forEach((UUID uuid, TeamColor color) -> {
			boolean winner = false;

			if (color == e.getWinningTeam()) {
				winner = true;
			}

			int placement = 0;

			if (e.getGameEndReason() == GameEndReason.WIN) {
				placement = winner ? 1 : -1;
			}

			PlayerSessionData psd = new PlayerSessionData(uuid, placement);

			session.getPlayerSessionData().add(psd);
		});

		NovaStatTracker.getInstance().submitSession(session);
		this.disable();
	}
}