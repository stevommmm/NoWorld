package com.c45y.NoWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class SLAPI
{
	public SLAPI(JavaPlugin j) {
		this.plugin = j;
	}
	
	public void save(HashMap<String, Location> hm)
    {
		List<String> al = new ArrayList<String>();
		for (Entry<String, Location> entry : hm.entrySet()) {
			String player = entry.getKey();
			Location l = entry.getValue();
			al.add(player + "," + l.getX() + "," +  l.getY() + "," +  l.getZ());
		}
		this.plugin.getConfig().set("spawns", al);
		this.plugin.saveConfig();
    }

    public HashMap<String, Location> load()
    {
    	HashMap<String, Location> playerSpawns = new HashMap<String, Location>();
    	List<String> s =  plugin.getConfig().getStringList("locations");
    	for(String loc:s){
    		String [] arg = loc.split(",");
    		String player = arg[0];
    		World world = plugin.getServer().getWorld("world");
    		double x = Double.parseDouble(arg[1]);
    		double y = Double.parseDouble(arg[2]);
    		double z = Double.parseDouble(arg[3]);
    		Location location =  new Location(world,x,y,z);
    		playerSpawns.put(player, location);
    	}
       return playerSpawns;
    }

    private JavaPlugin plugin;
}

