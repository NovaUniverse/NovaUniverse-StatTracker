package net.novauniverse.stattracker.game.trackers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import net.novauniverse.stattracker.NovaStatTracker;
import net.novauniverse.stattracker.common.PlayerSessionData;
import net.novauniverse.stattracker.common.SessionData;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameEndReason;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.events.GameEndEvent;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.events.GameStartEvent;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.events.PlayerEliminatedEvent;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.events.PlayerWinEvent;
import net.zeeraa.novacore.spigot.module.NovaModule;

public class BasicStatTracker extends NovaModule implements Listener {
	private SessionData session;
	private List<UUID> participants;

	public BasicStatTracker() {
		super("StatTrack.BasicStatTracker");
	}

	@Override
	public void onEnable() {
		participants = new ArrayList<UUID>();
		session = null;
	}

	@Override
	public void onDisable() {
		participants.clear();
		session = null;
	}

	@EventHandler
	public void onGameStart(GameStartEvent e) {
		session = new SessionData(e.getGame().getPlayers().size());

		e.getGame().getPlayers().forEach((uuid) -> {
			participants.add(uuid);
		});
	}

	@EventHandler
	public void onPlayerEliminated(PlayerEliminatedEvent e) {
		if (session == null) {
			return;
		}

		Log.trace(getName(), e.getClass().getName() + " uuid: " + e.getPlayer().getUniqueId() + " placement: " + e.getPlacement());

		participants.remove(e.getPlayer().getUniqueId());

		// Placement starts at 0 so we need to add 1
		PlayerSessionData psd = new PlayerSessionData(e.getPlayer().getUniqueId(), e.getPlacement() + 1);

		session.getPlayerSessionData().add(psd);
	}

	@EventHandler
	public void onPlayerWin(PlayerWinEvent e) {
		Log.trace(getName(), e.getClass().getName() + " uuid: " + e.getPlayer().getUniqueId());

		if (session == null) {
			return;
		}

		participants.remove(e.getPlayer().getUniqueId());

		PlayerSessionData psd = new PlayerSessionData(e.getPlayer().getUniqueId(), 1);

		session.getPlayerSessionData().add(psd);
	}

	@EventHandler
	public void onGameEnd(GameEndEvent e) {
		Log.trace(getName(), e.getClass().getName());
		if (session == null) {
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				if (e.getReason() == GameEndReason.WIN) {
				} else {
					participants.forEach((uuid) -> {
						PlayerSessionData psd = new PlayerSessionData(uuid, 0);

						session.getPlayerSessionData().add(psd);
					});
				}

				NovaStatTracker.getInstance().submitSession(session);
				disable();
			}
		}.runTaskLater(NovaStatTracker.getInstance(), 4L);
	}
}