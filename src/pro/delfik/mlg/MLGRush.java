package pro.delfik.mlg;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.EnumTitleAction;
import net.minecraft.server.v1_8_R1.PacketPlayOutTitle;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import pro.delfik.mlg.command.CommandCapitulate;
import pro.delfik.mlg.command.CommandMLG;
import pro.delfik.mlg.command.CommandSF;
import pro.delfik.mlg.interact.Call;
import pro.delfik.mlg.interact.Queue;
import pro.delfik.mlg.side.BlueSide;
import pro.delfik.mlg.side.RedSide;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MLGRush extends JavaPlugin {
	public static World w = null;
	public static double fallY;
	public static Location spawn;
	public final static ItemStack stick = enchant(create(Material.STICK, 1, (short)0, "§diStick",
			"§b- Лицензированная палка от Apple", "§b §b всего за 29 баксов!"), Enchantment.KNOCKBACK, 1);
	
	public static MLGRush plugin;
	public static final ItemStack queue = enchant(star(Color.LIME, "§6>> §aВойти в очередь §6<<"), Enchantment.LUCK, 1);
	public static final ItemStack leavequeue = enchant(star(Color.RED, "§6>> §cВыйти из очереди §6<<"), Enchantment.LUCK, 1);

	public static void equip(Player p) {
		p.getInventory().clear();
		p.getInventory().setItem(0, queue);
		p.updateInventory();
	}

	@Override
	public void onEnable() {
		plugin = this;
		Bukkit.getPluginManager().registerEvents(new Events(), this);
		Bukkit.getPluginCommand("mlgrush").setExecutor(new CommandMLG());
		Bukkit.getPluginCommand("stickfight").setExecutor(new CommandSF());
		Bukkit.getPluginCommand("capitulate").setExecutor(new CommandCapitulate());
		Call.class.getCanonicalName();
		Queue.class.getCanonicalName();
		
		try {
			w = Bukkit.getWorlds().get(0);
			spawn = w.getSpawnLocation().add(0.5, 0.5, 0.5);
			spawn.setYaw(-90);
		} catch (IndexOutOfBoundsException ignored) {}
		if (w != null) initDefaults();
		fallY = 100;
	}
	
	@EventHandler
	public void onWorld(WorldInitEvent e) {
		if (e.getWorld().getName().startsWith("SF")) {
			w = e.getWorld();
			spawn = w.getSpawnLocation().add(0.5, 0.5, 0.5);
			spawn.setYaw(-90);
			initDefaults();
		}
	}
	
	@Override
	public void onDisable() {
		for (int i = 0; i < Sector.ingame.length; i++) if (Sector.ingame[i] != null) Sector.ingame[i].clearArea();
		for (Entity e : w.getEntities()) if (e.getType() == EntityType.ARMOR_STAND) e.remove();
	}
	
	public static void initDefaults() {
		MLGRush.w = Bukkit.getWorlds().get(0);
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(new File("settings.yml"));
		try {
			BlueSide.defaultLoc = parse(yml.getString("map.blue-spawn"), w);
			RedSide.defaultLoc = parse(yml.getString("map.red-spawn"), w);
			BlueSide.defaultBed = parse(yml.getStringList("map.blue-bed"), w);
			RedSide.defaultBed = parse(yml.getStringList("map.red-bed"), w);
			Sector.MAPS = yml.getInt("map.cl");
		} catch (Exception e) {
			Bukkit.broadcastMessage("§cУ тебя кривые конфиги. Исправляй.");
			Bukkit.getPluginManager().disablePlugin(MLGRush.plugin);
		}
	}
	
	public static Location[] sectorize(Location[] loc, int id) {
		return new Location[] {sectorize(loc[0], id), sectorize(loc[1], id)};
	}
	
	public static Location sectorize(Location loc, int id) {
		int betweenSameMaps = 200;
		int betweenVariousMaps = 200;
		Location l = loc.clone();
		l.setZ(l.getZ() + (betweenSameMaps * (id % Sector.SECTORS_IN_ROW)));
		l.setX(l.getX() + (betweenVariousMaps * (id / Sector.SECTORS_IN_ROW)));
		return l;
	}
	
	public static Location[] parse(List<String> s, World w) {
		Location[] locs = new Location[s.size()];
		for (int i = 0; i < locs.length; i++) {
			locs[i] = parseBlock(s.get(i), w);
		}
		return locs;
	}
	public static Location parseBlock(String s, World w) {
		s = s.replaceAll(" ", "");
		String s1[] = s.split(",");
		if (s1.length == 3) {
			return new Location(w, Integer.decode(s1[0]), Integer.decode(s1[1]), Integer.decode(s1[2]));
		} else return null;
	}
	public static Location parse(String s, World w){
		s = s.replaceAll(" ", "");
		String s1[] = s.split(",");
		if(s1.length == 3) {
			return new Location(w, Integer.decode(s1[0]) + 0.5, Integer.decode(s1[1]) + 0.5, Integer.decode(s1[2])
																									 + 0.5);
		} else if (s1.length == 5) {
			return new Location(w, Integer.decode(s1[0]) + 0.5, Integer.decode(s1[1]) + 0.5, Integer.decode(s1[2]) + 0.5,
									   (float) (Integer.decode(s1[3]) + 0.5), (float) (Integer.decode(s1[4]) + 0.5));
		} else return null;
	}

	public static BaseComponent run(String str, String subStr, String cmd){
		TextComponent c = new TextComponent(str);
		c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				new TextComponent[] {new TextComponent(subStr)}));
		c.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
		return c;
	}

	public static void sendTitle(String title, Player handle) {
		((CraftPlayer)handle).getHandle().playerConnection.sendPacket(
				new PacketPlayOutTitle(EnumTitleAction.TITLE, ChatSerializer.a("{\"text\":\"" + title + "\"," + "\"color\":\"white\"}"))
		);
	}

	public static void sendSubtitle(String subtitle, Player handle) {
		((CraftPlayer)handle).getHandle().playerConnection.sendPacket(
				new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, ChatSerializer.a("{\"text\":\"" + subtitle + "\"," +
						"\"color\":\"white\"}"))
		);
	}

	public static Player unary(CommandSender sender, String selector) {
		Player p = Bukkit.getPlayer(selector);
		if (p == null) return null;
		if (sender instanceof Player) {
			if (((Player) sender).getWorld().equals(p.getWorld())) return p;
			else return null;
		} else return p;
	}

	public static boolean compare(ItemStack a, ItemStack b, boolean dataMatter) {
		if (a.getType() != b.getType()) return false;
		if (dataMatter && a.getDurability() != b.getDurability()) return false;
		if (!(a.hasItemMeta() || b.hasItemMeta())) return true;
		if (a.hasItemMeta() && b.hasItemMeta()) {
			ItemMeta A = a.getItemMeta(), B = b.getItemMeta();
			if (A.hasDisplayName() && B.hasDisplayName())
				if (!A.getDisplayName().equals(B.getDisplayName())) return false;
			if (A.hasLore() && B.hasLore()) if (!A.getLore().equals(B.getLore())) return false;
			return true;
		} else return false;
	}

	public static ItemStack enchant(ItemStack i, Enchantment e, int level) {
		i.addUnsafeEnchantment(e, level);
		return i;
	}

	public static ItemStack create(Material type, int amount, short data, String name, String... lore) {
		return apply(new ItemStack(type, amount, data), name, lore);
	}

	public static ItemStack apply(ItemStack i, String name, String... lore) {
		ItemMeta m = i.getItemMeta();
		if (name != null) m.setDisplayName(name);
		if (lore != null) m.setLore(Arrays.asList(lore));
		m.addItemFlags(ItemFlag.values());
		i.setItemMeta(m);
		return i;
	}

	public static ItemStack star(Color color, String name, String... lore) {
		ItemStack i = create(Material.FIREWORK_CHARGE, 1, (short)0, name, lore);
		ItemMeta m = i.getItemMeta();
		FireworkEffectMeta metaFw = (FireworkEffectMeta) m;
		FireworkEffect aa = FireworkEffect.builder().withColor(color).build();
		metaFw.setEffect(aa);
		m.addItemFlags(ItemFlag.values());
		i.setItemMeta(m);
		return i;
	}
}
