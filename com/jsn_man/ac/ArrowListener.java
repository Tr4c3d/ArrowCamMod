package com.jsn_man.ac;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class ArrowListener{
	
	@ForgeSubscribe
	public void onEntityJoinWorld(EntityJoinWorldEvent event) throws Exception{
		if(event.entity instanceof EntityArrow){
			
			//Why can't we just process the arrow here?
			//Well, if the client is playing on an integrated server, we can and it will work fine.
			//However, if the client is connected to any other type of server, the EntityJoinWorldEvent
			//gets called just before the EntityArrow.shootingEntity field is processed.
			//You can thank the NetClientHandler.handleVehicleSpawn(...) method for that.
			//If in the future the shootingEntity field is processed before the event gets fired,
			//the work can be done here without consequence.
			ArrowCamMod.instance.processAtTickEnd(new VerifyArrowTask((EntityArrow)event.entity));
		}
	}
	
	public static class VerifyArrowTask implements Runnable{
		
		public VerifyArrowTask(EntityArrow a){
			arrow = a;
		}
		
		public void run(){
			if(arrow.shootingEntity != null && !arrow.isDead && arrow.shootingEntity.equals(Minecraft.getMinecraft().thePlayer)){
				ArrowCamMod.instance.startArrowCam(arrow);
			}
		}
		
		public EntityArrow arrow;
	};
}