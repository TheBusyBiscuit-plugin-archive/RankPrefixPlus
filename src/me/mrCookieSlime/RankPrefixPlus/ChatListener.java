package me.mrCookieSlime.RankPrefixPlus;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.earth2me.essentials.Essentials;
import com.massivecraft.factions.entity.MPlayer;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import io.mazenmc.prisonrankup.objects.PRPlayer;
import me.mrCookieSlime.CSCoreLibPlugin.general.Math.DoubleHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Player.PlayerStats;
import me.mrCookieSlime.CSCoreLibPlugin.general.Player.PlayerStats.PlayerStat;
import me.mrCookieSlime.ChatTitles.ChatTitles;
import me.mrCookieSlime.Clickssentials.AFKManager;
import me.mrCookieSlime.Slimefun.Objects.Research;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class ChatListener implements Listener {
	
	public static boolean isChatTitlesInstalled = false;
	public static boolean isVaultInstalled = false;
	public static boolean isTownyInstalled = false;
	public static boolean isFactionsInstalled = false;
	public static boolean isPrisonRankupInstalled = false;
	public static boolean isClickssentialsInstalled = false;
	public static boolean isEssentialsInstalled = false;
	public static boolean isuSkyBlockInstalled = false;
	public static boolean isSlimefunInstalled = false;
	
	public ChatListener(main plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		String message = e.getMessage();
		String chatcolor = main.getRank(e.getPlayer()) != null ? main.getRank(e.getPlayer()).getChatColor(): "";
		if (main.cfg.getBoolean("chat.use-layout")) {
			String format = main.cfg.getString("chat.layout");
			
			if (main.cfg.getBoolean("chat.world-specific-layout.enabled")) {
				if (main.cfg.contains("chat.world-specific-layout." + e.getPlayer().getWorld().getName())) format = main.cfg.getString("chat.world-specific-layout." + e.getPlayer().getWorld().getName());
			}
			else if (main.cfg.getBoolean("chat.per-rank-layout")) format = main.getRank(e.getPlayer()).getChatFormat();
			
			while (format.contains("[unicode: ")) {
                final String unicode = format.substring(format.indexOf("[") + 10, format.indexOf("]"));
                format = format.replace("[unicode: " + unicode + "]", String.valueOf((char)Integer.parseInt(unicode, 16)));
            }
			String namecolor = "";
			
			if (isChatTitlesInstalled) {
				chatcolor = !ChatTitles.getChatColor(e.getPlayer()).getPrefix().equalsIgnoreCase("") ? ChatTitles.getChatColor(e.getPlayer()).getPrefix(): chatcolor;
				namecolor = !ChatTitles.getNameColor(e.getPlayer()).getPrefix().equalsIgnoreCase("") ? ChatTitles.getNameColor(e.getPlayer()).getPrefix(): namecolor;
			}
			e.setFormat(ChatColor.translateAlternateColorCodes('&', applyVariables(e.getPlayer(), format)));
			message = ChatColor.translateAlternateColorCodes('&', chatcolor) + message;
		}
		
		for (Player p: Bukkit.getOnlinePlayers()) {
			if (message.contains(p.getName())) {
				if (main.cfg.getBoolean("notifications.name-in-chat.highlight")) message = message.replace(p.getName(), ChatColor.translateAlternateColorCodes('&', "&a@" + p.getName() + chatcolor));
				if (main.cfg.getBoolean("notifications.name-in-chat.sound.enabled")) p.playSound(p.getLocation(), Sound.valueOf(main.cfg.getString("notifications.name-in-chat.sound.sound")), 1F, 1F);
			}
		}
		e.setMessage(main.cfg.getBoolean("chat.force-lower-case") ? message.toLowerCase(): message);
		if (e.getPlayer().hasPermission("RankPrefixPlus.chatcolors")) e.setMessage(ChatColor.translateAlternateColorCodes('&', message));
		if (main.cfg.getBoolean("chat.sound.enabled")) e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.valueOf(main.cfg.getString("chat.sound.sound")), 0.5F, 1F);
		
		if (main.cfg.getBoolean("chat.local.enabled")) {
			Iterator<Player> iterator = e.getRecipients().iterator();
			while (iterator.hasNext()) {
				Player p = iterator.next();
				if (main.cfg.getBoolean("chat.local.per-world") && !e.getPlayer().getWorld().getName().equals(p.getWorld().getName())) iterator.remove();
				if (main.cfg.getInt("chat.local.radius") > 0 && distance(e.getPlayer(), p) > main.cfg.getInt("chat.local.radius")) iterator.remove();
			}
		}
	}
	
	private double distance(Player player, Player p) {
		if (!p.getWorld().getName().equals(player.getWorld().getName())) return Double.MAX_VALUE;
		return player.getLocation().distance(p.getLocation());
	}

	public static String applyVariables(Player p, String format) {
		String chatcolor = main.getRank(p) != null ? main.getRank(p).getChatColor(): "";
		String namecolor = "";
		
		if (isChatTitlesInstalled) {
			chatcolor = !ChatTitles.getChatColor(p).getPrefix().equalsIgnoreCase("") ? ChatTitles.getChatColor(p).getPrefix(): chatcolor;
			namecolor = !ChatTitles.getNameColor(p).getPrefix().equalsIgnoreCase("") ? ChatTitles.getNameColor(p).getPrefix(): namecolor;
		}
		if (format.contains("{PLAYER}")) format = format.replace("{PLAYER}", ChatColor.translateAlternateColorCodes('&', namecolor) + "%s");
		if (format.contains("{MESSAGE}")) format = format.replace("{MESSAGE}", "%s");
		if (format.contains("{PREFIX}")) format = format.replace("{PREFIX}", main.getRank(p).getPrefix());
		if (format.contains("{SUFFIX}")) format = format.replace("{SUFFIX}", main.getRank(p).getSuffix());
		if (format.contains("{WORLD}")) format = format.replace("{WORLD}", p.getWorld().getName());
		if (format.contains("{XPLEVEL}")) format = format.replace("{XPLEVEL}", String.valueOf(p.getLevel()));
		if (format.contains("{KILLS}")) format = format.replace("{KILLS}", String.valueOf(PlayerStats.getStats(p).getStatistic(PlayerStat.PLAYERS_KILLED)));
		if (format.contains("{DEATHS}")) format = format.replace("{DEATHS}", String.valueOf(PlayerStats.getStats(p).getStatistic(PlayerStat.DEATHS)));
		if (format.contains("{KDR}")) {
			long deaths = PlayerStats.getStats(p).getStatistic(PlayerStat.DEATHS);
			format = format.replace("{KDR}", String.valueOf(DoubleHandler.fixDouble(Double.valueOf(String.valueOf(PlayerStats.getStats(p).getStatistic(PlayerStat.PLAYERS_KILLED))) / (deaths == 0 ? 1.0d: Double.valueOf(String.valueOf(deaths))))));
		}
		if (isVaultInstalled ) {
			if (format.contains("{RANK}")) format = format.replace("{RANK}", main.chat.getPrimaryGroup(p));
			if (format.contains("{RANK-PREFIX}")) format = format.replace("{RANK-PREFIX}", main.chat.getPlayerPrefix(p));
			if (format.contains("{RANK-SUFFIX}")) format = format.replace("{RANK-SUFFIX}", main.chat.getPlayerSuffix(p));
			if (format.contains("{MONEY}")) format = format.replace("{MONEY}", String.valueOf(main.economy.getBalance(p)));
		}
		if (isTownyInstalled && format.contains("{TOWNY}")) {
			try {
				Resident resident = TownyUniverse.getDataSource().getResident(p.getName());
				if (resident.hasTown()) {
					Town town = resident.getTown();
					format = format.replace("{TOWNY}", town.hasTag() ? town.getTag(): town.getName());
				}
				else format = format.replace("{TOWNY}", "");
			} catch (Exception e) {
				format = format.replace("{TOWNY}", "");
			}
		}
		if (isFactionsInstalled && format.contains("{FACTION}")) format = format.replace("{FACTION}", MPlayer.get(p).getFactionName());
		if (isPrisonRankupInstalled && format.contains("{PRISONRANK}")) format = format.replace("{PRISONRANK}", new PRPlayer(p.getName()).getCurrentRank().getName());
		if (isChatTitlesInstalled && format.contains("{CHATTITLE}")) format = format.replace("{CHATTITLE}", ChatTitles.getTitle(p).getTitle());
		if (isClickssentialsInstalled && format.contains("{AFK}")) format = format.replace("{AFK}", AFKManager.isAFK(p.getUniqueId()) ? main.getVariable("afk.afk"): main.getVariable("afk.not-afk"));
		else if (isEssentialsInstalled  && format.contains("{AFK}")) format = format.replace("{AFK}", ((Essentials) Bukkit.getPluginManager().getPlugin("Essentials")).getUser(p.getUniqueId()).isAfk() ? main.getVariable("afk.afk"): main.getVariable("afk.not-afk"));
		if (isuSkyBlockInstalled && format.contains("{ISLAND-LEVEL}")) format = format.replace("{ISLAND-LEVEL}", String.valueOf((int) uSkyBlock.getAPI().getIslandLevel(p)));
		if (isSlimefunInstalled && format.contains("{SLIMEFUN-TITLE}")) format = format.replace("{SLIMEFUN-TITLE}", Research.getTitle(p, Research.getResearches(p.getUniqueId())));
		return format;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		main.updateScoreboard(e.getPlayer());
		for (Player p: Bukkit.getOnlinePlayers()) {
			main.updateScoreboard(p, e.getPlayer());
		}
	}

}
