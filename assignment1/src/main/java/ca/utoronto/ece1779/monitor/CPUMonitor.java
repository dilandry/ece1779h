package ca.utoronto.ece1779.monitor;

import java.util.Date;

import com.amazonaws.auth.*;
import com.amazonaws.auth.profile.*;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;

public class CPUMonitor {
	
	private AmazonCloudWatchClient client;
	
	/**
	 * Constructor. Create client object with credentials.
	 * 
	 * @param workerPool
	 */
	public CPUMonitor(){
		this.client = login();
	}
	
	/**
	 * Login to AWS cloud watch with credentials in ~/.aws/credentials
	 * 
	 * @return AmazonCloudWatchClient object, logged in.
	 */
	private AmazonCloudWatchClient login(){
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
    private GetMetricStatisticsRequest requestId(String instanceId,
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
    private GetMetricStatisticsRequest requestType(String instanceType,
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
    private GetMetricStatisticsResult result(final GetMetricStatisticsRequest request) {
         return client.getMetricStatistics(request);
    }
    
    /**
     * Return average CPU usage over "period" (in millisec) for instances of type
     * "instanceType".
     * 
     * @param instanceId
     * @param period
     * @return
     */
    public double getCPUbyType(String instanceType, long period){
		GetMetricStatisticsRequest request = requestType(instanceType, period);
		GetMetricStatisticsResult result = result(request);
		
		// In case no instance of monitored type is launched yet.
		if (result.getDatapoints().size() <= 0) return 0.0;
		
		// Makes sense only because there is only one data point.
		return result.getDatapoints().get(0).getAverage();	
    }
    
    /**
     * Return average CPU usage over "period" (in millisec) for instance "instanceId".
     * 
     * @param instanceId
     * @param period
     * @return
     */
    public double getCPUbyId(String instanceId, long period){
		GetMetricStatisticsRequest request = requestId(instanceId, period);
		GetMetricStatisticsResult result = result(request);
		
		// In case instance is not booted yet.
		if (result.getDatapoints().size() <= 0) return 0.0;
		
		// Makes sense only because there is only one data point.
		return result.getDatapoints().get(0).getAverage();	
    }
}
