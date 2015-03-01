package ca.utoronto.ece1779.monitor;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.amazonaws.auth.*;
import com.amazonaws.auth.profile.*;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;

public class CPUMonitor {

	private AmazonCloudWatchClient client;
	
	/**
	 * Constructor. Create client object with credentials.
	 * 
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
    private GetMetricStatisticsRequest requestId(String instanceId) {
        // Request cpu usage for a period "period", over a time span of "period".
        // Yields one data point. More are possible (if period<start time).
        return new GetMetricStatisticsRequest()
            .withStartTime(new Date(new Date().getTime() - 1200000))
            .withNamespace("AWS/EC2")
            .withPeriod(60)
            .withDimensions(new Dimension().withName("InstanceId").withValue(instanceId))
            .withMetricName("CPUUtilization")
            .withStatistics("Average")
            .withEndTime(new Date());
    }
    
    /**
     * Request aggregated CPU usage statistics for all instances of a given type.
     * 
     * @param instanceType
     * @param period
     * @return
     */
    private GetMetricStatisticsRequest requestType(String instanceType, int period) {
    	// Request cpu usage for a period "period", over a time span of "period".
    	// Yields one data point. More are possible (if period<start time).
    	return new GetMetricStatisticsRequest()
    		.withStartTime(new Date(new Date().getTime()- 1200000))
    		.withNamespace("AWS/EC2")
    		.withPeriod(period)
    		.withDimensions(new Dimension().withName("InstanceType").withValue(instanceType))
    		.withMetricName("CPUUtilization")
            .withStatistics("Average")
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
     * @param instanceType
     * @return
     */
    public double getCPUbyType(String instanceType, int period){
		GetMetricStatisticsRequest request = requestType(instanceType, period);
		GetMetricStatisticsResult result = result(request);
		
		// In case no instance of monitored type is launched yet.
		if (result.getDatapoints().size() <= 0) return 0.0;

        List<Datapoint> data = result.getDatapoints();

        Collections.sort(data, new SortDatapoint());
        System.out.println("m1: " + data);
        return data.get(data.size() - 1).getAverage();
    }
    
    /**
     * Return average CPU usage over "period" (in millisec) for instance "instanceId".
     * 
     * @param instanceId
     * @param period
     * @return
     */
    public double getCPUbyId(String instanceId){
		GetMetricStatisticsRequest request = requestId(instanceId);
		GetMetricStatisticsResult result = result(request);
        System.out.println(result.toString());
		// In case instance is not booted yet.
		if (result.getDatapoints().size() <= 0) return 0.0;
		
        List<Datapoint> data = result.getDatapoints();
        
        Collections.sort(data, new SortDatapoint());
		return data.get(data.size() - 1).getAverage();
    }
}
