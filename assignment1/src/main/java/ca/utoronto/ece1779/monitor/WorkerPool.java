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
	
	AmazonEC2 client;
	List<String> workerPoolInstanceIDs;
	
	// IMAGE_ID of the instances we're launching
	final String IMAGE_ID = "ami-5c074734";
	final String KEY_NAME = "didier-key-pair-useast";
	
	public WorkerPool(){
		
		client = login();
		workerPoolInstanceIDs = new ArrayList<String>();
		
	}
	
	public static AmazonEC2Client login(){
		ProfileCredentialsProvider legit = new ProfileCredentialsProvider();
		
		AWSCredentials accountID = legit.getCredentials();
		
		String AWSAccessKey = accountID.getAWSAccessKeyId();
		String AWSSecretKey = accountID.getAWSSecretKey();
		
		BasicAWSCredentials credentials = new BasicAWSCredentials(AWSAccessKey, AWSSecretKey);
	
		return new AmazonEC2Client(credentials);
	}
	
	public void launchInstances(int numberInstances){
		System.out.println("Launching " + numberInstances + " instance(s):");
		
		// Instances are launched one at a time or else they all shut down together.
		for (int i=0; i<numberInstances; i++){
			launchInstance();
		}
	}
	
	private void launchInstance(){
        try {
        	RunInstancesRequest request = new RunInstancesRequest(IMAGE_ID,1,1);

        	// Set key used to SSH to instance.
        	request.setKeyName(KEY_NAME);
        	
        	// Enable detailed monitoring.
        	request.setMonitoring(true);
        	
        	RunInstancesResult result = client.runInstances(request);
        	Reservation reservation = result.getReservation();
        	List<Instance> instances = reservation.getInstances();
        	
        	System.out.println("Instance Info = "
        							+ instances.get(0).toString());
        	
        	// Add instance id to the pool.
        	workerPoolInstanceIDs.add(instances.get(0).getInstanceId());
   
        	System.out.println(workerPoolInstanceIDs);

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
	
	public void terminateInstances(int numberInstances){
		if (numberInstances > workerPoolInstanceIDs.size()) return;
		
		// Make a list of the <numberInstances> first instances of the worker pool.
		// These will be terminated.
		List<String> instanceIds = new ArrayList<String>();
		for (int i=0; i<numberInstances; i++){
			instanceIds.add(workerPoolInstanceIDs.get(i));
		}
		
		// Terminate instances in list.
		try {
			System.out.println("Terminating instances " + instanceIds.toString() + ".");
		
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
}
