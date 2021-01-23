/*
*This class is created only for demo purpose.
*/

package com.ani.dsl.job.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ani.dsl.job.IJob;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
* BatchJob a IJob implementation.
* 
* <P>This class is designed to take a list of IJob instances runs them in parallel.
*  
* <P>The number of threads to run the jobs in parallel is configurable. 
* 
* <P>If one of the jobs throws an exception, all other currently running jobs will be stopped as well
*  and the execute() method  passed the exception to the caller.
*  Remember, this is the responsibility of the input jobs to periodically check 
*  Thread.currentThread().isInterrupted() in their execute method to receive the thread interruption signal
*  
*  
* @author Anirban Sarkar (anirban_ju05@yahoo.co.in)
* @version 0.1
*/

	public class BatchJob implements IJob {
	
		final static Logger logger = LogManager.getLogger(BatchJob.class);
		
		private int id = 0;
		private final static int NO_OF_THREADS;
		private final List<IJob> jobs ;
		private List<Integer> cancelledJobList = new ArrayList<Integer>(); ;
			
		/*
		 * Load configuration  -- could be moved into a different dedicated class 
		 * to initialize the configurable properties
		 */
		static {
			InputStream inputStream;
		    Properties props = new Properties();
			String propFileName = "config.properties";
			inputStream = BatchJob.class.getClassLoader().getResourceAsStream(propFileName);
			if(inputStream == null){
				throw new RuntimeException(propFileName+" File not found");
			}
		    try {
				props.load(inputStream);
			    NO_OF_THREADS = Integer.valueOf(props.getProperty("NO_OF_THREADS"));		    
			} catch(NumberFormatException e){
				throw new NumberFormatException("NO_OF_THREADS is not a number");
			}catch (IOException e) {
				throw new RuntimeException();
			}finally{
				try {
					if(inputStream != null) inputStream.close();
				} catch (IOException e) {
					throw new RuntimeException();
				}
			}
		  }
		
		
		
		private  final ListeningExecutorService executorService = 
				MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(NO_OF_THREADS));
		
		
		 /**
		  * Constructor.
		  * 
		  * @param jobs (required) must be a non-empty IJob list 
		  * @exception IllegalArgumentException if the input is null or empty list
		  */
		public BatchJob(List<IJob> jobs) {
			if(jobs == null || jobs.isEmpty()){
				throw new IllegalArgumentException("Found no job to initialize.");
			}
			this.jobs = new ArrayList<IJob>(jobs);	
		}

		/**
		  *
		  * @exception Exception if any job sends any exception during it's process
		  */
		@Override
		public void execute() throws Exception {
			
			ListenableFuture<List<IJob>> futAsList = null;
			Map<Integer,ListenableFuture<IJob>>  listenableFutureMap = Maps.newHashMap();
			Collection<ListenableFuture<IJob>> listenableFutureList = Collections.emptyList();
			
			try{
				
				listenableFutureMap = runBatchJob();
				listenableFutureList = listenableFutureMap.values();
				futAsList = Futures.allAsList(listenableFutureList);  
				List<IJob> opJobs = futAsList.get();
							
			}catch(ExecutionException | CancellationException  | InterruptedException e){
				if(logger.isDebugEnabled()){
				    logger.debug("exception type="+e.getClass());
				}	
				//futAsList.cancel(true);
				
				for (Integer key : listenableFutureMap.keySet()) {
				//for(ListenableFuture<IJob> lisF : listenableFutureList){
					ListenableFuture<IJob> lisF = listenableFutureMap.get(key);
					lisF.cancel(true);
					if(lisF.isCancelled()){
						cancelledJobList.add(key);
					}
				}
				executorService.shutdownNow();
				throw e;
			
			}finally{
				executorService.shutdown();
				if(logger.isDebugEnabled()){
				    logger.debug("cancelledJobList ="+cancelledJobList);
				}
			}	
		}
		
		/**
		 * @return Map<Integer,ListenableFuture<IJob>>
		 * @throws Exception
		 */
		private Map<Integer,ListenableFuture<IJob>> runBatchJob() throws Exception{  
		     
		    //List<ListenableFuture<IJob>> listenableFutureList = new ArrayList<>();  
			Map<Integer,ListenableFuture<IJob>> listenableFutureMap = new HashMap<Integer,ListenableFuture<IJob>>();  
			
		     for (final IJob job : jobs) {  
		       ListenableFuture<IJob> listenableFuture = executorService.submit(new Callable<IJob>() {  
			         @Override  
			         public IJob call() throws Exception {  
			        	 try{
			        		 job.execute();
			        	 }catch(Exception e){
			        		 throw e;
			        	 }
			        	 return job; 
			         }  
			       });
		       
		       //listenableFutureList.add(listenableFuture);  
		       listenableFutureMap.put(job.getId(), listenableFuture);
		     }  
		     return listenableFutureMap;  
		}

		/** 
		 * @return int NO_OF_THREADS
		 */
		
		public static int getNoOfThreads() {
			return NO_OF_THREADS;
		}  

		/** 
		 * @return int job id
		 */
		@Override
		public int getId() {
			return id;
		}
	
		
		/**  
		 * @return List<Integer> unmodifiable view of underlying Cancelled Job List
		 */
		public List<Integer> getCancelledJobList() {
			return Collections.unmodifiableList(cancelledJobList);
		}

		

}
