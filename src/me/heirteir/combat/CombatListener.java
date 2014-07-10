package me.heirteir.combat;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import me.heirteir.antiff.Main;
import me.heirteir.antiff.config.Configurations;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class CombatListener implements Listener {
    public CopyOnWriteArrayList<String> time = new CopyOnWriteArrayList<String>();
    public ArrayList<Player> remove = new ArrayList<Player>();

    public CombatListener(Main main) {
	new BukkitRunnable() {
	    @Override
	    public void run() {
		for (String s : time) {
		    int ctime = Integer.parseInt(s.split("-")[1]);
		    String name = s.split("-")[0];
		    time.remove(s);
		    time.add(name + "-" + (ctime - 1));
		    if ((ctime - 1) == 0)
			time.remove(name + "-" + (ctime - 1));
		}
	    }
	}.runTaskTimer(main, 0L, 20L);
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent e) {
	if (contains(e.getPlayer().getName()))
	    time.remove(getIndex(e.getPlayer().getName()));
    }

    @EventHandler
    public void playerKick(PlayerKickEvent e) {
	if (contains(e.getPlayer().getName()))
	    time.remove(getIndex(e.getPlayer().getName()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAttack(EntityDamageByEntityEvent e) {
	if (e.isCancelled())
	    return;

	if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player))
	    return;

	Player damaged = (Player) e.getEntity();
	Player hitter = (Player) e.getDamager();

	if (!damaged.hasPermission("antiff.bypass")) {
	    if (contains(damaged.getName()))
		time.remove(getIndex(damaged.getName()));
	    time.add(damaged.getName() + "-" + Configurations.getCOMBAT_TIME());
	}
	if (!hitter.hasPermission("antiff.bypass")) {
	    if (contains(hitter.getName()))
		time.remove(getIndex(hitter.getName()));
	    time.add(hitter.getName() + "-" + Configurations.getCOMBAT_TIME());
	}
    }

    public boolean contains(String playername) {
	for (String s : time) {
	    if (s.startsWith(playername))
		return true;
	}
	return false;
    }

    private String getIndex(String playername) {
	for (String s : time) {
	    if (s.startsWith(playername)) {
		return playername + "-" + s.split("-")[1];
	    }
	}
	return null;
    }
}
