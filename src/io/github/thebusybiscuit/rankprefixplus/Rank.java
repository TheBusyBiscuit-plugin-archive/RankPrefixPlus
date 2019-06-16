package io.github.thebusybiscuit.rankprefixplus;

import java.util.HashMap;
import java.util.Map;

public class Rank {
	
	public static Map<String, Rank> ranks = new HashMap<String, Rank>();
	private static char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
	
	protected String prefix;
	protected String suffix;
	protected boolean scoreboard, bypassOP;
	protected String scoreboard_prefix;
	protected String scoreboard_suffix;
	protected String name, layout, color;
	protected String tabweight;
	
	public Rank(String name) {
		this.name = name;
		this.prefix = RankPrefixPlus.cfg.getString("ranks." + name + ".prefix");
		this.suffix = RankPrefixPlus.cfg.getString("ranks." + name + ".suffix");
		this.scoreboard = RankPrefixPlus.cfg.getBoolean("ranks." + name + ".scoreboard.enabled");
		this.scoreboard_prefix = RankPrefixPlus.cfg.getString("ranks." + name + ".scoreboard.prefix");
		this.scoreboard_suffix = RankPrefixPlus.cfg.getString("ranks." + name + ".scoreboard.suffix");
		this.color = RankPrefixPlus.cfg.getString("ranks." + name + ".message-color");
		this.bypassOP = RankPrefixPlus.cfg.getBoolean("ranks." + name + ".bypass-OP");
		this.layout = RankPrefixPlus.cfg.getString("ranks." + name + ".chat-layout");
		int index = chars.length - RankPrefixPlus.cfg.getInt("ranks." + name + ".scoreboard.tab-priority");
		
		if (index < 0 || index > chars.length) {
			this.tabweight = "A";
			System.err.println("[RankPrefix+] Rank \"" + name + "\" has a TAB Priority higher than " + chars.length + " or smaller than 0");
			System.out.println("This is not allowed and must be immediately fixed in the config.yml!");
		}
		else this.tabweight = String.valueOf(chars[index]);
		
		ranks.put(name, this);
	}
	
	public String getName() 		{		return name;				}
	public String getPrefix() 		{		return prefix;				}
	public String getSuffix() 		{		return suffix;				}
	public boolean hasScoreboard() 	{		return scoreboard;			}
	public String getSBPrefix() 	{		return scoreboard_prefix;	}
	public String getSBSuffix() 	{		return scoreboard_suffix;	}
	public String getChatColor() 	{		return color;				}
	public boolean canBypassOP() 	{		return bypassOP;			}
	public String getChatFormat() 	{		return layout;				}
	public String getTABWeight() 	{		return tabweight;			}

	public static Rank get(String rank) {
		return ranks.containsKey(rank) ? ranks.get(rank): new Rank(rank);
	}
	
}
