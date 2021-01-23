package com.ani.dsl.job.impl;

import org.apache.log4j.Logger;

import com.ani.dsl.job.IJob;

public class Job3 implements IJob {

	final static Logger logger = Logger.getLogger(Job3.class);
	private int id =3;
	//private String jobState;//RUNNING,CANCELLED,COMPLETED
	
	@Override
	public void execute() throws Exception{
		String s="";
		logger.debug("This is Job3 start");
		
		for(int i=0;i<100000;i++){
			s = s+i;
			if (Thread.currentThread().isInterrupted()) {
				//throw new InterruptedException();
				return;
            }
		}
		logger.debug("This is Job3 end");
	}
	
	
	public int getId() {
		return id;
	}
	
	

}
