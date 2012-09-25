package com.c45y.NoWorld;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import com.c45y.NoWorld.SLAPI;


public class NoWorld extends JavaPlugin implements Listener
{
	public void onEnable() {
		this.getConfig().options().copyDefaults(true);
		this.getConfig().addDefault("player.total.count", 1);
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info(this.getName() + " enabled.");
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            public void run() {
                slapi.save(playerSpawns);
            }
        }, 1200, 1200); // 1 Minute saves
	}

	public void onDisable()	{
		slapi.save(playerSpawns);
		getLogger().info(this.getName() + " disabled.");
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)	{
		return new NoWorldGen();
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onWorldLoad(WorldLoadEvent event) {
		if(playerSpawns.isEmpty()) {
			playerSpawns = slapi.load();
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onWeatherChange(WeatherChangeEvent event) {
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(!event.getPlayer().hasPlayedBefore()) {
			totalPlayerCount++;
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.getPlayer().hasPermission("World.op")) {
			return;
		}
		if(event.getBlock().getType() == Material.BEDROCK) {
			event.setCancelled(true);
		}
		if( playerSpawns.containsKey(event.getPlayer().getName())) {
			if(distance(playerSpawns.get(event.getPlayer().getName()), event.getBlock().getLocation()) <= 15.5) {
				return;
			}
		}
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getPlayer().hasPermission("World.op")) {
			return;
		}
		if(event.getBlock().getType() == Material.BEDROCK) {
			event.setCancelled(true);
			return;
		}
		if( playerSpawns.containsKey(event.getPlayer().getName())) {
			if(distance(playerSpawns.get(event.getPlayer().getName()), event.getBlock().getLocation()) <= 15.5) {
				return;
			}
		}
		event.setCancelled(true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("get")) {
			if (args.length >= 1){
				Player pSender = (Player) sender;
				String player = pSender.getName();
				Location l = pSender.getLocation();
				Location totp = getCenterSphere(l);
				if(args[0].equals("this")) {
					if (sender instanceof Player) {
						if( args.length == 2 && !args[1].isEmpty() && pSender.hasPermission("World.op")) {
							if (totp == null) {
								pSender.sendMessage(ChatColor.RED + "This is not a sphere :/");
								return true;
							}
							if (playerSpawns.containsValue(totp)) {
								pSender.sendMessage(ChatColor.AQUA + "Sorry somebody already claimed that sphere");
							} else {
								playerSpawns.put(args[1], totp);
								pSender.sendMessage(ChatColor.AQUA + "You have claimed this sphere for " + args[1]);
								pSender.teleport(totp);
							}
							return true;
						}
						if( playerSpawns.containsKey(player)) {
							pSender.sendMessage(ChatColor.RED + "You already own a sphere of land");
						} else {
							if (totp == null) {
								pSender.sendMessage(ChatColor.RED + "This is not a sphere :/");
								return true;
							}
							if (playerSpawns.containsValue(totp)) {
								pSender.sendMessage(ChatColor.AQUA + "Sorry somebody already claimed that sphere");
							} else {
								playerSpawns.put(player, totp);
								pSender.sendMessage(ChatColor.AQUA + "You have claimed this sphere");
								pSender.teleport(totp);
							}
						}
						return true;
					}
				}
				if(args[0].equals("unthis")) {
					if (sender instanceof Player) {
						if (pSender.hasPermission("World.op")) {
							if (totp == null) {
								pSender.sendMessage(ChatColor.RED + "This is not a sphere :/");
								return true;
							}
							if (playerSpawns.containsValue(totp)) {
								for (Entry<String, Location> entry : playerSpawns.entrySet()) {
									if (entry.getValue().equals(totp)) {
										playerSpawns.remove(entry.getKey());
										pSender.sendMessage(ChatColor.AQUA + "Unclaimed this sphere");
										return true;
									}
								}
							}
						}
						return true;
					}
				}
				if(args[0].equals("home")) {
					if (sender instanceof Player) {
						if( args.length == 2 && !args[1].isEmpty() ) {
							player = args[1];
						}
						if( playerSpawns.containsKey(player)) {
							pSender.teleport(playerSpawns.get(player));
						} else {
							pSender.sendMessage(ChatColor.RED + "Sorry can't find that home");
						}
						return true;
					}
				}
//				if(args[0].equals("list")) {
//					for (Entry<String, Location> entry : playerSpawns.entrySet()) {
//						String key = entry.getKey();
//						Object value = entry.getValue();
//						pSender.sendMessage(key + " = " + value);
//					}
//					return true;
//				}
				if(args[0].equals("who")) {
					if (totp == null) {
						pSender.sendMessage(ChatColor.RED + "This is not a sphere :/");
						return true;
					}
					String owner = getOwner(totp);
					if(owner != null) {
						pSender.sendMessage(ChatColor.AQUA + "This sphere owned by: " + owner);
						return true;
					}
					pSender.sendMessage(ChatColor.AQUA + "This sphere is not owned ");
					return true;
				}
			}
		}
		if (cmd.getName().equalsIgnoreCase("vote")) {
			Player pSender = (Player) sender;
			if (args.length < 1){
				pSender.sendMessage(ChatColor.AQUA + "Use vote [1, 2, 3] while in a sphere.");
				return true;
			}
			String player = pSender.getName();
			Location l = pSender.getLocation();
			Location totp = getCenterSphere(l);
			String owner = getOwner(totp);
			if (totp == null) {
				pSender.sendMessage(ChatColor.RED + "This is not a sphere :/");
				return true;
			}
			if(owner == null){
				pSender.sendMessage(ChatColor.RED + "This is claimed by anyone :/");
				return true;
			}
			if(owner.equalsIgnoreCase(player)) {
				pSender.sendMessage(ChatColor.RED + "How about you vote for somebody else :)");
				return true;
			}
			if(args[0].equalsIgnoreCase("1")) {
				getConfig().set("votes." + player + ".one", owner);
				pSender.sendMessage(ChatColor.AQUA + "You voted 1 for the build made by " + owner);
			}
			if(args[0].equals("2")) {
				getConfig().set("votes." + player + ".two", owner);
				pSender.sendMessage(ChatColor.AQUA + "You voted 2 for the build made by " + owner);
			}
			if(args[0].equals("3")) {
				getConfig().set("votes." + player + ".three", owner);
				pSender.sendMessage(ChatColor.AQUA + "You voted 3 for the build made by " + owner);
			}
			saveConfig();
			return true;
		}
		return false;
	}
	
	public String getOwner(Location loaction) {
		for (Entry<String, Location> entry : playerSpawns.entrySet()) {
			if (entry.getValue().equals(loaction)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public Location getCenterSphere(Location location) {
		Chunk c = location.getChunk();
		int valueX = (c.getX()>0) ? c.getX()+1 : c.getX();
		int valueZ = (c.getZ()>0) ? c.getZ()+1 : c.getZ();
		if((valueX/2 % 2 == 0) && (valueZ/2 % 2 == 0)) {
			int x = Math.abs(c.getX() % 2);
			int z = Math.abs(c.getZ() % 2);
			//int distance = (int) distance(c.getX() % 2 * 16,c.getZ() % 2 * 16 ,location.getBlockX() , 17, location.getBlockZ());
			//System.out.println("x: " + x + "; z " + z + "; d: " + distance);
			if (x == 1 && z == 0) { //SW
				Location l = c.getBlock(15, 20, 0).getLocation();
				l.setX(l.getX() + 1);
				return l;
			}
			if (x == 1 && z == 1) { //NW
				Location l = c.getBlock(15, 20, 15).getLocation();
				l.setX(l.getX() + 1);
				l.setZ(l.getZ() + 1);
				return l;
			}
			if (x == 0 && z == 1) { //NE
				Location l = c.getBlock(0, 20, 15).getLocation();
				l.setZ(l.getZ() + 1);
				return l;
			}
			if (x == 0 && z == 0) { //SE -
				return c.getBlock(0, 20, 0).getLocation();
			}
		}
		return null;
	}

	public double distance(Location pl, Location wo) {
		wo.setY(pl.getY());
		return pl.distance(wo);
	}

	private int totalPlayerCount;
	private HashMap<String, Location> playerSpawns = new HashMap<String, Location>();
	private SLAPI slapi = new SLAPI(this);
}