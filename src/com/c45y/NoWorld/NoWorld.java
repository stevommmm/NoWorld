package com.c45y.NoWorld;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;


public class NoWorld extends JavaPlugin implements Listener
{
	public void onEnable()	{
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info(this.getName() + " enabled.");
	}

	public void onDisable()	{
		getLogger().info(this.getName() + " disabled.");
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)	{
		return new NoWorldGen();
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onWorldInit(WorldInitEvent event) {
		// Make sure we always have somewhere to spawn and a basis to build from.
		getSpawnLocation().getBlock().getRelative(BlockFace.DOWN).setType(Material.BEDROCK);
		getLogger().info("Spawn location set.");
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(!event.getPlayer().hasPlayedBefore()) {
			event.getPlayer().teleport(getSpawnLocation());
		}
	}
	
	public Location getSpawnLocation() {
		// Should really be a config option.
		return new Location(getServer().getWorld("world"), 0.5, 64, 0.5);
		
	}

}