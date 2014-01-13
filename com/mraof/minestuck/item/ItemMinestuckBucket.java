package com.mraof.minestuck.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.mraof.minestuck.Minestuck;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMinestuckBucket extends ItemBucket 
{
	public List<Integer> fillFluids = new ArrayList<Integer>();
	public HashMap<Integer, String> textureFiles = new HashMap<Integer, String>();
	HashMap<Integer, Icon> textures = new HashMap<Integer, Icon>();
	public ItemMinestuckBucket(int par1) 
	{
		super(par1, 0);
		setUnlocalizedName("minestuckBucket");
		setCreativeTab(Minestuck.tabMinestuck);
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
	 */
	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(par2World, par3EntityPlayer, false);

		if (movingobjectposition == null)
		{
			return par1ItemStack;
		}
		else
		{
			FillBucketEvent event = new FillBucketEvent(par3EntityPlayer, par1ItemStack, par2World, movingobjectposition);
			if (MinecraftForge.EVENT_BUS.post(event))
			{
				return par1ItemStack;
			}

			if (event.getResult() == Event.Result.ALLOW)
			{
				if (par3EntityPlayer.capabilities.isCreativeMode)
				{
					return par1ItemStack;
				}

				if (--par1ItemStack.stackSize <= 0)
				{
					return event.result;
				}

				if (!par3EntityPlayer.inventory.addItemStackToInventory(event.result))
				{
					par3EntityPlayer.dropPlayerItem(event.result);
				}

				return par1ItemStack;
			}

			if (movingobjectposition.typeOfHit == EnumMovingObjectType.TILE)
			{
				int x = movingobjectposition.blockX;
				int y = movingobjectposition.blockY;
				int z = movingobjectposition.blockZ;

				if (!par2World.canMineBlock(par3EntityPlayer, x, y, z))
				{
					return par1ItemStack;
				}

				if (par1ItemStack.getItemDamage() < 0)
				{
					return new ItemStack(Item.bucketEmpty);
				}
				
				switch(movingobjectposition.sideHit)
				{
				case 0:
					--y; 
					break;
				case 1:
					++y; 
					break;
				case 2:
					--z; 
					break;
				case 3:
					++z; 
					break;
				case 4:
					--x; 
					break;
				case 5:
					++x; 
					break;
				}


				if (!par3EntityPlayer.canPlayerEdit(x, y, z, movingobjectposition.sideHit, par1ItemStack))
				{
					return par1ItemStack;
				}

				if (this.tryPlaceContainedLiquid(par2World, x, y, z, par1ItemStack.getItemDamage()) && !par3EntityPlayer.capabilities.isCreativeMode)
				{
					return new ItemStack(Item.bucketEmpty);
				}
			}

			return par1ItemStack;
		}
	}

	/**
	 * Attempts to place the liquid contained inside the bucket.
	 */
	public boolean tryPlaceContainedLiquid(World par1World, int par2, int par3, int par4, int id)
	{
		Material material = par1World.getBlockMaterial(par2, par3, par4);
		boolean flag = !material.isSolid();

		if (!par1World.isAirBlock(par2, par3, par4) && !flag)
		{
			return false;
		}
		else
		{
			if (!par1World.isRemote && flag && !material.isLiquid())
			{
				par1World.destroyBlock(par2, par3, par4, true);
			}

			par1World.setBlock(par2, par3, par4, id, 0, 3);

			return true;
		}
	}
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(int unknown, CreativeTabs tab, List subItems) 
	{
		for(Integer id : fillFluids)
			subItems.add(new ItemStack(this, 1, id));
	}
	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) 
	{
		return getUnlocalizedName() + "." + Block.blocksList[par1ItemStack.getItemDamage()].getUnlocalizedName();
	}
	@Override
	public Icon getIconFromDamage(int damage) 
	{
		return this.textures.get(damage);
	}
	@Override
	public void registerIcons(IconRegister par1IconRegister)
    {
        for (Entry<Integer, String> entry : textureFiles.entrySet())
            this.textures.put(entry.getKey(), par1IconRegister.registerIcon("minestuck:" + entry.getValue()));
    }


}
