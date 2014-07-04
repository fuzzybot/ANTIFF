package me.heirteir.antiff.config;

import java.io.File;
import java.util.List;

import me.heirteir.antiff.Main;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Configurations {
    private static String REPORT_MESSAGE;

    private static boolean UPDATER;
    private static boolean SHOULD_UPDATE;

    private static boolean BLOCK_SAFETY;
    private static int PLAYER_COOLDOWN;
    private static int NPC_LIFE;
    private static int SPAWN_RATE;
    private static String NPCNAME;
    private static int COMBAT_TIME;

    private static boolean KILL_PLAYER;
    private static String REPORT;
    private static List<String> COMMANDS;

    private static int CHANCES;
    private static List<String> OUTOFCHANCES;

    private static boolean USEANTILAG;
    private static int TPSCOUNTER;

    private static boolean GENERATE_LOG;

    public static void reloadMessages(Main main) {
	File file = new File(main.getDataFolder(), "config.yml");

	FileConfiguration config = YamlConfiguration.loadConfiguration(file);
	UPDATER = config.getBoolean("updater");
	SHOULD_UPDATE = config.getBoolean("inform-update");

	REPORT_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("REPORT_MESSAGE"));

	BLOCK_SAFETY = config.getBoolean("block-saftey");
	PLAYER_COOLDOWN = config.getInt("playercooldown");
	NPC_LIFE = config.getInt("npclife");
	SPAWN_RATE = config.getInt("spawnrate");
	NPCNAME = config.getString("npcname");
	COMBAT_TIME = config.getInt("combattime");

	KILL_PLAYER = config.getBoolean("killplayer");
	REPORT = config.getString("report");
	COMMANDS = config.getStringList("commands");

	CHANCES = config.getInt("chances");
	OUTOFCHANCES = config.getStringList("outofchances");

	USEANTILAG = config.getBoolean("use-antilag");
	TPSCOUNTER = config.getInt("tps-counter");

	GENERATE_LOG = config.getBoolean("generatelog");
    }

    public static boolean useAntiLag() {
	return USEANTILAG;
    }

    public static int getTPSCounter() {
	return TPSCOUNTER;
    }

    public static boolean blockSafety() {
	return BLOCK_SAFETY;
    }

    public static boolean shouldUpdate() {
	return UPDATER;
    }

    public static boolean informUpdate() {
	return SHOULD_UPDATE;
    }

    public static String getREPORT_MESSAGE() {
	return REPORT_MESSAGE;
    }

    public static int getPLAYER_COOLDOWN() {
	return PLAYER_COOLDOWN;
    }

    public static int getNPC_LIFE() {
	return NPC_LIFE;
    }

    public static int getSPAWN_RATE() {
	return SPAWN_RATE;
    }

    public static String getNPCNAME() {
	return NPCNAME;
    }

    public static int getCOMBAT_TIME() {
	return COMBAT_TIME;
    }

    public static boolean isKILL_PLAYER() {
	return KILL_PLAYER;
    }

    public static String getREPORT() {
	return REPORT;
    }

    public static List<String> getCOMMANDS() {
	return COMMANDS;
    }

    public static int getCHANCES() {
	return CHANCES;
    }

    public static List<String> getOUTOFCHANCES() {
	return OUTOFCHANCES;
    }

    public static boolean isGENERATE_LOG() {
	return GENERATE_LOG;
    }
}
