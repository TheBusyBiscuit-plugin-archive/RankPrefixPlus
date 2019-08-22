package io.github.thebusybiscuit.rankprefixplus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import io.github.thebusybiscuit.cscorelib2.config.Config;
import io.github.thebusybiscuit.cscorelib2.updater.BukkitUpdater;
import io.github.thebusybiscuit.cscorelib2.updater.GitHubBuildsUpdater;
import io.github.thebusybiscuit.cscorelib2.updater.Updater;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;

public class RankPrefixPlus extends JavaPlugin {
	
	@Getter
	private static RankPrefixPlus instance;
	
	@Getter
	private Config cfg;
	
	@Getter
	private Map<String, Rank> ranks = new HashMap<>();
	
	private boolean isPlaceholderAPIloaded = false;
	
	@Override
	public void onEnable() {
		instance = this;
		cfg = new Config(this);
		
		// Setting up bStats
		new Metrics(this);
		
		// Setting up the Auto-Updater
		Updater updater;
		
		if (!getDescription().getVersion().startsWith("DEV - ")) {
			// We are using an official build, use the BukkitDev Updater
			updater = new BukkitUpdater(this, getFile(), 84619);
		}
		else {
			// If we are using a development build, we want to switch to our custom 
			updater = new GitHubBuildsUpdater(this, getFile(), "TheBusyBiscuit/RankPrefixPlus/master");
		}
		
		if (cfg.getBoolean("options.auto-update")) updater.start();
		
		
		new ChatListener(this);
		reloadSettings();
		
		isPlaceholderAPIloaded = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
		
		if (cfg.getBoolean("options.use-scoreboard-teams")) {
			getServer().getScheduler().runTaskTimer(this, () -> {
				
				for (Player p: Bukkit.getOnlinePlayers()) {
					updateScoreboard(p);
				}
				
			}, 0L, cfg.getInt("options.update-delay-in-ticks"));
		}
	}
	
	@Override
	public void onDisable() {
		instance = null;
	}
	
	private void reloadSettings() {
		ranks.clear();
		cfg.reload();
		
		for (String rank: cfg.getStringList("ranks.order")) {
			String path = "ranks." + rank;
			cfg.setDefaultValue(path + ".prefix", "&7");
			cfg.setDefaultValue(path + ".suffix", "&7");
			cfg.setDefaultValue(path + ".required-permission", "RankPrefixPlus." + rank);
			cfg.setDefaultValue(path + ".message-color", "&7");
			cfg.setDefaultValue(path + ".scoreboard.enabled", true);
			cfg.setDefaultValue(path + ".scoreboard.prefix", "&7");
			cfg.setDefaultValue(path + ".scoreboard.suffix", "&7");
			cfg.setDefaultValue(path + ".scoreboard.tab-priority", 1);
			cfg.setDefaultValue(path + ".bypass-OP", false);
			cfg.setDefaultValue(path + ".chat-layout", "&7{PREFIX}{PLAYER}{SUFFIX}: {MESSAGE}");
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
	
	public Rank getRank(Permissible p) {
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
	
	public void updateScoreboard(Player p) {
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
	
	public void updateScoreboard(Player p, Player player) {
		Scoreboard scoreboard = getScoreboard(p);
		loadScoreboardTeam(scoreboard, player);
		p.setScoreboard(scoreboard);
	}

	public String getVariable(RankPrefixPlus plugin, String string) {
		return ChatColor.translateAlternateColorCodes('&', plugin.getCfg().getString("variables." + string));
	}
	
	private void loadScoreboardTeam(Scoreboard scoreboard, Player p) {
		Rank group = getRank(p);
		
		if (group != null && group.hasScoreboard()) {
			String id = "rp" + group.getScoreboardWeight() + p.getEntityId();
			Team team = scoreboard.getTeam(id);
			
			if (team == null) {
				team = scoreboard.registerNewTeam(id);
				
				if (!group.getScoreboardPrefix().equalsIgnoreCase("")) {
					team.setPrefix(ChatColor.translateAlternateColorCodes('&', applyPlaceholders(p, group.getScoreboardPrefix())));
				}
				
				if (!group.getScoreboardSuffix().equalsIgnoreCase("")) {
					team.setSuffix(ChatColor.translateAlternateColorCodes('&', applyPlaceholders(p, group.getScoreboardSuffix())));
				}
				
			}
			
			team.addEntry(p.getName());
		}
	}
	
	private Scoreboard getScoreboard(Player p) {
		if (p.getScoreboard() != null) return p.getScoreboard();
		
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		return manager.getNewScoreboard();
	}

	public String replaceUnicodes(String text) {
		while (text.contains("[unicode: ")) {
            final String unicode = text.substring(text.indexOf('[') + 10, text.indexOf(']'));
            text = text.replace("[unicode: " + unicode + "]", String.valueOf((char)Integer.parseInt(unicode, 16)));
        }
		
		return text;
	}

	public String applyPlaceholders(Player p, String text) {
		if (isPlaceholderAPIloaded) {
			text = PlaceholderAPI.setPlaceholders(p, text);
		}
		
		return text;
	}

}
