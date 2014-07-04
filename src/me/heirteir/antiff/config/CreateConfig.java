package me.heirteir.antiff.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.heirteir.antiff.Main;
import me.heirteir.antiff.config.editor.SimpleConfig;
import me.heirteir.antiff.config.editor.SimpleConfigManager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class CreateConfig {
    static Main main;

    static FileConfiguration config;
    static SimpleConfig newConfig;

    static File configfile;

    static SimpleConfigManager manager;

    public static void updateConfig(Main m) {
	main = m;
	config = main.getConfig();

	configfile = new File(main.getDataFolder(), "config.yml");

	manager = new SimpleConfigManager(main);

	main.getDataFolder().mkdir();

	if (!configfile.exists()) {
	    try {
		configfile.createNewFile();
	    } catch (IOException e) {
		e.printStackTrace();
		Bukkit.getPluginManager().disablePlugin(main);
	    }
	}

	if (config.get("version-dontchange") == null || !config.getString("version-dontchange").equals(main.getDescription().getVersion()))
	    generateText();
    }

    private static void generateText() {
	String[] header = { ">>[Anti-ForceField]<<", "><>> Created by Heirteir <<><", ">> Version " + main.getDescription().getVersion() + " <<" };

	configfile.delete();
	try {
	    configfile.createNewFile();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	newConfig = manager.getNewConfig("config.yml", header);

	newConfig.setHeader(header);

	String[] updater = { "This updater pings curse.com for the latest releases", "of the plugin if you don't want to get latest", "releases automatically set this to false" };

	if (config.get("updater") == null)
	    newConfig.set("updater", false, updater);
	else
	    newConfig.set("updater", config.getBoolean("updater"), updater);

	if (config.get("inform-update") == null)
	    newConfig.set("inform-update", true, "tell you if there is an update or not.");
	else
	    newConfig.set("inform-update", config.getBoolean("inform-update"), "tell you if there is an update or not.");

	if (config.get("REPORT_MESSAGE") == null)
	    newConfig.set("REPORT_MESSAGE", "&f%player% &cwas kicked for hacking.", "Message sent when someone is cheating.");
	else
	    newConfig.set("REPORT_MESSAGE", config.getString("REPORT_MESSAGE"), "Message sent when someone is cheating.");

	String[] blocksafety = { "Whether or not to block safety mode", "safety mode is an option in hacked", "clients to make users only hit in FOV", "This is experimental so disable if",
		"you see any strange behaviour." };

	if (config.get("BLOCK_SAFETY") == null)
	    newConfig.set("BLOCK_SAFETY", true, blocksafety);
	else
	    newConfig.set("BLOCK_SAFETY", config.getBoolean("BLOCK_SAFETY"), blocksafety);

	String[] npcinfo = { "Timers for npc spawning and player cooldowns.", "All of the following values are in ticks.", "How often an npc will spawn on a player." };

	if (config.get("playercooldown") == null)
	    newConfig.set("playercooldown", 80, npcinfo);
	else
	    newConfig.set("playercooldown", config.getInt("playercooldown"), npcinfo);

	if (config.get("npclife") == null)
	    newConfig.set("npclife", 8, "How long the npc will be alive.");
	else
	    newConfig.set("npclife", config.getInt("npclife"), "How long the npc will be alive.");

	if (config.get("spawnrate") == null)
	    newConfig.set("spawnrate", 20, "How often to spawn the npc.");
	else
	    newConfig.set("spawnrate", config.getInt("spawnrate"), "How often to spawn the npc.");

	String[] npcname = { "The name for the NPC make it difficult.", "DON'T MAKE IT THE NAME OF A PLAYER!" };

	if (config.get("npcname") == null)
	    newConfig.set("npcname", "10lI10IllI", npcname);
	else
	    newConfig.set("npcname", config.getString("npcname"), npcname);

	if (config.get("combattime") == null)
	    newConfig.set("combattime", 12, "How long to keep a player in the combat list. (in seconds)");
	else
	    newConfig.set("combattime", config.getInt("combattime"), "How long to keep a player in the combat list. (in seconds)");

	String[] playeractions = { "Whether or not to kill the player" };

	if (config.get("killplayer") == null)
	    newConfig.set("killplayer", true, playeractions);
	else
	    newConfig.set("killplayer", config.getBoolean("killplayer"), playeractions);

	String[] senderoption = { "Whether or not to send a player was kicked for", "hacking to the whole server or", "to just staff. Options [STAFF, ALL, NONE]" };

	if (config.get("report") == null)
	    newConfig.set("report", "STAFF", senderoption);
	else
	    newConfig.set("report", config.getString("report"), senderoption);

	String[] commands = { "Commands to be executed on the player when caught hacking", "Example", "commands: ", "  - /kick %player% Hacking", "  - /ban %player% Hacking",
		"  - /eco take %player% 10000" };

	List<String> commandstrings = new ArrayList<String>();

	if (config.get("commands") == null) {
	    commandstrings.add("/kick %player% &cYou have been kicked for hacking.");
	    newConfig.set("commands", commandstrings, commands);
	} else
	    newConfig.set("commands", config.getStringList("commands"), commands);

	String[] takeaction = { "Amount of times caught before a new action takes place.", "[-1 for infinite chances]" };
	if (config.get("chances") == null)
	    newConfig.set("chances", -1, takeaction);
	else
	    newConfig.set("chances", config.getInt("chances"), takeaction);

	ArrayList<String> outofchances = new ArrayList<String>();

	if (config.get("outofchances") == null) {
	    outofchances.add("/ban %player%");
	    newConfig.set("outofchances", outofchances, "Commands to execute when out of chances.");
	} else
	    newConfig.set("outofchances", config.getStringList("outofchances"), "Commands to execute when out of chances.");

	String[] antilag = { "This is an experimental antilag local to the plugin.", "This will help if your server is", "getting a low TPS rate." };
	if (config.get("use-antilag") == null)
	    newConfig.set("use-antilag", true, antilag);
	else
	    newConfig.set("use-antilag", config.getBoolean("use-antilag"), antilag);

	String[] antilagsetting = { "this option is when the antilag will run", "it is based on TPS, so if", "you choose 16.7 if you tps goes below", "16.7 it will execute the antilag", "MAXIMUM 20" };
	if (config.get("tps-counter") == null)
	    newConfig.set("tps-counter", 19.5, antilagsetting);
	else
	    newConfig.set("tps-counter", config.getInt("tps-counter"), antilagsetting);

	// EVERYTHING BEFORE
	String[] generatelog = { "Whether or not to generate a list of suspected cheaters.", "Must be set to true if your using chances system!" };

	if (config.get("generatelog") == null)
	    newConfig.set("generatelog", false, generatelog);
	else
	    newConfig.set("generatelog", config.getBoolean("generatelog"), generatelog);

	newConfig.set("version-dontchange", main.getDescription().getVersion(), "The config version your on (Changing will reload config).");

	newConfig.saveConfig();
    }
}
