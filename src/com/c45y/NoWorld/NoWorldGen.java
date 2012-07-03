package com.c45y.NoWorld;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.World;

public class NoWorldGen extends ChunkGenerator
{
	@Override
	public Location getFixedSpawnLocation(World world, Random random)
	{
		return new Location(world, 0, 64, 0);
	}
	
	@Override
	public byte[] generate(World world, Random rand, int chx, int chz)
	{
		return new byte[32768];
	}
}