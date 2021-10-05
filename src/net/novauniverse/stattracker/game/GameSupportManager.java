package net.novauniverse.stattracker.game;

import net.novauniverse.stattracker.game.trackers.BasicStatTracker;
import net.novauniverse.stattracker.game.trackers.ManhuntTracker;
import net.novauniverse.stattracker.game.trackers.MissileWarsTracker;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.module.ModuleManager;
import net.zeeraa.novacore.spigot.module.modules.game.Game;
import net.zeeraa.novacore.spigot.module.modules.game.GameManager;

public class GameSupportManager {
	public static void init() {
		if (GameManager.getInstance().isEnabled()) {
			if (GameManager.getInstance().hasGame()) {
				Game game = GameManager.getInstance().getActiveGame();

				boolean useBasicTracker = false;
				
				String gameName = game.getName();
				
				Log.info("GameSupportManager", "Game name: " + gameName);
				
				switch (gameName) {
				case "missilewars":
					Log.info("GameSupportManager", "Setting up tracker for missilewars using MissileWarsTracker");
					ModuleManager.loadModule(MissileWarsTracker.class, true);
					break;

				case "manhunt":
					Log.info("GameSupportManager", "Setting up tracker for manhunt using ManhuntTracker");
					ModuleManager.loadModule(ManhuntTracker.class, true);
					break;

				case "deathswap":
					useBasicTracker = true;
					break;
					
				case "survivalgames":
					useBasicTracker = true;
					break;
					
				case "skywars":
					useBasicTracker = true;
					break;
					
				case "uhcv2":
					useBasicTracker = true;
					break;
					
				default:
					break;
				}
				
				if(useBasicTracker) {
					Log.info("GameSupportManager", "Using basic BasicStatTracker");
					ModuleManager.loadModule(BasicStatTracker.class, true);
				}
			}
		}
	}
}