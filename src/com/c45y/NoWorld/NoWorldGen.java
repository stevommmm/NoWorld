package com.c45y.NoWorld;

import java.util.Random;

import org.bukkit.Location;
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
		System.out.println("X: " + chunkX % 2 * 16 + " Z: " + chunkZ % 2 * 16);
		byte[][] result = new byte[256 / 16][]; //world height / chunk part height (=16, look above)
		int x = 0, y = 0, z = 0;
		this.i = (this.i % 4) + 1;
		for(x = 0; x < 16; x++)
		{
			for(z = 0; z < 16; z++)
			{
				for(y = 0; y < 16; y++) {
					if (distance(chunkX % 2 * 16,chunkZ % 2 * 16 ,x ,y ,z) < 15) {
						setBlock(result, x, y, z, (byte) this.i);
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

	private int coordsToInt(int x, int y, int z) {
		return (x * 64 + z) * 256 + y;
	}

	public double distance(int sx, int sz, int x, int y, int z)	{
		return Math.sqrt(Math.pow(Math.abs(sx) - x, 2.0D) + Math.pow(15 - y, 2.0D) + Math.pow(Math.abs(sz) - z, 2.0D));
	}
	
	private int i = 0;
}