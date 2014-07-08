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
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

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

	// starting timers and checkers
	plr = new PlayerListener(this, hasEssentials());
	combat = new CombatListener(this);

	// generating choices
	Configurations.reloadMessages(this);

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
	if (pm.getPlugin("ProtocolLib") == null || !pm.getPlugin("ProtocolLib").isEnabled()) {
	    Logger logger = Bukkit.getLogger();
	    logger.log(Level.SEVERE, "[===============================]");
	    logger.log(Level.SEVERE, "[=======[ANTI-FORCEFIELD]=======]");
	    logger.log(Level.SEVERE, "[=========[ProtocolLib]=========]");
	    logger.log(Level.SEVERE, "[==Not Found or isn't enabled!==]");
	    logger.log(Level.SEVERE, "[===Please get Protocolib or====]");
	    logger.log(Level.SEVERE, "[=====Plugin won't be usable====]");
	    logger.log(Level.SEVERE, "[=======[ANTI-FORCEFIELD]=======]");
	    logger.log(Level.SEVERE, "[===============================]");
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
