package pro.delfik.mlg;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import pro.delfik.mlg.side.BlueSide;
import pro.delfik.mlg.side.RedSide;
import pro.delfik.mlg.side.Side;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class Sector {
	
	public static final int SECTORS_IN_ROW = 6;
	public static volatile int MAPS = YamlConfiguration.loadConfiguration(new File("settings.yml")).getInt("map.cl");
	
	// blue 0 | red 1
	public static Sector[] ingame = new Sector[MAPS * SECTORS_IN_ROW];
	public static final HashMap<String, Sector> byname = new HashMap<>();
	
	public final Scoreboard sb;
	public LinkedList<Location> blocks = new LinkedList<>();
	public final int id;
	
	private final RedSide red;
	private final BlueSide blue;
	public final Side[] sides;
	public BukkitTask task;
	
	public Sector(Player blue, Player red) {
		this(blue, red, -1);
	}

	public Sector(Player blue, Player red, int sector) throws IllegalArgumentException{
		if (sector == -1) id = randomEmpty(); else id = sector;
		ingame[id] = this;
		this.red = new RedSide(this, red);
		this.blue = new BlueSide(this, blue);
		sides = new Side[] {this.red, this.blue};
		for (Side s : sides) {
			if (!s.getPlayer().isOnline()) {
				Side a = opposite(s);
				if (a.getPlayer().isOnline()) {
					a.getPlayer().sendMessage("§6Ваш соперник испугался и ливнул. Земля ему пухом.");
					a.getPlayer().getInventory().setItem(0, MLGRush.queue);
				}
				byname.remove(a.getPlayer().getName());
				throw new IllegalArgumentException(s.getPlayer().getName());
			}
			byname.put(s.getPlayer().getName(), this);
		}
		sb = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective o = sb.registerNewObjective("dummy", "dummy");
		o.setDisplaySlot(DisplaySlot.SIDEBAR);
		o.getScore("§e" + blue.getName()).setScore(0);
		o.getScore("§e" + red.getName()).setScore(0);
		Score score = o.getScore("§bВремя до конца игры");
		o.setDisplayName("§a§lMLGRush");
		for (Side s : sides) {
			s.getPlayer().sendMessage("Вы играете в секторе §e" + id/SECTORS_IN_ROW + "-" + id%SECTORS_IN_ROW + "§f против " + opposite(s).getPlayer().getName());
			s.getPlayer().setScoreboard(sb);
			Events.hits.put(s.getPlayer(), 0);
		}
		start(score);
	}
	
	
	public static int randomEmpty() {
		List<Integer> integers = new LinkedList<>();
		Random r = new Random();
		for (int i = 0; i < ingame.length; i++) {
			if (ingame[i] == null) integers.add(i);
		}
		if (integers.isEmpty()) return -1;
		int se = integers.get(r.nextInt(integers.size()));
		try {if (ingame[se] != null) return randomEmpty();}
		catch (StackOverflowError e) {return -1;}
		return se;
	}
	
	public static int firstEmpty() {
		for (int i = 0; i < ingame.length; i++) if (ingame[i] == null) return i;
		throw new IllegalStateException("All sectors are ingame");
	}
	
	
	
	public void start(final Score o) {
		for (Side s : sides) {
			MLGRush.sendTitle("§aИгра началась!", s.getPlayer());
			s.respawn(false);
			s.getPlayer().setGameMode(GameMode.SURVIVAL);
		}
		o.setScore(300);
		task = new BukkitRunnable() {@Override public void run() {
			int score = o.getScore();
			if (score == 0) {
				Side s = null;
				int max = 0;
				int redpoints = red.getPoints();
				int bluepoints = blue.getPoints();
				if (redpoints > bluepoints) s = red;
				if (redpoints < bluepoints) s = blue;
				end(s);
				return;
			}
			o.setScore(score - 1);
		}}.runTaskTimer(MLGRush.plugin, 20L, 20L);
	}

	public Side getSide(String name) {
		if (red.getPlayer().getName().equals(name)) return red;
		else return blue;
	}
	
	public void breakBed(String name, Side s) {
		clearArea();
		opposite(s).breakEnemy(sb);
		for (Side side : sides) {
			side.getPlayer().sendMessage("§e" + name + "§a сломал вражескую кровать! §6" + side.getPoints() + ":" + this.opposite(side).getPoints());
			if (side.beds > 4) {
				end(side);
				return;
			} else side.respawn(true);
		}
	}
	
	public void clearArea() {
		blocks.forEach((l) -> l.getBlock().setType(Material.AIR));
		blocks = new LinkedList<>();
	}
	
	public Side opposite(Side s) {
		if (s instanceof RedSide) return blue;
		else return red;
	}
	
	public void end(Side winner) {
		task.cancel();
		String score = red.getPoints() + ":" + blue.getPoints();
		clearArea();
		ingame[id] = null;
		for (Side s : sides) {
			if (Events.hits.get(s.getPlayer()) < 10) {
				unfair(true);
				return;
			}
		}
		if (winner == null) {
			for (Side s : sides) {
				Player p = s.getPlayer();
				if (p == null) continue;
				p.sendMessage("MLG §e> §f§lНичья.");
				MLGRush.sendTitle("Ничья!", p);
				p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
				byname.remove(p.getName());
				p.getInventory().clear();
				new Cooldown("endgame", 5, Collections.singletonList(p), () -> {
					p.teleport(MLGRush.w.getSpawnLocation());
					MLGRush.equip(p);
				});
				addDust(p.getName(), 30);
			}
			return;
		}
		addDust(winner.getPlayer().getName(), 50);
		MLGRush.sendTitle("§aПобеда!", winner.getPlayer());
		MLGRush.sendSubtitle("§aВы победили со счётом §6" + score, winner.getPlayer());
		for (Side s : sides) {
			if (!s.equals(winner)) {
				MLGRush.sendTitle("§cПоражение!", s.getPlayer());
				addDust(s.getPlayer().getName(), 10);
			}
			Player p = s.getPlayer();
			if (p == null) continue;
			p.sendMessage("§8[§dMLG§8] §eИгрок §f" + winner.getPlayer().getName() + "§e победил со счётом §f" + score + "§e!");
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			byname.remove(p.getName());
			p.getInventory().clear();
			new Cooldown("endgame", 5, Collections.singletonList(p), () -> {
				p.teleport(MLGRush.w.getSpawnLocation());
				MLGRush.equip(p);
			});
		}
	}
	
	private int get(String[] array, int element) {
		try {return Integer.parseInt(array[element]);}
		catch (Exception e) {return 0;}
	}
	
	public void unfair() {unfair(false);}
	public void unfair(boolean b) {
		task.cancel();
		clearArea();
		ingame[id] = null;
		for (Side s : sides) {
			Player p = s.getPlayer();
			if (!p.isOnline()) continue;
			p.teleport(MLGRush.w.getSpawnLocation());
			p.sendMessage(b ? "§8[§dMLG§8] §eИгра признана нечестной и не засчитана." :
					"§8[§dMLG§8] §eВаш соперник сбежал, а минуты времени от игры не прошло. Игра не засчитана.");
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			byname.remove(p.getName());
			MLGRush.equip(p);
		}
	}
	
	public static void addDust(String player, int dust) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mysterydust add " + player + " " + dust);
	}
}
