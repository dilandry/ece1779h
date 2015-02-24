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
	private static final double DEFAULT_LOWER_THRESHOLD = 20.0;
	private static final double DEFAULT_UPPER_THRESHOLD = 80.0;
	
	private static AmazonCloudWatchClient client;
	
	private WorkerPool workerPool;
	private double lower_threshold;
	private double upper_threshold;
	
	public CPUMonitor(WorkerPool workerPool){
		this.workerPool = workerPool;
		this.lower_threshold = DEFAULT_LOWER_THRESHOLD;
		this.upper_threshold = DEFAULT_UPPER_THRESHOLD;
	}
	
	public void run() {
		
		client = login();
		
        while(true) {
		    try {
				// Get CPU usage averaged over all the "t1.micro" instances.
				double averageCPU = getCPUUsage("m1.small", ONE_HOUR);
				
				System.out.println(averageCPU);
				
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
    
}
