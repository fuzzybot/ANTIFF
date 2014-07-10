package me.heirteir.antiff.commands;

import me.heirteir.antiff.Main;
import me.heirteir.antiff.config.Configurations;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commands implements CommandExecutor {
    Main main;

    public Commands(Main main) {
	this.main = main;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	if (cmd.getName().equalsIgnoreCase("antiff")) {
	    if (args.length == 0) {
		help(sender);
	    } else if (args[0].equalsIgnoreCase("disable")) {
		if (sender.hasPermission("antiff.disable")) {
		    Bukkit.getPluginManager().disablePlugin(main);
		    sender.sendMessage(ChatColor.RED + "Anti-FF Disabled!");
		}
	    } else if (args[0].equalsIgnoreCase("reload")) {
		if (sender.hasPermission("antiff.reload")) {
		    Configurations.reloadMessages(main);
		    sender.sendMessage(ChatColor.GREEN + "Configuration reloaded (" + main.getDescription().getVersion() + ").");
		}
	    } else {
		help(sender);
	    }
	}

	return false;
    }

    public void help(CommandSender sender) {
	if (sender.hasPermission("antiff.disable"))
	    sender.sendMessage(ChatColor.GREEN + "/antiff disable - " + ChatColor.YELLOW + "Disable the plugin if anything goes wrong.");
	if (sender.hasPermission("antiff.reload"))
	    sender.sendMessage(ChatColor.GREEN + "/antiff reload - " + ChatColor.YELLOW + "Reload any changes to the config.");
	if (!sender.hasPermission("antiff.reload") && !sender.hasPermission("antiff.disable"))
	    sender.sendMessage(ChatColor.RED + "You don't have access to any of these commands!");
    }
}
