package com.jsn_man.ac;

import java.util.EnumSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandler implements ITickHandler{
	
	public TickHandler(){
		tasks = new ConcurrentLinkedQueue();
	}
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData){
		
	}
	
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData){
		if(type.contains(TickType.CLIENT)){
			Runnable task = tasks.poll();
			while(task != null){
				task.run();
				task = tasks.poll();
			}
		}
	}
	
	@Override
	public EnumSet<TickType> ticks(){
		return EnumSet.of(TickType.CLIENT);
	}
	
	@Override
	public String getLabel(){
		return "ArrowCam TickHandler";
	}
	
	public Queue<Runnable> tasks;
}