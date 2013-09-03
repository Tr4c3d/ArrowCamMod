package com.jsn_man.ac;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class EntityCamera extends EntityLivingBase{
	
	public EntityCamera(World world){
		super(world);
		delay = 10;
		lastLastTargetPos = new double[3];
		lastLastTargetRotation = new float[2];
		setSize(0.0F, 0.0F);
		boundingBox.setBounds(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
	}
	
	public EntityCamera(EntityArrow arrow){
		this(arrow.worldObj);
		setTarget(arrow);
		lastLastTargetPos[0] = target.posX;
		lastLastTargetPos[1] = target.posY;
		lastLastTargetPos[2] = target.posZ;
		lastLastTargetRotation[0] = 360.0F - target.rotationYaw;
		lastLastTargetRotation[1] = 360.0F - target.rotationPitch;
		setPositionAndRotation(target.posX, target.posY, target.posZ, 360.0F - target.rotationYaw, 360.0F - target.rotationPitch);
	}
	
	@Override
	public void onLivingUpdate(){
		super.onLivingUpdate();
		
		if(target != null){
			//Is the arrow alive and is it still in the air?
			if(target.isEntityAlive() && !ArrowCamMod.isArrowInGround(target)){
				
				//IMPORTANT: Only subtract 1.0 from the y if you are not using trig
				setPosition(lastLastTargetPos[0], lastLastTargetPos[1] - 1.0, lastLastTargetPos[2]);
				
				//Storing the last tick position so the camera is where the arrow was 2 ticks ago
				lastLastTargetPos[0] = target.lastTickPosX;
				lastLastTargetPos[1] = target.lastTickPosY;
				lastLastTargetPos[2] = target.lastTickPosZ;
				
				//Method to find the viewing angle without using trig
				setRotation(lastLastTargetRotation[0], lastLastTargetRotation[1]);
				
				//Same as lastLastTargetPos except for this one stores rotation
				//It works better if you subtract the angles from 360
				lastLastTargetRotation[0] = 360.0F - target.prevRotationYaw;
				lastLastTargetRotation[1] = 360.0F - target.prevRotationPitch;
				
				/*
				 * Method to find the viewing angle using trig
				 * Not really worth the processing power
				 * 
				 * When using this method, the rotationYaw isn't correct if you fire the arrow in the positive x direction
				 * 
				//Should we use x or z for the x coord on our atan2?
				//Because atan2 is a 2D function, we have to simplify 3D
				boolean useX = Math.abs(target.posX - posX) >= Math.abs(target.posZ - posZ);
				
				//By using trig, we can use the relative coords of the arrow to find the best viewing angle
				//Or at least that's the idea
				setRotation(
						90.0F + (float)-ArrowCamMod.properAtan2(target.posX - posX, target.posZ - posZ),
						(float)-ArrowCamMod.properAtan2(useX ? target.posX - posX : target.posZ - posZ, target.posY - posY)
				);
				*/
				
			}else if(--delay <= 0){
				ArrowCamMod.instance.stopArrowCam();
			}
		}
		
		//Counteract gravity
		if(!onGround && motionY < 0.0){
			motionY = 0.0;
		}
		
		//The camera will get stuck if it is in an unloaded chunk
		Chunk chunk = worldObj.getChunkFromBlockCoords((int)posX, (int)posY);
		if(!chunk.isChunkLoaded){
			worldObj.getChunkProvider().loadChunk(chunk.xPosition, chunk.zPosition);
		}
	}
	
	public void setTarget(EntityArrow arrow){
		target = arrow;
	}
	
	public EntityArrow getTarget(){
		return target;
	}
	
	//We don't want the camera to be saved/loaded from games
	@Override
	public void writeToNBT(NBTTagCompound tag){
		
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag){
		
	}
	
	//For some reason the regular method throws an NPE, so I rerouted it to the player
	@Override
	public boolean isPotionActive(Potion potion){
		return Minecraft.getMinecraft().thePlayer.isPotionActive(potion);
	}
	
	//Don't want fall damage
	@Override
	protected void fall(float blocks){
		
	}
	
	@Override
	public boolean isEntityInvulnerable(){
		return true;
	}
	
	@Override
	protected boolean canTriggerWalking(){
		return false;
	}
	
	@Override
	public boolean canBeCollidedWith(){
		return false;
	}
	
	@Override
	public boolean canBePushed(){
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean canRenderOnFire(){
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public float getShadowSize(){
		return 0.0F;
	}
	
	@Override
	public ItemStack getHeldItem(){
		return null;
	}
	
	@Override
	public ItemStack getCurrentItemOrArmor(int i){
		return null;
	}
	
	@Override
	public void setCurrentItemOrArmor(int i, ItemStack itemstack){
		
	}
	
	@Override
	public ItemStack[] getLastActiveItems(){
		return new ItemStack[]{null, null, null, null, null};
	}
	
	/** How many ticks the camera will stay around for after the arrow is dead or in a block */
	public int delay;
	
	/**
	 * The position of the target 2 ticks ago
	 * [x, y, z]
	 */
	public double[] lastLastTargetPos;
	
	/**
	 * The rotation of the target 2 ticks ago
	 * [yaw, pitch]
	 */
	public float[] lastLastTargetRotation;
	
	/** The arrow that the camera is following */
	public EntityArrow target;
}