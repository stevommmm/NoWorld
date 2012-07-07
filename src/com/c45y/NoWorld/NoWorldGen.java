package com.c45y.NoWorld;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.World;

public class NoWorldGen extends ChunkGenerator
{
	@Override
	public Location getFixedSpawnLocation(World world, Random random) {
		return new Location(world, 0, 64, 0);
	}

	@Override
	public boolean canSpawn(World world, int x, int z) {
		return true;
	}

	public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid)
	{
		byte[][] result = new byte[256 / 16][]; //world height / chunk part height (=16, look above)
		for(int y = 0; y < 16; y++) {
			for(int x = 0; x < 16; x++)
			{
				for(int z = 0; z < 16; z++)
				{
					if (distance(x,y,z) < 8) {
						setBlock(result, x, y, z, (byte) Material.DIRT.getId());
					}
				}
			}
		}
		return result;
	}

	void setBlock(byte[][] result, int x, int y, int z, byte blkid) {
		if (result[y >> 4] == null)
		{
			result[y >> 4] = new byte[4096];
		}
		result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
	}

	@SuppressWarnings("unused") // Yea yea eclipse I know it's not used but handy to keep
	private int coordsToInt(int x, int y, int z) {
		return (x * 64 + z) * 256 + y;
	}

	public double distance(int x, int y, int z)	{
		return Math.sqrt(Math.pow(8 - x, 2.0D) + Math.pow(8 - y, 2.0D) + Math.pow(8 - z, 2.0D));
	}
}