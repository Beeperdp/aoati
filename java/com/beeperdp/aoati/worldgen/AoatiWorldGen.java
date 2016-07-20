package com.beeperdp.aoati.worldgen;

import java.util.Random;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;

import com.beeperdp.aoati.aoati;

public class AoatiWorldGen implements IWorldGenerator {
	
	private void runGenerator(WorldGenerator generator, World world, Random rand, int chunk_X, int chunk_Z, int chancesToSpawn, int minHeight, int maxHeight) {
	    if (minHeight < 0 || maxHeight > 256 || minHeight > maxHeight)
	        throw new IllegalArgumentException("Illegal Height Arguments for WorldGenerator");

	    int heightDiff = maxHeight - minHeight + 1;
	    for (int i = 0; i < chancesToSpawn; i ++) {
	        int x = chunk_X * 16 + rand.nextInt(16);
	        int y = minHeight + rand.nextInt(heightDiff);
	        int z = chunk_Z * 16 + rand.nextInt(16);
	        generator.generate(world, rand, x, y, z);
	    }
	}
	
	private WorldGenerator gen_adamantium_ore; //Generates Adamantium Ore (Used in Overworld)

	public AoatiWorldGen() {
	    this.gen_adamantium_ore = new WorldGenMinable(aoati.blockAdamantiumOre, 12);
	}
	
	
	
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider){   
        switch (world.provider.dimensionId) {
        case 0: //Overworld
        	this.runGenerator(this.gen_adamantium_ore, world, random, chunkX, chunkZ, 2, 0, 128);//Adamantium Ore
            break;
        case -1: //Nether

            break;
        case 1: //End

            break;
        }
    }
}
