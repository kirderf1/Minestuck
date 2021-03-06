package com.mraof.minestuck.world.gen.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

import com.mraof.minestuck.Minestuck;

public class ComponentCastleStartPiece extends ComponentCastlePiece 
{

    /** List of other Castle components linked to this room. */

    protected boolean isBlack, bottom;
    public int averageGroundLevel = -1;
    public int castleWidth, castleLength, x, z, totalPieces;
    public ArrayList<ComponentCastlePiece> pendingPieces = new ArrayList<ComponentCastlePiece>();
    
    public ComponentCastleStartPiece() {}
	protected ComponentCastleStartPiece(int par1, int x, int z,  boolean isBlack) 
	{
		super(par1, (ComponentCastleStartPiece)null);
        this.boundingBox = new StructureBoundingBox(x, 0, z, x, 74, z);
        this.x = x;
        this.z = z;
		if(pendingPieces == null)pendingPieces = new ArrayList<ComponentCastlePiece>();
 		this.isBlack = isBlack;
 		this.bottom = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void buildComponent(StructureComponent structureComponent,
			@SuppressWarnings("rawtypes") List components, Random random)
	{
		this.castleWidth = (random.nextInt(12) + 4) * 16;
		this.castleLength = (random.nextInt(24) + 8) * 16;
		this.componentType = 1;
		this.getNextComponentNormal(this, components, random, 8, 0, true);
		this.componentType = 2;
		for(int depth = 8; depth < this.castleLength; depth += 8)
			this.getNextComponentNormal(this, components, random,  0, depth, true);
		this.componentType = 0;
		this.getNextComponentNormal(this, components, random, 0, -8, 0, true);
		
	}
    public boolean addComponentParts(World world, Random random, StructureBoundingBox structureBoundingBox)
    {
    	int chessTileMetadata = this.isBlack ? 0 : 1;
    	int chessTileMetadata1 = this.isBlack ? 2 : 3;
//    	Debug.print("addComponentParts in ComponentCastleStartPiece running");
        if (this.averageGroundLevel < 0)
        {
            this.averageGroundLevel = this.getAverageGroundLevel(world, structureBoundingBox);

            if (this.averageGroundLevel < 0)
            {
                return false;
            }

        }
        this.boundingBox.offset(0, this.averageGroundLevel - 1, 0);
        if (this.isLiquidInStructureBoundingBox(world, structureBoundingBox))
        {
            return false;
        }
        else
        {
//        	Debug.print("CCSP: " + this.averageGroundLevel);
//            Debug.print(structureBoundingBox.minX + ", " + structureBoundingBox.minY + ", " + structureBoundingBox.minZ + ", " + structureBoundingBox.maxX + ", " + structureBoundingBox.maxY + ", " + structureBoundingBox.maxZ);
            this.fillWithAlternatingBlocks(world, structureBoundingBox, 0, 0, 0, 7 ,6, 7, Minestuck.chessTile, chessTileMetadata,  Minestuck.chessTile, chessTileMetadata1, false);
            this.fillWithAlternatingBlocks(world, structureBoundingBox, 0, 0, 0, 7 ,7, 0, Minestuck.chessTile, chessTileMetadata, Minestuck.chessTile, chessTileMetadata1, false);
            this.fillWithAlternatingBlocks(world, structureBoundingBox, 0, 0, 7, 7 ,7, 7, Minestuck.chessTile, chessTileMetadata, Minestuck.chessTile, chessTileMetadata1, false);
            this.fillWithBlocks(world, structureBoundingBox, 2, 1, 0, 5, 5, 7, Blocks.air, Blocks.air, false);

            return true;
        }
    }

}
