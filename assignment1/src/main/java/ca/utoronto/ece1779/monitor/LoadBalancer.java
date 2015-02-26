package monitor;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;

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
	
	public void deregister(List<String> instanceIds){
		List<Instance> instances = new ArrayList<Instance>();
		
		for (int i=0; i<instanceIds.size(); i++){
			Instance instance = new Instance(instanceIds.get(i));
			instances.add(instance);
		}
		
		System.out.println("Deregistering instance(s) \"" + instanceIds.toString() + "\"" +
				   " from load balancer \"" + load_balancer_name + "\".");
		
		DeregisterInstancesFromLoadBalancerRequest request = 
				new DeregisterInstancesFromLoadBalancerRequest(load_balancer_name, instances);
		
		DeregisterInstancesFromLoadBalancerResult result = 
				client.deregisterInstancesFromLoadBalancer(request);
	}
	
	public void register(String instanceId){
		Instance instance = new Instance(instanceId);
		
		List<Instance> instances = new ArrayList<Instance>();
		
		instances.add(instance);
		
		System.out.println("Registering instance \"" + instanceId + "\"" +
						   " in load balancer \"" + load_balancer_name + "\".");
		
		RegisterInstancesWithLoadBalancerRequest request = 
				new RegisterInstancesWithLoadBalancerRequest(load_balancer_name, instances);
		
		RegisterInstancesWithLoadBalancerResult result =
				client.registerInstancesWithLoadBalancer(request);
	}
}
