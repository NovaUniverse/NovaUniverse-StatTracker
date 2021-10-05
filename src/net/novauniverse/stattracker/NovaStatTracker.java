package net.novauniverse.stattracker;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.novauniverse.commons.NovaUniverseCommons;
import net.novauniverse.commons.network.server.NovaServerType;
import net.novauniverse.main.NovaMain;
import net.novauniverse.stattracker.common.PlayerSessionData;
import net.novauniverse.stattracker.common.SessionData;
import net.novauniverse.stattracker.game.GameSupportManager;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.NovaCore;

public class NovaStatTracker extends JavaPlugin {
	private static NovaStatTracker instance;

	public static NovaStatTracker getInstance() {
		return instance;
	}

	@Override
	public void onLoad() {
		NovaStatTracker.instance = this;
	}

	@Override
	public void onEnable() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (NovaCore.isNovaGameEngineEnabled()) {
					Log.info(getName(), "Calling GameSupportManager.init()");
					GameSupportManager.init();
				}
			}
		}.runTaskLater(this, 1L);
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Plugin) this);
		Bukkit.getScheduler().cancelTasks(this);
	}

	public void submitSession(SessionData session) {
		NovaServerType serverType = NovaMain.getInstance().getServerType();

		String sql;
		PreparedStatement ps;
		ResultSet rs;

		int sessionId = -1;

		Log.info(getName(), ChatColor.GREEN + "Submitting session...");

		try {
			sql = "INSERT INTO sessions (game_id, total_places, metadata) VALUES (?, ?, ?)";
			ps = NovaUniverseCommons.getDbConnection().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			ps.setInt(1, serverType.getId());
			ps.setInt(2, session.getTotalPlaces());
			if (session.getMetadata() == null) {
				ps.setNull(3, Types.VARCHAR);
			} else {
				ps.setString(3, session.getMetadata());
			}

			ps.executeUpdate();

			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				sessionId = rs.getInt(1);
			} else {
				Log.error("NovaStatTracker", "Could not find insert id for session");
				return;
			}

			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.error("NovaStatTracker", "Failed to insert session in database");
			return;
		}

		for (PlayerSessionData psd : session.getPlayerSessionData()) {
			UUID uuid = psd.getUuid();

			int playerId = -1;

			try {
				sql = "SELECT id FROM players WHERE uuid = ?";

				ps = NovaUniverseCommons.getDbConnection().getConnection().prepareStatement(sql);

				ps.setString(1, uuid.toString());

				rs = ps.executeQuery();

				if (rs.next()) {
					playerId = rs.getInt("id");
				} else {
					rs.close();
					ps.close();
					Log.error("NovaStatTracker", "Could not find id for player with uuid " + uuid.toString());
					continue;
				}

				rs.close();
				ps.close();
			} catch (Exception e) {
				e.printStackTrace();
				Log.error("NovaStatTracker", "Failed to fetch id for player with uuid " + uuid.toString());
				continue;
			}

			try {
				sql = "INSERT INTO session_player (player_id, session_id, placement) VALUES (?, ?, ?)";

				ps = NovaUniverseCommons.getDbConnection().getConnection().prepareStatement(sql);

				ps.setInt(1, playerId);
				ps.setInt(2, sessionId);
				ps.setInt(3, psd.getPlacement());
				
				ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				Log.error("NovaStatTracker", "Failed to submit session data for player with uuid " + uuid.toString());
			}
		}

		Log.info(getName(), ChatColor.GREEN + "Session submitted");
	}
}