package pro.delfik.mlg;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.libs.org.ibex.nestedvm.util.Platform;
import org.bukkit.entity.Player;

import java.util.List;

public class Cooldown {
    private String name;
    private int remain;
    private int task;
    private List<Player> players;
    private Runnable action;

    public Cooldown(String name, int seconds, List<Player> p, Runnable action) {
        this.name = name;
        this.remain = seconds;
        this.players = p;
        this.action = action;
        task = task();
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(task);
        for (Player p : players == null ? Bukkit.getOnlinePlayers() : players) {
            MLGRush.sendTitle("§f", p);
            MLGRush.sendSubtitle("§eОтсчёт отменён.", p);
        }
    }

    public int task() {

        return Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.plugin, () -> {
            remain--;
            for (Player p : players == null ? Bukkit.getOnlinePlayers() : players) {
                MLGRush.sendTitle("§f", p);
                MLGRush.sendSubtitle("§a§l" + remain, p);
                p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1, 1);
            }
            if (remain <= 0) {
                Bukkit.getScheduler().cancelTask(task);
                action.run();
            }
        }, 20L, 20L);
    }

    public String getName() {
        return name;
    }

    public int getRemain() {
        return remain;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getTask() {
        return task;
    }

    public Runnable getAction() {
        return action;
    }
}
