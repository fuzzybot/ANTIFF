package me.heirteir.antiff.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import me.heirteir.antiff.EntityHider;
import me.heirteir.antiff.Main;
import me.heirteir.antiff.Policy;
import me.heirteir.antiff.config.Configurations;
import me.heirteir.antiff.npc.NPC;
import me.heirteir.antiff.npc.NPCDamageEvent;
import me.heirteir.antiff.npc.NPCFactory;
import me.heirteir.antiff.npc.NPCProfile;
import me.heirteir.antiff.npc.NameGen;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class PlayerListener implements Listener {

    public static HashMap<String, String> suspects = new HashMap<String, String>();

    List<String> cooldown = new ArrayList<String>();

    public EntityHider hider;

    Main main;

    PlayerListener plr = this;

    Essentials ess;

    boolean hasEssentials;

    final NPCFactory np;

    public PlayerListener(Main main, boolean hasEssentials) {
	this.main = main;

	// Register Hider
	hider = new EntityHider(main, Policy.BLACKLIST);

	this.hasEssentials = hasEssentials;

	if (hasEssentials)
	    ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

	np = new NPCFactory(main);

	beginTask(main);
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent e) {
	if (!suspects.containsKey(e.getPlayer().getName()))
	    return;
	suspects.remove(e.getPlayer().getName());
    }

    @EventHandler
    public void playerLeave(PlayerKickEvent e) {
	if (!suspects.containsKey(e.getPlayer().getName()))
	    return;
	suspects.remove(e.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void NPCDamage(NPCDamageEvent e) {
	if (!e.getNpc().getBukkitEntity().isDead())
	    e.getNpc().getBukkitEntity().remove();

	if (!e.getCause().equals(DamageCause.ENTITY_ATTACK))
	    return;

	if (!(e.getDamager() instanceof Player))
	    return;

	final Player playerHitter = (Player) e.getDamager();

	if (!suspects.containsKey(playerHitter.getName()))
	    return;

	if (!suspects.get(playerHitter.getName()).equals(e.getNpc().getBukkitEntity().getUniqueId().toString()))
	    return;

	if (Configurations.isKILL_PLAYER())
	    playerHitter.setHealth(0.0);

	suspects.remove(playerHitter.getName());

	if (Configurations.getREPORT().equalsIgnoreCase("all"))
	    Bukkit.broadcastMessage(Configurations.getREPORT_MESSAGE());
	else if (Configurations.getREPORT().equalsIgnoreCase("staff")) {
	    for (Player staff : Bukkit.getOnlinePlayers()) {
		if (!staff.hasPermission("antiff.notify"))
		    continue;
		staff.sendMessage(ChatColor.translateAlternateColorCodes('&', Configurations.getREPORT_MESSAGE().replace("%player%", playerHitter.getName())));
	    }
	}

	List<String> commands = Configurations.getCOMMANDS();

	for (String s : commands) {
	    String command = s.replace("%player%", playerHitter.getName()).replace("/", "");
	    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}

	if (Configurations.isGENERATE_LOG())
	    new UserEditor(playerHitter, main).updatePlayer();
    }

    public void beginTask(final Main main) {
	Bukkit.getScheduler().runTaskTimer(main, new Runnable() {
	    public void run() {

		if (main.combat.time.isEmpty())
		    return;

		ArrayList<String> temp = new ArrayList<String>();

		for (String s : main.combat.time) {
		    if (!cooldown.contains(s) && !suspects.containsKey(s))
			temp.add(s.split("-")[0]);
		}

		@SuppressWarnings("deprecation")
		final Player playerHitter = Bukkit.getPlayer(temp.get(new Random().nextInt(temp.size())));
		temp.clear();

		if (!((Entity) playerHitter).isOnGround())
		    return;

		if (np.getNPCs().size() > 0)
		    np.despawnAll();

		final NPC npc = np.spawnHumanNPC(playerHitter.getLocation().subtract(playerHitter.getEyeLocation().getDirection().normalize()).add(1, 3.5, 1), new NPCProfile(NameGen.newName()));
		npc.setInvulnerable(false);
		npc.setGravity(false);

		npc.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 8, 2));

		for (Player p : Bukkit.getOnlinePlayers()) {
		    if (!p.getName().equalsIgnoreCase(playerHitter.getName()) && !p.hasPermission("antiff.shownpcs"))
			hider.hideEntity(p, npc.getBukkitEntity());
		}

		if (hasEssentials) {
		    User user = ess.getUser(npc.getBukkitEntity().getName());
		    if (!user.isNPC()) {
			user.setNPC(true);
			user.setPlayerListName("");
		    }
		}

		cooldown.add(playerHitter.getName());

		PlayerListener.suspects.put(playerHitter.getName(), npc.getBukkitEntity().getUniqueId().toString());

		Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
		    public void run() {
			cooldown.remove(playerHitter.getName());
		    }
		}, Configurations.getPLAYER_COOLDOWN());

		Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
		    public void run() {
			if (!npc.getBukkitEntity().isDead()) {
			    npc.getBukkitEntity().remove();
			    PlayerListener.suspects.remove(playerHitter.getName());
			}
		    }
		}, 8);
	    }
	}, 0L, Configurations.getSPAWN_RATE());
    }
}
