package pro.delfik.mlg.command;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.delfik.mlg.MLGRush;
import pro.delfik.mlg.Sector;
import pro.delfik.mlg.side.BlueSide;
import pro.delfik.mlg.side.RedSide;

public class CommandMLG implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String strCmd, String[] args){
		if(!sender.isOp()){
			sender.sendMessage("§cНету опки, щито поделать");
			return false;
		}
		if(args.length == 0){
			sender.sendMessage("§cИспользование: §e/mlg [Подкоманда]");
			return false;
		}
		Player player = (Player)sender;
		String prefix = "§8[§dMLG§8] §a";
		switch (args[0].toLowerCase()) {
			case "sector": {
				try {
					int sector = Integer.parseInt(args[1]);
					Player p = MLGRush.unary(sender, args[2]);
					if (p == null) sender.sendMessage(prefix + "§cИгрок не найден.");
					new Sector(player, p, sector);
					sender.sendMessage(prefix + "Игра начата.");
					return true;
				} catch (NumberFormatException e) {
					sender.sendMessage(prefix + "§e" + args[1] + "§c - не число.");
					return false;
				} catch (ArrayIndexOutOfBoundsException e) {
					sender.sendMessage(prefix + "§cИспользование: §e/mlg sector [Номер сектора] [Игрок]");
					return false;
				} catch (IllegalArgumentException e){
					sender.sendMessage(prefix + "§eИгрока §f" + e.getMessage() + "§e нету!");
					return false;
				}
			}
			case "setup": {
				if(args.length == 1){
					sender.sendMessage("§cИспользование: §e/mlg setup [Номер сектора]");
					return false;
				}
				int id = Integer.parseInt(args[1]);
				MLGRush.sectorize(RedSide.defaultLoc, id).getBlock().setType(Material.REDSTONE_BLOCK);
				MLGRush.sectorize(BlueSide.defaultLoc, id).getBlock().setType(Material.LAPIS_BLOCK);
				for (Location loc : MLGRush.sectorize(RedSide.defaultBed, id)) loc.getBlock().setType(Material.GOLD_BLOCK);
				for (Location loc : MLGRush.sectorize(BlueSide.defaultBed, id)) loc.getBlock().setType(Material.GOLD_BLOCK);
				sender.sendMessage("§aОтметки для сектора §e" + id + " §aустановлены.");
				return true;
			}
			case "win": {
				Sector sec = Sector.byname.get(sender.getName());
				if (sec == null) {
					player.sendMessage(prefix + "§cВы не в игре");
					return false;
				}
				sender.sendMessage(prefix + "Принудительная победа...");
				sec.end(sec.getSide(sender.getName()));
				return true;
			}
			case "lose": {
				Sector sec = Sector.byname.get(sender.getName());
				if (sec == null) {
					sender.sendMessage(prefix + "§cВы не в игре.");
					return false;
				}
				sender.sendMessage(prefix + "Принудительное поражение...");
				sec.end(sec.opposite(sec.getSide(sender.getName())));
				return true;
			}
			case "break": {
				Sector sec = Sector.byname.get(sender.getName());
				if (sec == null) {
					sender.sendMessage(prefix + "§cВы не в игре.");
					return false;
				}
				sender.sendMessage(prefix + "Ломаем кровать...");
				sec.breakBed(sender.getName(), sec.opposite(sec.getSide(sender.getName())));
				return true;
			}
			case "clear": {
				Sector sec = Sector.byname.get(sender.getName());
				if (sec == null) {
					player.sendMessage(prefix + "§cВы не в игре");
					return false;
				}
				sender.sendMessage(prefix + "Очистка территории...");
				sec.clearArea();
				return true;
			}
			default:
				player.sendMessage(prefix + "Подкоманда не найдена");
				return false;
		}
	}
}
