package com.c45y.NoWorld;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.World;

public class NoWorldGen extends ChunkGenerator
{
	@Override
	public Location getFixedSpawnLocation(World world, Random random) {
		return new Location(world, 0, 17, 0);
	}

	@Override
	public boolean canSpawn(World world, int x, int z) {
		return true;
	}

	public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid)
	{
		byte[][] result = new byte[256 / 16][]; //world height / chunk part height (=16, look above)
		byte bld = bldat[random.nextInt(bldat.length)];
		int x = 0, y = 0, z = 0;
		for(x = 0; x < 16; x++)
		{
			for(z = 0; z < 16; z++)
			{
				for(y = 0; y < 16; y++) {
					int distance = (int) distance(chunkX % 2 * 16,chunkZ % 2 * 16 ,x ,y ,z);
					if (distance < 15) {
						setBlock(result, x, y, z, bld);
					} else if (distance == 15) {
						setBlock(result, x, y, z, (byte) 7);
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

	public double distance(int sx, int sz, int x, int y, int z)	{
		return Math.sqrt(Math.pow(Math.abs(sx) - x, 2.0D) + Math.pow(15 - y, 2.0D) + Math.pow(Math.abs(sz) - z, 2.0D));
	}
	
	private byte bldat[] = {1,2,5,24};
}