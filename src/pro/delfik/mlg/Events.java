package pro.delfik.mlg;

import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.WorldServer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.scoreboard.DisplaySlot;
import pro.delfik.mlg.interact.Queue;
import pro.delfik.mlg.side.Side;

import java.util.HashMap;

public class Events implements Listener {

	public static final HashMap<Player, Integer> hits = new HashMap<>();
	
	@EventHandler(ignoreCancelled = true)
	public void onDamage(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player && e.getDamager() instanceof Player)) return;
		Player p = ((Player) e.getEntity());
		hits.putIfAbsent(p, 0);
		hits.put(p, hits.get(p) + 1);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onChat(AsyncPlayerChatEvent e) {
		if (e.getMessage().startsWith("!")) {
			e.setMessage(e.getMessage().substring(1));
			return;
		}
		Sector s = Sector.byname.get(e.getPlayer().getName());
		if (s == null) return;
		e.setCancelled(true);
		for (Side side : s.sides) side.getPlayer().sendMessage("§7(Игра) " + e.getPlayer().getDisplayName() + "§7: §f" + e.getMessage());
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e) {
		if (e.getPlayer().getGameMode() != GameMode.CREATIVE) e.setCancelled(true);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getWhoClicked().getGameMode() != GameMode.CREATIVE) e.setCancelled(true);
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if (!p.getWorld().equals(MLGRush.w)) return;
		// Проверка на падение с обрыва и респавн
		if (p.getLocation().getY() > MLGRush.fallY) return;
		Sector s = Sector.byname.get(p.getName());
		if (s != null) s.getSide(p.getName()).respawn(true);
		else if (p.getLocation().getY() < 0) e.getPlayer().teleport(MLGRush.spawn);
	}
	
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		if (!p.getWorld().equals(MLGRush.w)) return;
		Sector s = Sector.byname.get(p.getName());
		if (s == null && p.getGameMode() != GameMode.CREATIVE) e.setCancelled(true);
		else s.blocks.add(e.getBlock().getLocation());
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.setJoinMessage("");
		e.getPlayer().teleport(MLGRush.spawn);
		MLGRush.equip(e.getPlayer());
		e.getPlayer().setGameMode(GameMode.SURVIVAL);
		//e.getPlayer().setMaximumNoDamageTicks(0);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		e.setQuitMessage("");
		Sector s = Sector.byname.get(e.getPlayer().getName());
		if (Queue.waiting != null) if (Queue.waiting.getName().equals(e.getPlayer().getName())) Queue.waiting = null;
		if (s == null) return;
		s.clearArea();
		if (s.sb.getObjective(DisplaySlot.SIDEBAR).getScore("§bВремя до конца игры").getScore() > 240) {
			s.unfair();
			return;
		}
		s.end(s.opposite(s.getSide(e.getPlayer().getName())));
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
			if (MLGRush.compare(e.getPlayer().getItemInHand(), MLGRush.queue, false) || MLGRush.compare(e.getPlayer().getItemInHand(), MLGRush.leavequeue, true)) {
				if (Queue.waiting != null) if (Queue.waiting.getName().equals(e.getPlayer().getName())) {
					Queue.waiting = null;
					e.getPlayer().sendMessage("§6Вы вышли из очереди.");
					e.getPlayer().getInventory().setItem(0, MLGRush.queue);
					e.getPlayer().updateInventory();
					return;
				}
				if (Queue.join(e.getPlayer())) {
					e.getPlayer().sendMessage("§aВы вошли в очередь.");
					e.getPlayer().updateInventory();
					return;
				}
				else e.getPlayer().sendMessage("§cПодождите немного...");
				return;
			}
			return;
		}
		if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;
		if (e.getClickedBlock().getType() == Material.SANDSTONE) {
			WorldServer nmsWorld = ((CraftWorld) e.getPlayer().getWorld()).getHandle();
			Location block = e.getClickedBlock().getLocation();
			nmsWorld.setAir(new BlockPosition(block.getBlockX(), block.getBlockY(), block.getBlockZ()), true);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (e.getPlayer().getGameMode() != GameMode.CREATIVE) e.setCancelled(true); else return;
		if (e.getBlock().getType() == Material.BED_BLOCK) {
			Sector s = Sector.byname.get(e.getPlayer().getName());
			Side side = s.opposite(s.getSide(e.getPlayer().getName()));
			if (!side.getBed()[0].equals(e.getBlock().getLocation()) && !side.getBed()[1].equals(e.getBlock().getLocation())) {
				e.getPlayer().sendMessage("§cТы ломаешь какую-то неправильную кровать...");
				return;
			}
			s.breakBed(e.getPlayer().getName(), side);
			
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
			e.setCancelled(true);
			return;
		}
		if (e.getEntity() instanceof Player) {
			e.setDamage(0);
			String name = e.getEntity().getName();
			if (!Sector.byname.containsKey(name)) e.setCancelled(true);
		}
	}

	@EventHandler
	public void event(PlayerBedEnterEvent event){
		event.setCancelled(true);
		event.getPlayer().sendMessage("§cПрости, тебе нельзя спать");
	}
}
