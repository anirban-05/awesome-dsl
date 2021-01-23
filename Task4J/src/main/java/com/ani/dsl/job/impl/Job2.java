package com.ani.dsl.job.impl;

import org.apache.log4j.Logger;

import com.ani.dsl.job.IJob;

public class Job2 implements IJob {
	
	final static Logger logger = Logger.getLogger(Job2.class);
	private int id =2;
	//private String jobState;//RUNNING,CANCELLED,COMPLETED
	
	@Override
	public void execute() {
		logger.debug("This is Job2 start");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("InterruptedException in Job2");	
		}
		//s = 5/0;
		throw new IllegalArgumentException("some job exception");
	}
	
	
	public int getId() {
		return id;
	}
	
	

}
