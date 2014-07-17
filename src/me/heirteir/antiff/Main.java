package me.heirteir.antiff;

import java.io.File;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.heirteir.antiff.commands.Commands;
import me.heirteir.antiff.config.Configurations;
import me.heirteir.antiff.config.CreateConfig;
import me.heirteir.antiff.listeners.PlayerListener;
import me.heirteir.antiff.updater.Updater;
import me.heirteir.combat.CombatListener;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

//Made by HEIRTEIR

public class Main extends JavaPlugin implements Listener {
    public CombatListener combat;
    private PlayerListener plr;

    public void onEnable() {
	if (!checkDependencies()) {
	    Bukkit.getPluginManager().disablePlugin(this);
	    return;
	}

	// Update Config
	CreateConfig.updateConfig(this);

	// generating choices
	Configurations.reloadMessages(this);

	// starting timers and checkers
	plr = new PlayerListener(this, hasEssentials());
	combat = new CombatListener(this);

	// Updater
	initUpdater();

	// Register events
	Bukkit.getPluginManager().registerEvents(this, this);
	Bukkit.getPluginManager().registerEvents(combat, this);
	Bukkit.getPluginManager().registerEvents(plr, this);

	// generate hacker file
	if (Configurations.isGENERATE_LOG())
	    generateHackerFile();

	this.getCommand("antiff").setExecutor(new Commands(this));
    }

    public boolean checkDependencies() {
	PluginManager pm = Bukkit.getPluginManager();
	if (Configurations.allowAntiLag()) {
	    if (pm.getPlugin("WorldGuard") != null && pm.getPlugin("WorldGuard").isEnabled()) {
		PlayerTeleportEvent.getHandlerList().unregister(Bukkit.getPluginManager().getPlugin("WorldGuard"));
		PlayerMoveEvent.getHandlerList().unregister(Bukkit.getPluginManager().getPlugin("WorldGuard"));
	    }

	    if (pm.getPlugin("Essentials") != null && pm.getPlugin("Essentials").isEnabled()) {
		PlayerTeleportEvent.getHandlerList().unregister(Bukkit.getPluginManager().getPlugin("Essentials"));
		PlayerMoveEvent.getHandlerList().unregister(Bukkit.getPluginManager().getPlugin("Essentials"));
		PlayerInteractEvent.getHandlerList().unregister(Bukkit.getPluginManager().getPlugin("Essentials"));
		BlockBreakEvent.getHandlerList().unregister(Bukkit.getPluginManager().getPlugin("Essentials"));
	    }

	    if (pm.getPlugin("WorldEdit") != null && pm.getPlugin("WorldEdit").isEnabled()) {
		PlayerTeleportEvent.getHandlerList().unregister(Bukkit.getPluginManager().getPlugin("WorldEdit"));
		PlayerMoveEvent.getHandlerList().unregister(Bukkit.getPluginManager().getPlugin("WorldEdit"));
		PlayerInteractEvent.getHandlerList().unregister(Bukkit.getPluginManager().getPlugin("WorldEdit"));
		BlockBreakEvent.getHandlerList().unregister(Bukkit.getPluginManager().getPlugin("WorldEdit"));
	    }
	}

	if (pm.getPlugin("ProtocolLib") == null || !pm.getPlugin("ProtocolLib").isEnabled()) {
	    Logger logger = Bukkit.getLogger();
	    logger.log(Level.SEVERE, "#<|>><><><><><><><><><><><><><><><<<|>#");
	    logger.log(Level.SEVERE, "#<|>>=============================<<|>#");
	    logger.log(Level.SEVERE, "#<|>>======[ANTI-FORCEFIELD]======<<|>#");
	    logger.log(Level.SEVERE, "#<|>>========[ProtocolLib]========<<|>#");
	    logger.log(Level.SEVERE, "#<|>>=Not Found or isn't enabled!=<<|>#");
	    logger.log(Level.SEVERE, "#<|>>==Please get Protocolib or===<<|>#");
	    logger.log(Level.SEVERE, "#<|>>====Plugin won't be usable===<<|>#");
	    logger.log(Level.SEVERE, "#<|>>======[ANTI-FORCEFIELD]======<<|>#");
	    logger.log(Level.SEVERE, "#<|>>=============================<<|>#");
	    logger.log(Level.SEVERE, "#<|>><><><><><><><><><><><><><><><<<|>#");
	    return false;
	}
	return true;
    }

    public boolean hasEssentials() {
	PluginManager pm = Bukkit.getPluginManager();
	if (pm.getPlugin("Essentials") == null || !pm.getPlugin("Essentials").isEnabled())
	    return false;
	return true;
    }

    public void generateHackerFile() {
	File hackerfile = new File(this.getDataFolder(), "hackers.yml");

	if (!hackerfile.exists()) {
	    try {
		hackerfile.createNewFile();
	    } catch (IOException e) {
		e.printStackTrace();
		Bukkit.getPluginManager().disablePlugin(this);
	    }
	}
    }

    public void initUpdater() {

	if (!Bukkit.getOnlineMode()) {
	    Bukkit.getLogger().log(Level.SEVERE, "Couldn't check for update as server is in offline mode.");
	    return;
	}

	if (Configurations.shouldUpdate()) {
	    new Updater(this, true).performUpdateCheck();
	} else
	    new Updater(this, false).performUpdateCheck();
    }
}
