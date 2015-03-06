package ca.utoronto.ece1779.monitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.*;

public class LoadBalancer {

    public String getLoad_balancer_name() {
        return load_balancer_name;
    }

    private String load_balancer_name;

	private AmazonElasticLoadBalancingClient client;

	public LoadBalancer(String load_balancer_name){
		this.load_balancer_name = load_balancer_name;

		client = login();
	}

	/**
	 * Return a load balancer client with credentials in ~/.aws/credentials
	 *
	 * @return
	 */
	public AmazonElasticLoadBalancingClient login(){
		ProfileCredentialsProvider legit = new ProfileCredentialsProvider();

		AWSCredentials accountID = legit.getCredentials();

		String AWSAccessKey = accountID.getAWSAccessKeyId();
		String AWSSecretKey = accountID.getAWSSecretKey();

		BasicAWSCredentials credentials = new BasicAWSCredentials(AWSAccessKey, AWSSecretKey);

		return new AmazonElasticLoadBalancingClient(credentials);
	}

	/**
	 * Deregister (multiple) instances in list from load balancer.
	 *
	 * @param instanceIds
	 */
	public void deregister(List<String> instanceIds){
		List<Instance> instances = new ArrayList<Instance>();

		for (int i=0; i<instanceIds.size(); i++){
			Instance instance = new Instance(instanceIds.get(i));
			instances.add(instance);
		}

		System.out.println("Deregistering instance(s) \"" + instanceIds.toString() + "\"" +
				   " from load balancer \"" + load_balancer_name + "\".");

		// Generate the request object.
		DeregisterInstancesFromLoadBalancerRequest request =
				new DeregisterInstancesFromLoadBalancerRequest(load_balancer_name, instances);

		// Send the object.
		DeregisterInstancesFromLoadBalancerResult result =
				client.deregisterInstancesFromLoadBalancer(request);

	}

	/**
	 * Register one instance with the load balancer.
	 *
	 * @param instanceId
	 */
	public void register(String instanceId){
		Instance instance = new Instance(instanceId);

		List<Instance> instances = new ArrayList<Instance>();

		instances.add(instance);

		System.out.println("Registering instance \"" + instanceId + "\"" +
						   " in load balancer \"" + load_balancer_name + "\".");

		// Generate a request object.
		RegisterInstancesWithLoadBalancerRequest request =
				new RegisterInstancesWithLoadBalancerRequest(load_balancer_name, instances);

		// Send the request.
		RegisterInstancesWithLoadBalancerResult result =
				client.registerInstancesWithLoadBalancer(request);
	}

    public List<String> getActiveInstances() {
        client.describeLoadBalancers().getLoadBalancerDescriptions();
        List<String> instanceNames = new LinkedList<String>();
        List<LoadBalancerDescription> descriptions = client.describeLoadBalancers().getLoadBalancerDescriptions();

        for (LoadBalancerDescription desc: descriptions) {
            if (desc.getLoadBalancerName().equals(load_balancer_name)) {
                List<Instance> instances = desc.getInstances();

                for (Instance a : instances) {
                    instanceNames.add(a.getInstanceId());
                }
                return instanceNames;
            }
        }
        // if we reached here, bad things happened
        return instanceNames;
    }
}
