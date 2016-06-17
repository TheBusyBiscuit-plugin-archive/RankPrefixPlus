package me.mrCookieSlime.RankPrefixPlus;

import java.util.HashMap;
import java.util.Map;

public class Rank {
	
	public static Map<String, Rank> ranks = new HashMap<String, Rank>();
	private static char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
	
	String prefix;
	String suffix;
	boolean scoreboard, bypassOP;
	String scoreboard_prefix;
	String scoreboard_suffix;
	String name, layout, color;
	String tabweight;
	
	public Rank(String name) {
		this.name = name;
		this.prefix = main.cfg.getString("ranks." + name + ".prefix");
		this.suffix = main.cfg.getString("ranks." + name + ".suffix");
		this.scoreboard = main.cfg.getBoolean("ranks." + name + ".scoreboard.enabled");
		this.scoreboard_prefix = main.cfg.getString("ranks." + name + ".scoreboard.prefix");
		this.scoreboard_suffix = main.cfg.getString("ranks." + name + ".scoreboard.suffix");
		this.color = main.cfg.getString("ranks." + name + ".message-color");
		this.bypassOP = main.cfg.getBoolean("ranks." + name + ".bypass-OP");
		this.layout = main.cfg.getString("ranks." + name + ".chat-layout");
		int index = chars.length - main.cfg.getInt("ranks." + name + ".scoreboard.tab-priority");
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
