package pro.delfik.mlg.interact;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pro.delfik.mlg.Cooldown;
import pro.delfik.mlg.MLGRush;
import pro.delfik.mlg.Sector;

import java.util.Arrays;

public class Queue {
	public static volatile Player waiting = null;
	public static boolean join(Player p) {
		if (waiting == null) {
			waiting = p;
			p.getInventory().setItem(0, MLGRush.leavequeue);
			return true;
		}
		Player[] players = new Player[] {p, waiting};
		for (Player pp : players) {
			pp.sendMessage("§aСоперник найден, игра начинается!");
			pp.getInventory().setItem(0, new ItemStack(Material.AIR));
			pp.updateInventory();
		}
		Player p2 = waiting;
		new Cooldown("queue", 4, Arrays.asList(players), () -> {try {new Sector(p, p2);} catch (IllegalArgumentException ignored) {}});
		waiting = null;
		return true;
	}
	
}
