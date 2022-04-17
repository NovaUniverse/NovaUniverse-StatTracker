package net.novauniverse.stattracker.game.trackers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.novauniverse.games.manhunt.v1.game.event.ManhuntGameEndEvent;
import net.novauniverse.games.manhunt.v1.game.event.ManhuntGameStartEvent;
import net.novauniverse.games.manhunt.v1.game.team.ManhuntRole;
import net.novauniverse.stattracker.NovaStatTracker;
import net.novauniverse.stattracker.common.PlayerSessionData;
import net.novauniverse.stattracker.common.SessionData;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameEndReason;
import net.zeeraa.novacore.spigot.module.NovaModule;

public class ManhuntTracker extends NovaModule implements Listener {
	private SessionData session;
	private Map<UUID, ManhuntRole> participants;

	public ManhuntTracker() {
		super("StatTrack.ManhuntTracker");
	}

	@Override
	public void onEnable() {
		participants = new HashMap<UUID, ManhuntRole>();
		session = null;
	}

	@Override
	public void onDisable() {
		participants.clear();
		session = null;
	}

	@EventHandler
	public void onManhuntGameStart(ManhuntGameStartEvent e) {
		int hunter = 0;
		int speedrunner = 0;

		for (UUID uuid : e.getPlayerRoles().keySet()) {
			ManhuntRole color = e.getPlayerRoles().get(uuid);

			participants.put(uuid, color);

			switch (color) {
			case HUNTER:
				hunter++;
				break;

			case SPEEDRUNNER:
				speedrunner++;
				break;

			default:
				break;
			}
		}

		session = new SessionData(2);
		session.setMetadata(hunter + "," + speedrunner);
	}

	@EventHandler
	public void onManhuntGameEnd(ManhuntGameEndEvent e) {
		if (session == null) {
			return;
		}

		session.setMetadata(session.getMetadata() + "," + e.getWinner().name());

		participants.forEach((UUID uuid, ManhuntRole color) -> {
			boolean winner = false;

			if (color == e.getWinner()) {
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