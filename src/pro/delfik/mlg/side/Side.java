package pro.delfik.mlg.side;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import pro.delfik.mlg.MLGRush;
import pro.delfik.mlg.Sector;

import static pro.delfik.mlg.Sector.addDust;

public abstract class Side {
	private Location loc;
	private Location[] bed;
	private int points = 0;
	private Player p;
	public int beds = 0;
	public int deaths = 0;
	//private final Sector s;
	
	protected Side(Sector s, Player p) {
		this.p = p;
		loc = MLGRush.sectorize(getDefaultLocation(), s.id);
		bed = MLGRush.sectorize(getDefaultBed(), s.id);
	}
	
	public void respawn(boolean count) {
		p.teleport(getLocation());
		Inventory inv = p.getInventory();
		inv.clear();
		inv.setItem(0, MLGRush.stick);
		ItemStack is = new ItemStack(Material.SANDSTONE, 64, (short) 2);
		for (int i = 1; i < 9; i++) inv.setItem(i, is);
		p.updateInventory();
		if (count) deaths++;
	}
	
	public void breakEnemy(Scoreboard sb) {
		
		addDust(p.getName(), 10);
		Score sc = sb.getObjective(DisplaySlot.SIDEBAR).getScore("§e" + getPlayer().getName());
		sc.setScore(sc.getScore() + 1);
		setPoints(getPoints() + 1);
		beds++;
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public int getPoints() {
		return points;
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public Location[] getBed() {
		return bed;
	}
	
	protected void setPoints(int points) {
		this.points = points;
	}
	
	protected void setLoc(Location loc) {
		this.loc = loc;
	}
	
	protected void setBed(Location[] bed) {
		this.bed = bed;
	}
	
	protected void setPlayer(Player p) {
		this.p = p;
	}
	
	public abstract Location getDefaultLocation();
	public abstract Location[] getDefaultBed();
}
