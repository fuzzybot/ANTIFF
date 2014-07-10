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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {

    public static HashMap<String, Integer> hits = new HashMap<String, Integer>();
    public static HashMap<String, String> suspects = new HashMap<String, String>();

    List<String> cooldown = new ArrayList<String>();

    public EntityHider hider;

    Main main;

    PlayerListener plr = this;

    public PlayerListener(Main main) {
	this.main = main;

	// Register Hider
	hider = new EntityHider(main, Policy.BLACKLIST);

	beginTask(main);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerLeave(PlayerQuitEvent e) {
	if (!hits.containsKey(e.getPlayer().getName()))
	    return;
	hits.remove(e.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void NPCDamage(NPCDamageEvent e) {
	if (!(e.getDamager() instanceof Player))
	    return;

	final Player playerhitter = (Player) e.getDamager();

	e.setCancelled(true);

	if (Configurations.isKILL_PLAYER())
	    playerhitter.setHealth(0.0);

	hits.remove(playerhitter.getName());
	suspects.remove(playerhitter.getName());
	e.getNpc().getBukkitEntity().teleport(playerhitter.getLocation().subtract(0, 50, 0));
	if (!e.getNpc().getBukkitEntity().isDead())
	    e.getNpc().getBukkitEntity().remove();

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

		    final NPCFactory np = new NPCFactory(main);

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

			final NPC npc = np.spawnHumanNPC(playerhitter.getLocation().subtract(playerhitter.getEyeLocation().getDirection()).add(0, 3.5, 0), new NPCProfile(Configurations.getNPCNAME()));
			npc.setInvulnerable(false);
			npc.setGravity(false);

			cooldown.add(playerhitter.getName());

			for (Player p : Bukkit.getOnlinePlayers()) {
			    if (!p.getName().equalsIgnoreCase(playerhitter.getName())) {
				if (!p.hasPermission("antiff.shownpcs"))
				    hider.hideEntity(p, npc.getBukkitEntity());
			    }
			}

			PlayerListener.suspects.put(playerhitter.getName(), npc.getBukkitEntity().getUniqueId().toString());

			new BukkitRunnable() {

			    @Override
			    public void run() {
				if (!playerhitter.isOnline()) {
				    hider.hideEntity(playerhitter, npc.getBukkitEntity());
				    npc.getBukkitEntity().remove();
				    PlayerListener.suspects.remove(playerhitter.getName());
				}

				if (!npc.getBukkitEntity().isDead()) {

				    if (Configurations.blockSafety()) {
					if (new Random().nextInt(2) == 1) {
					    npc.getBukkitEntity().teleport(playerhitter.getLocation().add(playerhitter.getEyeLocation().getDirection()).add(0, 2, 0));
					} else
					    npc.getBukkitEntity().teleport(playerhitter.getLocation().subtract(playerhitter.getEyeLocation().getDirection()).add(1, 3.5, 1));
				    } else
					npc.getBukkitEntity().teleport(playerhitter.getLocation().subtract(playerhitter.getEyeLocation().getDirection()).add(1, 3.5, 1));
				} else {
				    this.cancel();
				}
			    }
			}.runTaskTimerAsynchronously(main, 0, 0L);

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
			}, Configurations.getNPC_LIFE());
		    }
		} catch (NullPointerException e) {
		    return;
		}
	    }
	}.runTaskTimer(main, 0L, Configurations.getSPAWN_RATE());
    }
}
