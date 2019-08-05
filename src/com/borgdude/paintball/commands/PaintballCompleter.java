package com.borgdude.paintball.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.managers.ArenaManager;
import com.borgdude.paintball.objects.Arena;

public class PaintballCompleter implements TabCompleter {
	
	private ArenaManager arenaManager;
	
	public PaintballCompleter(Main p) {
		this.arenaManager = p.getArenaManager();
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(command.getName().equalsIgnoreCase("pb")) {
			if(args.length == 1) {
				List<String> list = new ArrayList<>();
				list.add("join");
				list.add("spectate");
				list.add("leave");
				list.add("leaderboard");
				list.add("lb");
				if(sender.hasPermission("paintball.admin")) {
					list.add("create");
					list.add("remove");
					list.add("set");
					list.add("reset");
					list.add("edit");
					list.add("start");
				}
				return getListSorted(args[0], list);				
			}
			
			if(args.length == 2) {
				String arg = args[0];
				if(arg.equalsIgnoreCase("join") || arg.equalsIgnoreCase("spectate")) {
					return this.getArenaSorted(args[1]);
				}
				if(arg.equalsIgnoreCase("leaderboard") || arg.equalsIgnoreCase("lb")) {
					return Arrays.asList("wins", "kills");
				}
				if(sender.hasPermission("paintball.admin")) {
					if(arg.equalsIgnoreCase("edit") || arg.equalsIgnoreCase("start") || arg.equalsIgnoreCase("remove"))
						return this.getArenaSorted(args[1]);
					if(arg.equalsIgnoreCase("set"))
						return Arrays.asList("lobby", "end", "blue", "red", "activate");
					if(arg.equalsIgnoreCase("reset"))
						return Arrays.asList("blue", "red");
					
				}
			}

		}
		return null;
	}
	
	private List<String> getArenaSorted(String a){
		ArrayList<String> r = new ArrayList<>();

        if(!a.equals("")){
            for(Arena ar : arenaManager.getArena().values()){
                if(ar.getTitle().startsWith(a))
                    r.add(ar.getTitle());
            }
        } else {
            for(Arena ar : arenaManager.getArena().values()){
                r.add(ar.getTitle());
            }   
        }
        Collections.sort(r);

        return r;
	}
	
	private List<String> getListSorted(String s, List<String> list){
		ArrayList<String> r = new ArrayList<>();

        if(!s.equals("")){
            for(String str : list){
                if(str.startsWith(s))
                    r.add(str);
            }
        } else {
            for(String str : list){
                r.add(str);
            }   
        }
        Collections.sort(r);

        return r;
	}

}
