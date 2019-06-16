package io.github.thebusybiscuit.rankprefixplus;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import io.github.thebusybiscuit.cscorelib2.config.Config;
import io.github.thebusybiscuit.cscorelib2.updater.BukkitUpdater;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.metrics.bukkit.Metrics;

public class RankPrefixPlus extends JavaPlugin {
	
	public static Config cfg;
	
	@Override
	public void onEnable() {
		cfg = new Config(this);
		new Metrics(this);
		new BukkitUpdater(this, getFile(), 84619);
		
		new ChatListener(this);
		
		reloadSettings();
		
		if (!RankPrefixPlus.cfg.getBoolean("options.use-scoreboard-teams"))  return;
		
		getServer().getScheduler().runTaskTimer(this, () -> {
			for (Player p: Bukkit.getOnlinePlayers()) {
				updateScoreboard(p);
			}
		}, 0L, cfg.getInt("options.update-delay-in-ticks"));
	}
	
	public void onDisable() {
		cfg = null;
		Rank.ranks = null;
	}
	
	private void reloadSettings() {
		Rank.ranks.clear();
		cfg.reload();
		
		for (String rank: cfg.getStringList("ranks.order")) {
			cfg.setDefaultValue("ranks." + rank + ".prefix", "&7");
			cfg.setDefaultValue("ranks." + rank + ".suffix", "&7");
			cfg.setDefaultValue("ranks." + rank + ".required-permission", "RankPrefixPlus." + rank);
			cfg.setDefaultValue("ranks." + rank + ".message-color", "&7");
			cfg.setDefaultValue("ranks." + rank + ".scoreboard.enabled", true);
			cfg.setDefaultValue("ranks." + rank + ".scoreboard.prefix", "&7");
			cfg.setDefaultValue("ranks." + rank + ".scoreboard.suffix", "&7");
			cfg.setDefaultValue("ranks." + rank + ".scoreboard.tab-priority", 1);
			cfg.setDefaultValue("ranks." + rank + ".bypass-OP", false);
			cfg.setDefaultValue("ranks." + rank + ".chat-layout", "&7{PREFIX}{PLAYER}{SUFFIX}: {MESSAGE}");
		}
		cfg.save();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof ConsoleCommandSender || sender.hasPermission("RankPrefixPlus.reload")) {
			reloadSettings();
			sender.sendMessage(ChatColor.GREEN + "The Config File has been reloaded successfully");
		}
		return true;
	}
	
	public static Rank getRank(Player p) {
		for (String rank: cfg.getStringList("ranks.order")) {
			String permission = cfg.getString("ranks." + rank + ".required-permission");
			
			if (permission.equalsIgnoreCase("")) {
				return Rank.get(rank);
			}
			else {
				Rank group = Rank.get(rank);
				if (group.canBypassOP()) {
					if (p.hasPermission(new Permission(permission, PermissionDefault.FALSE))) return group;
				}
				else if (p.hasPermission(permission)) return group;
			}
		}
		return null;
	}
	
	public static boolean isLoaded(String plugin) {
		return Bukkit.getPluginManager().isPluginEnabled(plugin);
	}
	
	public static void updateScoreboard(Player p) {
		Scoreboard scoreboard = getScoreboard(p);
		
		Iterator<Team> iterator = scoreboard.getTeams().iterator();
		while (iterator.hasNext()) {
			iterator.next().unregister();
		}
		
		for (Player player: Bukkit.getOnlinePlayers()) {
			loadScoreboardTeam(scoreboard, player);
		}
		
		p.setScoreboard(scoreboard);
	}
	
	public static void updateScoreboard(Player p, Player player) {
		Scoreboard scoreboard = getScoreboard(p);
		loadScoreboardTeam(scoreboard, player);
		p.setScoreboard(scoreboard);
	}

	public static String getVariable(String string) {
		return ChatColor.translateAlternateColorCodes('&', RankPrefixPlus.cfg.getString("variables." + string));
	}
	
	private static void loadScoreboardTeam(Scoreboard scoreboard, Player p) {
		Rank group = getRank(p);
		if (group.hasScoreboard()) {
			String id = "rp" + group.getTABWeight() + String.valueOf(p.getEntityId());
			Team team = scoreboard.getTeam(id);
			
			if (team == null) {
				team = scoreboard.registerNewTeam(id);
				
				if (!group.getSBPrefix().equalsIgnoreCase("")) {
					team.setPrefix(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(p, group.getSBPrefix())));
				}
				
				if (!group.getSBSuffix().equalsIgnoreCase("")) {
					team.setSuffix(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(p, group.getSBSuffix())));
				}
				
			}
			
			team.addEntry(p.getName());
		}
	}
	
	private static Scoreboard getScoreboard(Player p) {
		if (p.getScoreboard() != null) return p.getScoreboard();
		
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		return manager.getNewScoreboard();
	}

}
