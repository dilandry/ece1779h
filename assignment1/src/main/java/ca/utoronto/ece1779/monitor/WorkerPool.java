package monitor;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class WorkerPool {
	
	private AmazonEC2 client;
	private List<String> workerPoolInstanceIDs;
	
	// Image ID of the (clone) instances we're launching
	private String image_id;
	// SSH key to use for instances
	private String key_name;
	// Load balancer to which instances will be added.
	private LoadBalancer loadBalancer;
	
	public WorkerPool(LoadBalancer loadBalancer, String image_id, String key_name){
		
		this.loadBalancer = loadBalancer;
		this.image_id = image_id;
		this.key_name = key_name;
		
		client = login();
		
		workerPoolInstanceIDs = new ArrayList<String>();
		
		launchInstances(1);
		
	}
	
	/**
	 * Login using credentials in ~/.aws/credentials and return a client object.
	 * 
	 * @return
	 */
	public static AmazonEC2Client login(){
		// Object that gets the AWS credentials in ~/.aws/credentials
		ProfileCredentialsProvider legit = new ProfileCredentialsProvider();
		
		AWSCredentials accountID = legit.getCredentials();
		
		String AWSAccessKey = accountID.getAWSAccessKeyId();
		String AWSSecretKey = accountID.getAWSSecretKey();
		
		BasicAWSCredentials credentials = new BasicAWSCredentials(AWSAccessKey, AWSSecretKey);
	
		return new AmazonEC2Client(credentials);
	}
	
	/**
	 * Launch multiple instances.
	 * 
	 * @param numberInstances
	 */
	public void launchInstances(int numberInstances){
		System.out.println("Launching " + numberInstances + " instance(s):");
		
		// Instances are launched one at a time or else they all shut down together.
		for (int i=0; i<numberInstances; i++){
			launchInstance();
		}
	}
	
	/**
	 * Launch a (single) instance and add it to the worker pool.
	 */
	private void launchInstance(){
        try {
        	RunInstancesRequest request = new RunInstancesRequest(image_id,1,1);

        	// Set key used to SSH to instance.
        	request.setKeyName(key_name);
        	
        	// Enable detailed monitoring.
        	request.setMonitoring(true);
        	
        	RunInstancesResult result = client.runInstances(request);
        	Reservation reservation = result.getReservation();
        	List<Instance> instances = reservation.getInstances();
        	
        	System.out.println("Instance Info = "
        							+ instances.get(0).toString());
        	
        	// Add instance to the load balancer.
        	loadBalancer.register(instances.get(0).getInstanceId());
        	
        	// Add instance id to the pool.
        	workerPoolInstanceIDs.add(instances.get(0).getInstanceId());

       } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon EC2, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with EC2, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
	}
	
	/**
	 * Get ArrayList of all the worker's instance ids.
	 * @return
	 */
	public ArrayList<String> getList(){
		return new ArrayList<String>(workerPoolInstanceIDs);
	}

	/**
	 * Get worker pool size.
	 * 
	 * @return
	 */
	public int size(){
		return workerPoolInstanceIDs.size();
	}
	
	/**
	 * Terminate all instances in worker pool.
	 * 
	 */
	public void terminateAll(){
		loadBalancer.deregister(workerPoolInstanceIDs);
		
		terminateInstance(workerPoolInstanceIDs);
	}
	
	/**
	 * Terminate instances in list "instanceIds".
	 * 
	 * @param instanceIds
	 */
	public void terminateInstance(List<String> instanceIds){
		if (instanceIds.size() == 0) return;
		
		System.out.println("Terminating instance(s) " + instanceIds.toString() + ".");
		
		loadBalancer.deregister(instanceIds);
		
		// Terminate instances in list.
		try {		
		    TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest(instanceIds);
		    client.terminateInstances(terminateRequest);
		} catch (AmazonServiceException e) {
		    // Write out any exceptions that may have occurred.
		    System.out.println("Error terminating instances");
		    System.out.println("Caught Exception: " + e.getMessage());
		    System.out.println("Reponse Status Code: " + e.getStatusCode());
		    System.out.println("Error Code: " + e.getErrorCode());
		    System.out.println("Request ID: " + e.getRequestId());
		}
	}
	
	/**
	 * Terminate a number (numberInstances) of instances from the pool.
	 * 
	 * @param numberInstances
	 */
	public void terminateInstances(int numberInstances){
		if (numberInstances > workerPoolInstanceIDs.size()) return;
		
		// Make a list of the <numberInstances> first instances of the worker pool.
		// These will be terminated.
		List<String> instanceIds = new ArrayList<String>();
		for (int i=0; i<numberInstances; i++){
			instanceIds.add(workerPoolInstanceIDs.remove(0));
		}
		
		terminateInstance(instanceIds);
	}
}
