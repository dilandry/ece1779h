package monitor;

public class Monitor {

	public static void main(String[] args) {
		
		WorkerPool workerPool = new WorkerPool();
		
		(new Thread(new CPUMonitor(workerPool))).start();

	}
}
