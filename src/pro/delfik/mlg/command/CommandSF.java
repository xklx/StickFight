package pro.delfik.mlg.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.delfik.mlg.MLGRush;
import pro.delfik.mlg.Sector;
import pro.delfik.mlg.interact.Call;

public class CommandSF implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String strCmd, String[] args){
		if(args.length == 0){
			sender.sendMessage("§cИспользование: §e/sf [Игрок]");
			return false;
		}
		Player player = (Player)sender;
		Player receiver = MLGRush.unary(sender, args[0]);
		if(player == null){
			sender.sendMessage("§cшто");
			return false;
		}
		if(receiver == null){
			sender.sendMessage("§8[§dMLG§8] §6Игрок §e" + args[0] + "§6 не найден.");
			return false;
		}
		if (Sector.byname.get(receiver.getName()) != null) {
			sender.sendMessage("§8[§dMLG§8] §6Игрок §e" + receiver.getName() + "§6 уже играет.");
			return false;
		}
		if (Sector.byname.get(player.getName()) != null) {
			sender.sendMessage("§8[§dMLG§8] §6Вы не можете кидать вызовы во время игры.");
			return false;
		}
		if (receiver.getName().equals(player.getName())) {
			sender.sendMessage("§8[§dMLG§8] §6И как ты себе это представляешь? §7(ну ващет изи..(с) 6oogle)");
			return false;
		}
		Call call = Call.list.get(args[0].toLowerCase());
		if (call != null) {
			sender.sendMessage("§8[§dMLG§8] §aПринятие вызова...");
			call.accept();
			return true;
		}
		new Call(player.getName(), receiver.getName()).call();
		sender.sendMessage("§8[§dMLG§8] §aВызов брошен игроку §e" + receiver.getDisplayName());
		return true;
	}
}
