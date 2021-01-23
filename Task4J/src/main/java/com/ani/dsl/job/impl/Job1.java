package com.ani.dsl.job.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ani.dsl.job.IJob;

public class Job1 implements IJob{


	private static final Logger logger = LogManager.getLogger(Job1.class);
	private int id =1;
	//private String jobState;//RUNNING,CANCELLED,COMPLETED
	
	@Override
	public void execute() {
		
		logger.debug("This is Job1 start");
		try {
			Thread.sleep(10000);
			logger.debug("This is Job1 end");
		} catch (InterruptedException e) {
			logger.debug("InterruptedException in Job1");
		}
	}
	
	public int getId() {
		return id;
	}
}
