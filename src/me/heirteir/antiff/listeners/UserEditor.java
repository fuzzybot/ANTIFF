package me.heirteir.antiff.listeners;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.heirteir.antiff.Main;
import me.heirteir.antiff.config.Configurations;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class UserEditor {
    Main main;
    Player player;

    public UserEditor(Player player, Main main) {
	this.main = main;
	this.player = player;
    }

    public void updatePlayer() {
	if (!Configurations.isGENERATE_LOG())
	    return;

	File hackerfile = new File(main.getDataFolder(), "hackers.yml");
	FileConfiguration config = YamlConfiguration.loadConfiguration(hackerfile);

	Date d = new Date(System.currentTimeMillis());
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	String date = format.format(d).toString();

	if (config.get(player.getName()) == null) {
	    config.set(player.getName() + ".uuid", player.getUniqueId().toString());
	    config.set(player.getName() + ".amount", 1);
	    config.set(player.getName() + ".ip", player.getAddress().getAddress().toString().replace("/", ""));
	    config.set(player.getName() + ".latestdate", date);
	    config.set(player.getName() + ".banned", player.isBanned());
	} else {
	    config.set(player.getName() + ".amount", (config.getInt(player.getName() + ".amount") + 1));
	    config.set(player.getName() + ".ip", player.getAddress().getAddress().toString().replace("/", ""));
	    config.set(player.getName() + ".latestdate", date);
	    config.set(player.getName() + ".banned", player.isBanned());
	}

	int amount = config.getInt(player.getName() + ".amount");

	if (amount >= Configurations.getCHANCES()) {
	    if (Configurations.getCHANCES() > 0) {
		for (String s : Configurations.getOUTOFCHANCES())
		    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", player.getName()).replace("/", ""));
		config.set(player.getName() + ".banned", player.isBanned());
	    }
	}

	try {
	    config.save(hackerfile);
	} catch (IOException e) {
	    e.printStackTrace();
	    Bukkit.getPluginManager().disablePlugin(main);
	}
    }
}
