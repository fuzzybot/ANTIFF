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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.earth2me.essentials.Essentials;

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

	beginTask(main);

	this.hasEssentials = hasEssentials;

	if (hasEssentials)
	    ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

	np = new NPCFactory(main);
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

    @EventHandler
    public void NPCDamage(NPCDamageEvent e) {
	if (!e.getNpc().getBukkitEntity().isDead()) {
	    e.getNpc().getBukkitEntity().teleport(Bukkit.getWorlds().get(0).getSpawnLocation().subtract(0, 75, 0));
	    e.getNpc().getBukkitEntity().remove();
	}

	if (!e.getCause().equals(DamageCause.ENTITY_ATTACK))
	    return;

	if (!(e.getDamager() instanceof Player))
	    return;

	final Player playerhitter = (Player) e.getDamager();

	if (!suspects.containsKey(playerhitter.getName()))
	    return;

	if (!suspects.get(playerhitter.getName()).equals(e.getNpc().getBukkitEntity().getUniqueId().toString()))
	    return;

	if (Configurations.isKILL_PLAYER())
	    playerhitter.setHealth(0.0);

	suspects.remove(playerhitter.getName());

	if (Configurations.getREPORT().equalsIgnoreCase("all"))
	    Bukkit.broadcastMessage(Configurations.getREPORT_MESSAGE());
	else if (Configurations.getREPORT().equalsIgnoreCase("staff")) {
	    for (Player staff : Bukkit.getOnlinePlayers()) {
		if (!staff.hasPermission("antiff.notify"))
		    continue;
		staff.sendMessage(ChatColor.translateAlternateColorCodes('&', Configurations.getREPORT_MESSAGE().replace("%player%", playerhitter.getName())));
	    }

	    List<String> commands = Configurations.getCOMMANDS();

	    for (String s : commands) {
		String command = s.replace("%player%", playerhitter.getName()).replace("/", "");
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	    }

	    if (Configurations.isGENERATE_LOG())
		new UserEditor(playerhitter, main).updatePlayer();
	}
    }

    public void beginTask(final Main main) {

	new BukkitRunnable() {

	    @Override
	    public void run() {
		try {
		    if (main.combat.time.isEmpty())
			return;

		    ArrayList<String> temp = new ArrayList<String>();

		    for (String s : main.combat.time) {
			temp.add(s.split("-")[0]);
		    }

		    if (temp.size() <= 0)
			return;

		    final Player playerhitter = Bukkit.getPlayer(temp.get(new Random().nextInt(temp.size())));

		    if (cooldown.contains(playerhitter.getName()))
			return;

		    if (suspects.containsKey(playerhitter.getName()))
			return;

		    if (main.combat.contains(playerhitter.getName())) {

			if (playerhitter.getLocation().getBlock().isLiquid() || playerhitter.getLocation().getBlock().getType().equals(Material.LADDER)
				|| playerhitter.getLocation().getBlock().getType().equals(Material.VINE))
			    return;

			String name = NameGen.newName();

			final NPC npc = np.spawnHumanNPC(playerhitter.getLocation().subtract(playerhitter.getEyeLocation().getDirection().normalize()).add(0, 3, 0), new NPCProfile(name));

			npc.setInvulnerable(false);
			npc.setGravity(false);

			if (Configurations.vanishNPC())
			    npc.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20, 2));

			for (Player p : Bukkit.getOnlinePlayers()) {
			    if (!p.getName().equalsIgnoreCase(playerhitter.getName())) {
				if (!p.hasPermission("antiff.shownpcs"))
				    hider.hideEntity(p, npc.getBukkitEntity());
			    }
			}

			if (hasEssentials) {
			    if (!ess.getUser(name).isNPC()) {
				ess.getUser(name).setNPC(true);
				ess.getUser(name).setPlayerListName("");
			    }
			}

			cooldown.add(playerhitter.getName());

			PlayerListener.suspects.put(playerhitter.getName(), npc.getBukkitEntity().getUniqueId().toString());

			new BukkitRunnable() {

			    @Override
			    public void run() {
				if (!playerhitter.isOnline()) {
				    hider.hideEntity(playerhitter, npc.getBukkitEntity());
				    npc.getBukkitEntity().remove();
				    PlayerListener.suspects.remove(playerhitter.getName());
				}
				if (!npc.getBukkitEntity().isDead())
				    npc.getBukkitEntity().teleport(playerhitter.getLocation().subtract(playerhitter.getEyeLocation().getDirection().normalize()).add(0, 3, 0));
				else {
				    this.cancel();
				}
			    }
			}.runTaskTimer(main, 0, 0L);

			Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
			    public void run() {
				cooldown.remove(playerhitter.getName());
			    }
			}, Configurations.getPLAYER_COOLDOWN());

			Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
			    public void run() {
				if (!npc.getBukkitEntity().isDead()) {
				    hider.hideEntity(playerhitter, npc.getBukkitEntity());
				    npc.getBukkitEntity().remove();
				    PlayerListener.suspects.remove(playerhitter.getName());
				}
			    }
			}, 8);
		    }
		} catch (NullPointerException e) {
		    return;
		}
	    }
	}.runTaskTimer(main, 0L, Configurations.getSPAWN_RATE());
    }

    public void automaticProcess() {
	new BukkitRunnable() {
	    public void run() {
		if (np.getNPCs().size() > 0) {
		    for (NPC npc : np.getNPCs()) {
			npc.getBukkitEntity().remove();
		    }
		}
	    }
	}.runTaskTimer(main, 0L, (long) (8 + Configurations.getSPAWN_RATE()));
    }
}
