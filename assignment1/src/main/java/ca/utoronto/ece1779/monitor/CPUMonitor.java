package monitor;

import java.util.Date;

import com.amazonaws.auth.*;
import com.amazonaws.auth.profile.*;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;

public class CPUMonitor implements Runnable{

	private static final long ONE_HOUR = 1000 * 60 * 60;
	private static final long ONE_MINUTE = 1000 * 60 * 1;
	
	private static final double DEFAULT_LOWER_THRESHOLD = 20.0;
	private static final double DEFAULT_UPPER_THRESHOLD = 80.0;
	private static final int DEFAULT_INCREASE_RATIO = 2;
	private static final int DEFAULT_DECREASE_RATIO = 2;
	
	private static final int MAX_WORKERS = 20;
	
	private static AmazonCloudWatchClient client;
	
	// Instance variables.
	private WorkerPool workerPool;
	private double lower_threshold;
	private double upper_threshold;
	private int increase_ratio;
	private int decrease_ratio;
	
	/**
	 * Constructor. Initialize instance variables to default values.
	 * 
	 * @param workerPool
	 */
	public CPUMonitor(WorkerPool workerPool){
		this.workerPool = workerPool;
		this.lower_threshold = DEFAULT_LOWER_THRESHOLD;
		this.upper_threshold = DEFAULT_UPPER_THRESHOLD;
		this.increase_ratio = DEFAULT_INCREASE_RATIO;
		this.decrease_ratio = DEFAULT_DECREASE_RATIO;
	}
	
	/**
	 * Monitor CPU in thread and act on pool size accordingly.
	 */
	public void run() {
		
		client = login();
		
		System.out.print("Waiting for first instance to start.");
		while (getCPUUsage("m1.small", ONE_MINUTE) == 0.0) {
			sleep(15000);
			
			System.out.print(".");
		}
		System.out.println("DONE.");
		
        while(true) {
        	
        	// Get CPU usage averaged over all the "m1.small" instances.
        	double minuteAverageCPU = getCPUUsage("m1.small", ONE_MINUTE);
        	
       		// Shrink worker pool if average CPU usage over a minute is
       		// over threshold.
			if (minuteAverageCPU > this.upper_threshold &&
							workerPool.size() < 20) {
				growPool();
				
				// Wait 5 minutes to allow new instances to launch.
				sleep(5*60000);
			}
        	
			double hourAverageCPU = getCPUUsage("m1.small", ONE_HOUR);
			
			// Shrink worker pool if hourly average CPU usage is below threshold.
			if (hourAverageCPU < this.lower_threshold &&
							workerPool.size() > 1) {
				shrinkPool();
			}

			sleep(60000);
	    }
	}
	
	/**
	 * Login to AWS cloud watch with credentials in ~/.aws/credentials
	 * 
	 * @return AmazonCloudWatchClient object, logged in.
	 */
	public static AmazonCloudWatchClient login(){
		ProfileCredentialsProvider legit = new ProfileCredentialsProvider();
		
		AWSCredentials accountID = legit.getCredentials();
		
		String AWSAccessKey = accountID.getAWSAccessKeyId();
		String AWSSecretKey = accountID.getAWSSecretKey();
		
		BasicAWSCredentials credentials = new BasicAWSCredentials(AWSAccessKey, AWSSecretKey);
	
		return new AmazonCloudWatchClient(credentials);
	}
	
	/**
	 * Request make a usage request from CloudWatch for a given instance "instanceId".
	 * 
	 * @param instanceId
	 * @return 
	 */
    private static GetMetricStatisticsRequest requestInstance(String instanceId,
    													long period) {
        // Request cpu usage for a period "period", over a time span of "period".
        // Yields one data point. More are possible (if period<start time).
        return new GetMetricStatisticsRequest()
            .withStartTime(new Date(new Date().getTime()- period))
            .withNamespace("AWS/EC2")
            .withPeriod((int)period)
            .withDimensions(new Dimension().withName("InstanceId").withValue(instanceId))
            .withMetricName("CPUUtilization")
            .withStatistics("Average", "Maximum")
            .withEndTime(new Date());
    }
    
    /**
     * Request aggregated CPU usage statistics for all instances of a given type.
     * 
     * @param instanceType
     * @param period
     * @return
     */
    private static GetMetricStatisticsRequest requestType(String instanceType,
			long period) {
    	// Request cpu usage for a period "period", over a time span of "period".
    	// Yields one data point. More are possible (if period<start time).
    	return new GetMetricStatisticsRequest()
    		.withStartTime(new Date(new Date().getTime()- period))
    		.withNamespace("AWS/EC2")
    		.withPeriod((int)period)
    		.withDimensions(new Dimension().withName("InstanceType").withValue(instanceType))
    		.withMetricName("CPUUtilization")
    		.withStatistics("Average", "Maximum")
    		.withEndTime(new Date());
}
    
    /**
     * Extract usage data from request.
     * 
     * @param client
     * @param request
     * @return
     */
    private static GetMetricStatisticsResult result(final GetMetricStatisticsRequest request) {
         return client.getMetricStatistics(request);
    }
    
    /**
     * Return average CPU usage over "period" for instance "instanceId".
     * 
     * @param instanceId
     * @param period
     * @return
     */
    private static double getCPUUsage(String instanceType, long period){
		GetMetricStatisticsRequest request = requestType(instanceType, period);
		GetMetricStatisticsResult result = result(request);
		
		// In case no instance of monitored type is launched yet.
		if (result.getDatapoints().size() <= 0) return 0.0;
		
		// Makes sense only because there is only one data point.
		return result.getDatapoints().get(0).getAverage();	
    }
    
    /**
     * Set multiple by which instance pool is shrunk if CPU usage under threshold.
     * 
     * @param ratio
     */
    public void setDecreaseRatio(int ratio){
    	if (ratio > 1) this.decrease_ratio = ratio;
    }
    
    /**
     * Set multiple by which instance pool is grown if CPU usage is over threshold.
     * 
     * @param ratio
     */
    public void setIncreaseRatio(int ratio){
    	if (ratio > 1) this.increase_ratio = ratio;
    }
    
    /**
     * Set CPU usage threshold over which pool is grown. To be set, new upper 
     * threshold must be < 100 and > than the lower threshold.
     * 
     * @param threshold
     */
    public void setUpperThreshold(double threshold){
    	if (this.lower_threshold < threshold && threshold < 100.0) 
    		this.upper_threshold = threshold;
    }
    
    /**
     * Set CPU usage threshold under which pool is shrunk. To be set, new lower 
     * threshold must be > 0 and < than the upper threshold.
     * 
     * @param threshold
     */
    public void setLowerThreshold(double threshold){
    	if (0.0 < threshold && threshold < this.upper_threshold) 
    		this.lower_threshold = threshold;
    }
    
    /**
     * Decrease pool size by [decrease_ratio]. Floor at minimum size.
     */
    private void shrinkPool()
    {
		int toTerminate = workerPool.size()*(this.decrease_ratio-1);
		
		// Make sure number of workers never goes below the minimum.
		if (toTerminate > workerPool.size()){
			toTerminate = workerPool.size() - 1;
		};
		
		System.out.println("Average hourly CPU usage under threshold (" + 
				this.lower_threshold + "). Terminating " + 
				toTerminate + " instance(s).");
		
		workerPool.terminateInstances(toTerminate);
		
    }
    
    /**
     * Increase pool size by [increase_ratio]. Ceiling at maximum size.
     */
    private void growPool(){
    	
		int toLaunch = workerPool.size()*(this.increase_ratio-1);
		
		// Do not launch more than the maximum number of workers.
		if (toLaunch + workerPool.size() > MAX_WORKERS) 
			toLaunch = MAX_WORKERS - workerPool.size();
		
		System.out.println("CPU usage over threshold (" + this.upper_threshold
				+ "). Launching " + toLaunch + " new instance(s).");
		
		workerPool.launchInstances(toLaunch);
    }
    
    /**
     * Self-explanatory...
     * 
     * @param time
     */
    public static void sleep(int time){
	    try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}
