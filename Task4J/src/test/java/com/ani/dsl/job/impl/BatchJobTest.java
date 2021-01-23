package com.ani.dsl.job.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ani.dsl.job.IJob;

public class BatchJobTest {
	
	private static final Logger logger = LogManager.getLogger(BatchJobTest.class);
	
	private IJob iJob ;
	private List<IJob> jobs;
	
	@Before
    public void setUp() {
       // create the job list
		jobs = new ArrayList<IJob>();
		jobs.add(new Job1(){public void execute(){}});
		jobs.add(new Job2(){public void execute(){}});
		jobs.add(new Job3(){public void execute(){}});
    }
 
    @After
    public void tearDown() {
    	jobs = null;
    }

	@Test
	  public void testExecuteWithNoException() throws Exception {

		iJob = new BatchJob(jobs);
		assertEquals("No of threads count not matched with 3", 3, BatchJob.getNoOfThreads());
		iJob.execute();
		List<Integer> completedJobList = ((BatchJob)iJob).getCancelledJobList();
		assertEquals("No of cancelled jobs count not matched with 0", 0, completedJobList.size());

	  }
	
	@Test
	  public void testExecuteWithException1() {
		
		jobs = new ArrayList<IJob>();
		jobs.add(new Job1());
		jobs.add(new Job2());
		jobs.add(new Job3());
		
		iJob = new BatchJob(jobs);
		try {
			iJob.execute();
			Assert.fail();
		} catch (Exception e) {
			assertEquals("Exception mismatch", ExecutionException.class, e.getClass());
		}
		
	  }
	
	@Test(expected=ExecutionException.class)
	  public void testExecuteWithException2() throws Exception {
		
		jobs = new ArrayList<IJob>();
		jobs.add(new Job1());
		jobs.add(new Job2());
		jobs.add(new Job3());
		
		iJob = new BatchJob(jobs);
		try{
			iJob.execute();
		}catch(Exception  e){
			if(logger.isDebugEnabled()){
			    logger.debug("Root exception ="+e.getCause().getClass());
			    logger.debug("Root exception message ="+e.getCause().getMessage());
			}
			assertEquals("Root exception mismatch", IllegalArgumentException.class, e.getCause().getClass());
			List<Integer> completedJobList = ((BatchJob)iJob).getCancelledJobList();
			assertEquals("No of cancelled jobs count not matched with 2", 2, completedJobList.size());
			assertEquals("job id not matched", 1, completedJobList.get(0).intValue());
			assertEquals("job id not matched", 3, completedJobList.get(1).intValue());
			throw e;
		}

	  }
	
	@Test
	  public void testExecuteWithNoJob() {
		
		jobs = new ArrayList<IJob>();
		try{
			iJob = new BatchJob(jobs);
			assertEquals("No of threads count not matched with 3", 3, BatchJob.getNoOfThreads());
			Assert.fail();
			iJob.execute();
		}catch(Exception  e){
			assertEquals("Exception mismatch", IllegalArgumentException.class, e.getClass());
		}
	  }
	
	@Test(expected=IllegalArgumentException.class)
	  public void testExecuteWithNull() throws Exception{
		
		iJob = new BatchJob(null);
		assertEquals("No of threads count not matched with 3", 3, BatchJob.getNoOfThreads());
		Assert.fail();
		iJob.execute();
		
	  }

}
