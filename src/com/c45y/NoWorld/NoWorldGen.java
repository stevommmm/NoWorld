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
        byte[][] result = new byte[256 / 16][]; //world height / chunk part height (=16, look above)
        for(int x = 0; x < 16; x++)
        {
            for(int z = 0; z < 16; z++)
            {
            	if (distance(x,0,z) < 8) {
            		setBlock(result, x, 0, z, (byte) 20);
            	}
            }
        }
        return result;
    }

	void setBlock(byte[][] result, int x, int y, int z, byte blkid) {
	    if (result[y >> 4] == null) //is this chunkpart already initialised?
	    {
	        result[y >> 4] = new byte[4096]; //initialise the chunk part
	    }
	    result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid; //set the block (look above, how this is done)
	}

	private int coordsToInt(int x, int y, int z) {
		return (x * 64 + z) * 256 + y;
	}

	public double distance(int x, int y, int z)	{
		return Math.sqrt(Math.pow(8 - x, 2.0D) + Math.pow(0 - y, 2.0D) + Math.pow(8 - z, 2.0D));
	}
}