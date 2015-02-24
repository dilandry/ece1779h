package monitor;

public class Monitor {

	public static void main(String[] args) {
		
		WorkerPool workerPool = new WorkerPool();
		
		(new Thread(new CPUMonitor(workerPool))).start();

		workerPool.launchInstances(4);
		
		workerPool.terminateInstances(3);
	}
}
