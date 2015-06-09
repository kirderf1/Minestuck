package com.mraof.minestuck.item;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import com.mraof.minestuck.Minestuck;
import com.mraof.minestuck.MinestuckConfig;

import static com.mraof.minestuck.MinestuckConfig.artifactRange;

import com.mraof.minestuck.tracker.MinestuckPlayerTracker;
import com.mraof.minestuck.util.Debug;
import com.mraof.minestuck.util.ITeleporter;
import com.mraof.minestuck.util.MinestuckAchievementHandler;
import com.mraof.minestuck.util.Teleport;
import com.mraof.minestuck.world.lands.LandAspectRegistry;

public class ItemCruxiteArtifact extends ItemFood implements ITeleporter
{
	
	public ItemCruxiteArtifact(int par2, boolean par3) 
	{
		super(1, par2, par3);
		this.setCreativeTab(Minestuck.tabMinestuck);
		setUnlocalizedName("cruxiteArtifact");
		this.maxStackSize = 1;
	}
	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));

		return par1ItemStack;
	}
	@Override
	protected void onFoodEaten(ItemStack par1ItemStack, World par2World, EntityPlayer player) {
		if(!par2World.isRemote && player.worldObj.provider.getDimensionId() != -1) {
			
			int destinationId = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getInteger("LandId") == 0 ? LandAspectRegistry.createLand(player) : player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getInteger("LandId");
			if(player.worldObj.provider.getDimensionId() != destinationId) {
				player.triggerAchievement(MinestuckAchievementHandler.enterMedium);
				Teleport.teleportEntity(player, destinationId, this);
				MinestuckPlayerTracker.sendLandEntryMessage(player);
			}
		}
	}
	public void makeDestination(Entity entity, WorldServer worldserver0, WorldServer worldserver1)
	{
		if(entity instanceof EntityPlayerMP && entity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getInteger("LandId") == worldserver1.provider.getDimensionId())
		{
			int x = (int) entity.posX;
			int y = (int) entity.posY;
			int z = (int) entity.posZ;
			
			Debug.print("Loading spawn chunks...");
			for(int chunkX = ((x - artifactRange) >> 4) - 1; chunkX <= ((x + artifactRange) >> 4) + 2; chunkX++)	//Prevent anything to generate on the piece that we move
				for(int chunkZ = ((z - artifactRange) >> 4) - 1; chunkZ <= ((z + artifactRange) >> 4) + 2; chunkZ++)	//from the overworld.
					worldserver1.theChunkProviderServer.loadChunk(chunkX, chunkZ);
			
			Debug.print("Teleporting entities...");
			List<?> list = entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity, entity.getEntityBoundingBox().expand((double)artifactRange, artifactRange, (double)artifactRange));
			Iterator<?> iterator = list.iterator();
			
			while (iterator.hasNext())
			{
				Entity e = (Entity)iterator.next();
				if(MinestuckConfig.entryCrater || e instanceof EntityPlayer || e instanceof EntityItem)
					Teleport.teleportEntity(e, worldserver1.provider.getDimensionId(), this);
				else	//Copy instead of teleport
				{
					Entity newEntity = EntityList.createEntityByName(EntityList.getEntityString(entity), worldserver1);
					if (newEntity != null)
					{
						newEntity.copyDataFromOld(entity);
						newEntity.dimension = worldserver1.provider.getDimensionId();
						worldserver1.spawnEntityInWorld(newEntity);
					}
				}
			}
			
			Debug.print("Placing blocks...");
			for(int blockX = x - artifactRange; blockX <= x + artifactRange; blockX++)
			{
				int zWidth = (int) Math.sqrt(artifactRange * artifactRange - (blockX - x) * (blockX - x));
				for(int blockZ = z - zWidth; blockZ <= z + zWidth; blockZ++)
				{
					double radius = Math.sqrt(((blockX - x) * (blockX - x) + (blockZ - z) * (blockZ - z)) / 2);
					int minY =  y - (int) (Math.sqrt(artifactRange * artifactRange - radius * radius));
					minY = minY < 0 ? 0 : minY;
					for(int blockY = minY; blockY < 256; blockY++)
					{
						BlockPos pos = new BlockPos(blockX, blockY, blockZ);
						IBlockState block = worldserver0.getBlockState(pos);
						TileEntity te = worldserver0.getTileEntity(pos);
						if(block != Blocks.bedrock)
							worldserver1.setBlockState(pos, block, 0);
						if((te) != null)
						{
							TileEntity te1 = null;
							try {
								te1 = te.getClass().newInstance();
							} catch (Exception e) {e.printStackTrace();	continue;}
							NBTTagCompound nbt = new NBTTagCompound();
							te.writeToNBT(nbt);
							te1.readFromNBT(nbt);
							worldserver1.removeTileEntity(pos);
							worldserver1.setTileEntity(pos, te1);
						};
					}
				}
			}
			
			Debug.print("Removing old blocks...");
			for(int blockX = x - artifactRange; blockX <= x + artifactRange; blockX++)
			{
				int zWidth = (int) Math.sqrt(artifactRange * artifactRange - (blockX - x) * (blockX - x));
				for(int blockZ = z - zWidth; blockZ <= z + zWidth; blockZ++)
				{
					double radius = Math.sqrt(((blockX - x) * (blockX - x) + (blockZ - z) * (blockZ - z)) / 2);
					int minY =  y - (int) (Math.sqrt(artifactRange * artifactRange - radius*radius));
					minY = minY < 0 ? 0 : minY;
					for(int blockY = minY; blockY < 256; blockY++)
					{
						BlockPos pos = new BlockPos(blockX, blockY, blockZ);
						if(MinestuckConfig.entryCrater)
						{
							if(worldserver0.getBlockState(pos).getBlock() != Blocks.bedrock)
								worldserver0.setBlockState(pos, Blocks.air.getDefaultState(), 2);
						} else
							if(worldserver0.getTileEntity(pos) != null)
								worldserver0.setBlockState(pos, Blocks.air.getDefaultState(), 2);
					}
				}
			}
			
			Debug.print("Removing old entities...");
			list = entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity, entity.getEntityBoundingBox().expand((double)artifactRange, artifactRange, (double)artifactRange));
			iterator = list.iterator();
			while (iterator.hasNext())
				if(MinestuckConfig.entryCrater)
					((Entity)iterator.next()).setDead();
				else
				{
					Entity e = (Entity) iterator.next();
					if(e instanceof EntityItem)
						e.setDead();
				}
		}
	}
	
}
