package io.github.thebusybiscuit.rankprefixplus;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import me.clip.placeholderapi.PlaceholderAPI;

public class ChatListener implements Listener {
	
	public ChatListener(RankPrefixPlus plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		String message = e.getMessage();
		String chatcolor = RankPrefixPlus.getRank(e.getPlayer()) != null ? RankPrefixPlus.getRank(e.getPlayer()).getChatColor(): "";
		if (RankPrefixPlus.cfg.getBoolean("chat.use-layout")) {
			String format = RankPrefixPlus.cfg.getString("chat.layout");
			
			if (RankPrefixPlus.cfg.getBoolean("chat.world-specific-layout.enabled")) {
				if (RankPrefixPlus.cfg.contains("chat.world-specific-layout." + e.getPlayer().getWorld().getName())) format = RankPrefixPlus.cfg.getString("chat.world-specific-layout." + e.getPlayer().getWorld().getName());
			}
			else if (RankPrefixPlus.cfg.getBoolean("chat.per-rank-layout")) format = RankPrefixPlus.getRank(e.getPlayer()).getChatFormat();
			
			while (format.contains("[unicode: ")) {
                final String unicode = format.substring(format.indexOf("[") + 10, format.indexOf("]"));
                format = format.replace("[unicode: " + unicode + "]", String.valueOf((char)Integer.parseInt(unicode, 16)));
            }
			e.setFormat(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(e.getPlayer(), format)));
			message = ChatColor.translateAlternateColorCodes('&', chatcolor) + message;
		}
		
		for (Player p: Bukkit.getOnlinePlayers()) {
			if (message.contains(p.getName())) {
				if (RankPrefixPlus.cfg.getBoolean("notifications.name-in-chat.highlight")) message = message.replace(p.getName(), ChatColor.translateAlternateColorCodes('&', "&a@" + p.getName() + chatcolor));
				if (RankPrefixPlus.cfg.getBoolean("notifications.name-in-chat.sound.enabled")) p.playSound(p.getLocation(), Sound.valueOf(RankPrefixPlus.cfg.getString("notifications.name-in-chat.sound.sound")), 1F, 1F);
			}
		}
		e.setMessage(RankPrefixPlus.cfg.getBoolean("chat.force-lower-case") ? message.toLowerCase(): message);
		if (e.getPlayer().hasPermission("RankPrefixPlus.chatcolors")) e.setMessage(ChatColor.translateAlternateColorCodes('&', message));
		if (RankPrefixPlus.cfg.getBoolean("chat.sound.enabled")) e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.valueOf(RankPrefixPlus.cfg.getString("chat.sound.sound")), 0.5F, 1F);
		
		if (RankPrefixPlus.cfg.getBoolean("chat.local.enabled")) {
			Iterator<Player> iterator = e.getRecipients().iterator();
			
			while (iterator.hasNext()) {
				Player p = iterator.next();
				if (RankPrefixPlus.cfg.getBoolean("chat.local.per-world") && !e.getPlayer().getWorld().getName().equals(p.getWorld().getName())) iterator.remove();
				if (RankPrefixPlus.cfg.getInt("chat.local.radius") > 0 && distance(e.getPlayer(), p) > RankPrefixPlus.cfg.getInt("chat.local.radius")) iterator.remove();
			}
		}
	}
	
	private double distance(Player player, Player p) {
		if (!p.getWorld().getName().equals(player.getWorld().getName())) return Double.MAX_VALUE;
		return player.getLocation().distance(p.getLocation());
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		RankPrefixPlus.updateScoreboard(e.getPlayer());
		for (Player p: Bukkit.getOnlinePlayers()) {
			RankPrefixPlus.updateScoreboard(p, e.getPlayer());
		}
	}

}
