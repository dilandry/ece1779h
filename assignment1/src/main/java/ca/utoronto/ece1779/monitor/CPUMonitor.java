package monitor;

import java.awt.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.amazonaws.auth.*;
import com.amazonaws.auth.profile.*;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;

public class CPUMonitor {

	private static final long ONE_HOUR = 1000 * 60 * 60;
	
	private static AmazonCloudWatchClient client;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		client = login();
		
		// Get CPU usage averaged over all the "t1.micro" instances.
		double averageCPU = getCPUUsage("t1.micro", ONE_HOUR);
		
		System.out.println(averageCPU);
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
		
		// Makes sense only because there is only one data point.
		return result.getDatapoints().get(0).getAverage();	
    }

    private static void toStdOut(final GetMetricStatisticsResult result, final String instanceId) {
        System.out.println(result); // outputs empty result: {Label: CPUUtilization,Datapoints: []}
        for (final Datapoint dataPoint : result.getDatapoints()) {
            System.out.printf("%s instance's average CPU utilization : %s%n", instanceId, dataPoint.getAverage());      
            System.out.printf("%s instance's max CPU utilization : %s%n", instanceId, dataPoint.getMaximum());
        }
    }

}
