package com.beeperdp.aoati.tile_entity;

import com.beeperdp.aoati.aoati;
import com.beeperdp.aoati.block.BlockCompressor;
import com.beeperdp.aoati.handler.BlockCompressorRecipes;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class TileEntityBlockCompressor extends TileEntity implements ISidedInventory{
	
	public final int TotalCookTime = 75;
	
	private static final int[] slotsTop = new int[]{0};
	private static final int[] slotsBottom = new int[]{2,1};
	private static final int[] slotsSides = new int[]{1};
	
	private ItemStack[] furnaceItemStacks = new ItemStack[3];
	
	public int furnaceBurnTime;
	public int currentBurnTime;
	
	public int furnaceCookTime;
	
	private String furnaceName;
	
	public void furnaceName(String string){
		this.furnaceName = string;
	}
	
	@Override
	public int getSizeInventory() {
		return this.furnaceItemStacks.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return this.furnaceItemStacks[slot];
	}

	@Override
	public ItemStack decrStackSize(int p1, int p2) {
		if(this.furnaceItemStacks[p1] != null){
			ItemStack itemstack;
			if(this.furnaceItemStacks[p1].stackSize <= p2){
				itemstack = this.furnaceItemStacks[p1];
				this.furnaceItemStacks[p1] = null;
				return itemstack;
			}else{
				itemstack = this.furnaceItemStacks[1].splitStack(p2);
				
				if(this.furnaceItemStacks[p1].stackSize == 0){
					this.furnaceItemStacks[p1] = null;
				}
				
				return itemstack;
			}
		}else{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int p1) {
		if(this.furnaceItemStacks[p1] != null){
			ItemStack itemstack = this.furnaceItemStacks[p1];
			this.furnaceItemStacks = null;
			return itemstack;
		}else{
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack itemstack) {
		this.furnaceItemStacks[var1] = itemstack;
		
		if(itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()){
			itemstack.stackSize = this.getInventoryStackLimit();
		}
	}

	@Override
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.furnaceName : "Compressor";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return this.furnaceName != null && this.furnaceName.length() > 0;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}
	
	public void readFromNBT(NBTTagCompound tagCompound){
		super.readFromNBT(tagCompound);
		NBTTagList tagList = tagCompound.getTagList("Items", 10);
		this.furnaceItemStacks = new ItemStack[this.getSizeInventory()];
		
		for(int i=0;i<tagList.tagCount();++i){
			NBTTagCompound tagCompound1 = tagList.getCompoundTagAt(i);
			byte byte0 = tagCompound1.getByte("Slot");
			
			if(byte0 >= 0 && byte0 < this.furnaceItemStacks.length){
				this.furnaceItemStacks[byte0] = ItemStack.loadItemStackFromNBT(tagCompound1);
			}
		}
		
		this.furnaceBurnTime = tagCompound.getShort("BurnTime");
		this.furnaceCookTime = tagCompound.getShort("CookTime");
		this.currentBurnTime = getItemBurnTime(this.furnaceItemStacks[1]);
		
		if(tagCompound.hasKey("CustomName", 8)){
			this.furnaceName = tagCompound.getString("CustomName");
		}
	}
	
	public void writeToNBT(NBTTagCompound tagCompound){
		super.writeToNBT(tagCompound);
		
		tagCompound.setShort("BurnTime", (short) this.furnaceBurnTime);
		tagCompound.setShort("CookTime", (short) this.furnaceCookTime);
		NBTTagList tagList = new NBTTagList();
		
		for(int i=0;i < this.furnaceItemStacks.length;++i){
			if(this.furnaceItemStacks[i] != null){
				NBTTagCompound tagCompound1 = new NBTTagCompound();
				tagCompound1.setByte("Slot", (byte) i);
				this.furnaceItemStacks[i].writeToNBT(tagCompound1);
				tagList.appendTag(tagCompound1);
			}
		}
		
		tagCompound.setTag("Items", tagList);
		
		if(this.hasCustomInventoryName()){
			tagCompound.setString("CustomName", this.furnaceName);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public int getCookProgressScaled(int par1){
		return this.furnaceCookTime * par1 / TotalCookTime;
	}
	
	@SideOnly(Side.CLIENT)
	public int getBurnTimeRemainingScaled(int par1){
		if(this.currentBurnTime == 0){
			this.currentBurnTime = TotalCookTime;
		}
		
		return this.furnaceBurnTime * par1 / this.currentBurnTime;
	}
	
	public boolean isBurning(){
		return this.furnaceBurnTime > 0;
	}
	
	public void updateEntity(){
		boolean flag = this.furnaceBurnTime > 0;
		boolean flag1 = false;
		
		if(this.furnaceBurnTime > 0){
			--this.furnaceBurnTime;
		}
		
		if(!this.worldObj.isRemote){
			if(this.furnaceBurnTime == 0 && this.canSmelt()){
				this.currentBurnTime = this.furnaceBurnTime = getItemBurnTime(this.furnaceItemStacks[1]);
				
				if(this.furnaceBurnTime > 0){
					flag1 = true;
					if(this.furnaceItemStacks[1] != null){
						--this.furnaceItemStacks[1].stackSize;
						
						if(this.furnaceItemStacks[1].stackSize == 0){
							this.furnaceItemStacks[1] = furnaceItemStacks[1].getItem().getContainerItem(this.furnaceItemStacks[1]);
						}
					}
				}
			}
			
			if(this.isBurning() && this.canSmelt()){
				++this.furnaceCookTime;
				if(this.furnaceCookTime == TotalCookTime-5){
					this.furnaceCookTime = 0;
					this.smeltItem();
					flag1 = true;
				}
			}else{
				this.furnaceCookTime = 0;
			}
		}
		
		if(flag != this.furnaceBurnTime > 0){
			flag1 = true;
			BlockCompressor.updateBlockState(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		}
		
		if(flag1){
			this.markDirty();
		}
	}
	
	private boolean canSmelt(){
		if(this.furnaceItemStacks[0] == null){
			return false;
		}else{
			ItemStack itemstack = BlockCompressorRecipes.smelting().getSmeltingResult(this.furnaceItemStacks[0]);
			if(itemstack == null) return false;
			if(this.furnaceItemStacks[2] == null) return true;
			if(!this.furnaceItemStacks[2].isItemEqual(itemstack)) return false;
			int result = furnaceItemStacks[2].stackSize + itemstack.stackSize;
			boolean resultr = result <= getInventoryStackLimit() && result <= this.furnaceItemStacks[2].getMaxStackSize();
			return resultr;
		}
	}
	
	public void smeltItem() {
		if (this.canSmelt()) {
			ItemStack itemstack = BlockCompressorRecipes.smelting().getSmeltingResult(this.furnaceItemStacks[0]);

			if (this.furnaceItemStacks[2] == null) {
				this.furnaceItemStacks[2] = itemstack.copy();
			} else if (this.furnaceItemStacks[2].getItem() == itemstack.getItem()) {
				this.furnaceItemStacks[2].stackSize += itemstack.stackSize;
			}
			
			--this.furnaceItemStacks[0].stackSize;
			
			if(this.furnaceItemStacks[0].stackSize <= 0){
				this.furnaceItemStacks[0] = null;
			}
		}
	}
	
	public static int getItemBurnTime(ItemStack itemstack){
		if(itemstack == null){
			return 0;
		}else{
			Item item = itemstack.getItem();
			
			if(item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.air){
				Block block = Block.getBlockFromItem(item);
				
				//if(block == aoati.blockVirus){
				//	return 200;
				//}
				
				//if(block.getMaterial() == Material.rock){
				//	return 300;
				//}
				
			}
			if(item == aoati.itemCharredCoal){return 90;}
			//if(item instanceof ItemTool && ((ItemTool) item).getToolMaterialName().equals("EMERALD")) return 300;
			return GameRegistry.getFuelValue(itemstack);
		}
	}
	
	public static boolean isItemFuel(ItemStack itemstack){
		return getItemBurnTime(itemstack) < 0;
	}
	
	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : player.getDistanceSq((double) this.xCoord + 0.5D, (double) this.yCoord + 0.5D, (double) this.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeInventory() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isItemValidForSlot(int par1, ItemStack itemstack) {
		return par1 == 2 ? false : (par1 == 1 ? isItemFuel(itemstack) : true);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int par1){
		return par1 == 0 ? slotsBottom : (par1 == 1 ? slotsTop : slotsSides);
	}

	@Override
	public boolean canInsertItem(int par1, ItemStack itemstack, int par3) {
		return this.isItemValidForSlot(par1, itemstack);
	}

	@Override
	public boolean canExtractItem(int par1, ItemStack itemstack, int par3) {
		return par3 != 0 || par1 != 1 || itemstack.getItem() == Items.bucket;
	}
	
}
