package monitor;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;

public class LoadBalancer {
	
	private String load_balancer_name;
	
	private AmazonElasticLoadBalancingClient client;
	
	public LoadBalancer(String load_balancer_name){
		this.load_balancer_name = load_balancer_name;
		
		client = login();
	}
	
	public AmazonElasticLoadBalancingClient login(){
		ProfileCredentialsProvider legit = new ProfileCredentialsProvider();
		
		AWSCredentials accountID = legit.getCredentials();
		
		String AWSAccessKey = accountID.getAWSAccessKeyId();
		String AWSSecretKey = accountID.getAWSSecretKey();
		
		BasicAWSCredentials credentials = new BasicAWSCredentials(AWSAccessKey, AWSSecretKey);
	
		return new AmazonElasticLoadBalancingClient(credentials);
	}
	
	public void register(String instanceId){
		Instance instance = new Instance(instanceId);
		
		List<Instance> instances = new ArrayList<Instance>();
		
		instances.add(instance);
		
		RegisterInstancesWithLoadBalancerRequest request = 
				new RegisterInstancesWithLoadBalancerRequest(load_balancer_name, instances);
	}
}
