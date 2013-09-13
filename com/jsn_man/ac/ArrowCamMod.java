package com.jsn_man.ac;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = "ArrowCamMod", name = "Arrow Cam Mod", version = "1.1.0")
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
public class ArrowCamMod{
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		//TODO: Load a config if I want to have one
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event){
		EntityRegistry.registerModEntity(EntityCamera.class, "Camera", EntityRegistry.findGlobalUniqueEntityId(), this, 64, 5, false);
		
		if(event.getSide().equals(Side.CLIENT)){
			MinecraftForge.EVENT_BUS.register(new ArrowListener());
			
			ticker = new TickHandler();
			TickRegistry.registerTickHandler(ticker, Side.CLIENT);
			
		}else if(!Minecraft.getMinecraft().isIntegratedServerRunning()){
			FMLLog.severe("The Arrow Cam Mod is a client only mod. Running it on a server will cause undefined behavior! Please remove this mod from your server ASAP.");
		}
	}
	
	/**
	 * TickHandler will run the task at the end of the current tick.
	 * Because of the thread-safe nature of queues, this method is also thread-safe
	 * @param task The task to be processed
	 */
	@SideOnly(Side.CLIENT)
	public void processAtTickEnd(Runnable task){
		ticker.tasks.offer(task);
	}
	
	/**
	 * Called when ArrowListener confirms the local player fires an arrow
	 * @param arrow The arrow fired
	 */
	@SideOnly(Side.CLIENT)
	public void startArrowCam(EntityArrow arrow){		
		if(!isInArrowCam()){
			camera = new EntityCamera(arrow);
			
			if(camera.worldObj.spawnEntityInWorld(camera)){
				Minecraft mc = Minecraft.getMinecraft();
				
				hideGUI = mc.gameSettings.hideGUI;
				fovSetting = mc.gameSettings.fovSetting;
				thirdPersonView = mc.gameSettings.thirdPersonView;
				
				mc.gameSettings.hideGUI = true;
				mc.gameSettings.fovSetting *= 1.1F;
				mc.gameSettings.thirdPersonView = thirdPersonView != 0 ? thirdPersonView : 1;
				mc.renderViewEntity = camera;
			}else{
				camera = null;
			}
		}
	}
	
	/**
	 * Called when the EntityCamera has decided it can no longer follow its target arrow
	 */
	@SideOnly(Side.CLIENT)
	public void stopArrowCam(){
		if(isInArrowCam()){
			Minecraft mc = Minecraft.getMinecraft();
			mc.gameSettings.hideGUI = hideGUI;
			mc.gameSettings.fovSetting = fovSetting;
			mc.gameSettings.thirdPersonView = thirdPersonView;
			mc.renderViewEntity = mc.thePlayer;
			
			camera.setDead();
			camera = null;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public boolean isInArrowCam(){
		return camera != null;
	}
	
	/**
	 * The "inGround" field in EntityArrow is private, so we have to get around that one way or another
	 * @param arrow Is this arrow in the ground?
	 * @return If the arrow is in the ground
	 */
	public static boolean isArrowInGround(EntityArrow arrow){
		try{
			Field inGround = EntityArrow.class.getDeclaredField("inGround");
			inGround.setAccessible(true);
			
			return (Boolean)inGround.get(arrow);
		}catch(Exception e){
			NBTTagCompound tag = new NBTTagCompound();
			arrow.writeEntityToNBT(tag);
			
			return tag.getByte("inGround") == 1;
		}
	}
	
	@Instance("ArrowCamMod")
	public static ArrowCamMod instance;
	
	/** There should only ever be one camera in the game */
	@SideOnly(Side.CLIENT)
	public EntityCamera camera;
	
	/** Basically just processes tasks at the end of each tick. See ArrowListener for why this is necessary */
	@SideOnly(Side.CLIENT)
	public TickHandler ticker;
	
	/** Stores whether or not the GUI is hidden before entering arrow cam */
	private boolean hideGUI;
	
	/** Stores the FOV before entering arrow cam */
	private float fovSetting;
	
	/** Stores the POV before entering arrow cam */
	private int thirdPersonView;
}