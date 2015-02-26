package monitor;

public class Monitor {

	final static String LOAD_BALANCER_NAME = "ece1779-lb-2";
	
	final static String IMAGE_ID = "ami-5c074734";
	final static String KEY_NAME = "didier-key-pair-useast";
	
	public static void main(String[] args) {
		
		LoadBalancer loadBalancer = new LoadBalancer(LOAD_BALANCER_NAME);
		
		WorkerPool workerPool = new WorkerPool(loadBalancer, IMAGE_ID, KEY_NAME);
		
		(new Thread(new CPUMonitor(workerPool))).start();

	}
}
