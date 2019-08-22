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

public class ChatListener implements Listener {
	
	private RankPrefixPlus plugin;
	
	public ChatListener(RankPrefixPlus plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		String message = e.getMessage();
		Rank rank = plugin.getRank(e.getPlayer());
		
		String chatcolor = rank != null ? rank.getChatColor(): "";
		
		if (plugin.getCfg().getBoolean("chat.use-layout")) {
			String format = plugin.getCfg().getString("chat.layout");
			
			if (plugin.getCfg().getBoolean("chat.world-specific-layout.enabled")) {
				if (plugin.getCfg().contains("chat.world-specific-layout." + e.getPlayer().getWorld().getName())) {
					plugin.getCfg().getString("chat.world-specific-layout." + e.getPlayer().getWorld().getName());
				}
			}
			else if (plugin.getCfg().getBoolean("chat.per-rank-layout")) {
				format = rank.getChatFormat();
			}
			
			format = plugin.replaceUnicodes(format);
			
			e.setFormat(ChatColor.translateAlternateColorCodes('&', plugin.applyPlaceholders(e.getPlayer(), format)));
			message = ChatColor.translateAlternateColorCodes('&', chatcolor) + message;
		}
		
		for (Player p: Bukkit.getOnlinePlayers()) {
			if (message.contains(p.getName())) {
				if (plugin.getCfg().getBoolean("notifications.name-in-chat.highlight")) message = message.replace(p.getName(), ChatColor.translateAlternateColorCodes('&', "&a@" + p.getName() + chatcolor));
				if (plugin.getCfg().getBoolean("notifications.name-in-chat.sound.enabled")) p.playSound(p.getLocation(), Sound.valueOf(plugin.getCfg().getString("notifications.name-in-chat.sound.sound")), 1F, 1F);
			}
		}
		e.setMessage(plugin.getCfg().getBoolean("chat.force-lower-case") ? message.toLowerCase(): message);
		if (e.getPlayer().hasPermission("RankPrefixPlus.chatcolors")) e.setMessage(ChatColor.translateAlternateColorCodes('&', message));
		if (plugin.getCfg().getBoolean("chat.sound.enabled")) e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.valueOf(plugin.getCfg().getString("chat.sound.sound")), 0.5F, 1F);
		
		if (plugin.getCfg().getBoolean("chat.local.enabled")) {
			Iterator<Player> iterator = e.getRecipients().iterator();
			
			while (iterator.hasNext()) {
				Player p = iterator.next();
				
				if (plugin.getCfg().getBoolean("chat.local.per-world") && !e.getPlayer().getWorld().getName().equals(p.getWorld().getName())) iterator.remove();
				if (plugin.getCfg().getInt("chat.local.radius") > 0 && distance(e.getPlayer(), p) > plugin.getCfg().getInt("chat.local.radius")) iterator.remove();
			}
		}
	}
	
	private double distance(Player player, Player p) {
		if (!p.getWorld().getName().equals(player.getWorld().getName())) return Double.MAX_VALUE;
		return player.getLocation().distance(p.getLocation());
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		plugin.updateScoreboard(e.getPlayer());
		
		for (Player p: Bukkit.getOnlinePlayers()) {
			plugin.updateScoreboard(p, e.getPlayer());
		}
	}

}
