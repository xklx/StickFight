package pro.delfik.mlg.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.delfik.mlg.MLGRush;
import pro.delfik.mlg.Sector;

public class CommandCapitulate implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String strCmd, String[] args){
		Sector s = Sector.byname.get(sender.getName());
		if (s == null) {
			sender.sendMessage("§6Вы не можете сдаться, не находясь в игре.");
			return false;
		}
		if (args.length != 0 && (args[0].equals("confirm=") || args[0].equals("="))) {
			sender.sendMessage("§6Вы сдались.");
			s.end(s.opposite(s.getSide(sender.getName())));
			return true;
		}
		sender.sendMessage("§6Вы ввели команду, которая позволяет мгновенно проиграть (сдаться)");
		((Player) sender).spigot().sendMessage(MLGRush.run("§6§nНажмите сюда, чтобы подтвердить ввод команды",
				"§eЛучше не тыкай, ты сможешь выиграть, я верю в тебя!", "/capitulate confirm="));
		return true;
	}
}
