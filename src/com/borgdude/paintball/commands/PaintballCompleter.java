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
	
	private ArenaManager arenaManager = Main.arenaManager;

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(command.getName().equalsIgnoreCase("pb")) {
			if(args.length == 1) {
				List<String> list = new ArrayList<>();
				list.add("join");
				list.add("spectate");
				list.add("leave");
				if(sender.hasPermission("paintball.admin")) {
//					list.addAll(Arrays.asList("create", "set", "reset", "edit"));
					list.add("create");
					list.add("set");
					list.add("reset");
					list.add("edit");
				}
				return list;				
			}
			
			if(args.length == 2) {
				String arg = args[0];
				if(arg.equalsIgnoreCase("join") || arg.equalsIgnoreCase("spectate")) {
					return this.getArenaSorted(args[1]);
				}
				if(sender.hasPermission("paintball.admin")) {
					if(arg.equalsIgnoreCase("edit"))
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
            for(Arena ar : arenaManager.getArena()){
                if(ar.getTitle().startsWith(a))
                    r.add(ar.getTitle());
            }
        } else {
            for(Arena ar : arenaManager.getArena()){
                r.add(ar.getTitle());
            }   
        }
        Collections.sort(r);

        return r;
	}

}
