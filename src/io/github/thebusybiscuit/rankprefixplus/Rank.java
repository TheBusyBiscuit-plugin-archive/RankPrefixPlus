package io.github.thebusybiscuit.rankprefixplus;

import java.util.Map;
import java.util.logging.Level;

import lombok.Getter;

public class Rank {
	
	private static final char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

	protected boolean scoreboard;
	protected boolean bypassOP;

	@Getter
	protected String prefix;

	@Getter
	protected String suffix;
	
	@Getter
	protected String scoreboardPrefix;
	
	@Getter
	protected String scoreboardSuffix;
	
	@Getter
	protected String name;
	
	@Getter
	protected String chatFormat;
	
	@Getter
	protected String chatColor;
	
	@Getter
	protected String scoreboardWeight;
	
	public Rank(String name) {
		String path = "ranks." + name;
		
		this.name = name;
		this.prefix = RankPrefixPlus.getInstance().getCfg().getString(path + ".prefix");
		this.suffix = RankPrefixPlus.getInstance().getCfg().getString(path + ".suffix");
		this.scoreboard = RankPrefixPlus.getInstance().getCfg().getBoolean(path + ".scoreboard.enabled");
		this.scoreboardPrefix = RankPrefixPlus.getInstance().getCfg().getString(path + ".scoreboard.prefix");
		this.scoreboardSuffix = RankPrefixPlus.getInstance().getCfg().getString(path + ".scoreboard.suffix");
		this.chatColor = RankPrefixPlus.getInstance().getCfg().getString(path + ".message-color");
		this.bypassOP = RankPrefixPlus.getInstance().getCfg().getBoolean(path + ".bypass-OP");
		this.chatFormat = RankPrefixPlus.getInstance().getCfg().getString(path + ".chat-layout");
		int index = chars.length - RankPrefixPlus.getInstance().getCfg().getInt(path + ".scoreboard.tab-priority");
		
		if (index < 0 || index > chars.length) {
			this.scoreboardWeight = "A";
			RankPrefixPlus.getInstance().getLogger().log(Level.WARNING, "Rank \"" + name + "\" has a TAB Priority higher than " + chars.length + " or smaller than 0");
			RankPrefixPlus.getInstance().getLogger().log(Level.WARNING, "This is not allowed and must be immediately fixed in the config.yml!");
		}
		else {
			this.scoreboardWeight = String.valueOf(chars[index]);
		}
		
		RankPrefixPlus.getInstance().getRanks().put(name, this);
	}
	
	public boolean hasScoreboard() {		
		return scoreboard;
	}
	
	public boolean canBypassOP() {
		return bypassOP;
	}

	public static Rank get(String rank) {
		Map<String, Rank> ranks = RankPrefixPlus.getInstance().getRanks();
		return ranks.containsKey(rank) ? ranks.get(rank): new Rank(rank);
	}
	
}
