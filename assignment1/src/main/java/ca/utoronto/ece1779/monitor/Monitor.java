package monitor;

public class Monitor {

	final static String IMAGE_ID = "ami-5c074734";
	final static String KEY_NAME = "didier-key-pair-useast";
	
	public static void main(String[] args) {
		
		WorkerPool workerPool = new WorkerPool(IMAGE_ID, KEY_NAME);
		
		(new Thread(new CPUMonitor(workerPool))).start();

	}
}
