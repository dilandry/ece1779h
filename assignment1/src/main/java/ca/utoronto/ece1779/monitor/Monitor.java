package monitor;

public class Monitor implements Runnable{

	private static final long ONE_HOUR = 1000 * 60 * 60;
	private static final long ONE_MINUTE = 1000 * 60 * 1;
	
	private static final double DEFAULT_LOWER_THRESHOLD = 20.0;
	private static final double DEFAULT_UPPER_THRESHOLD = 80.0;
	private static final int DEFAULT_INCREASE_RATIO = 2;
	private static final int DEFAULT_DECREASE_RATIO = 2;
	
	private static final int MAX_WORKERS = 20;
	
	private static CPUMonitor mon;
	
	// Instance variables.
	private WorkerPool workerPool;
	private double lower_threshold;
	private double upper_threshold;
	private int increase_ratio;
	private int decrease_ratio;
	
	/**
	 * Constructor. Initialize instance variables to default values.
	 * 
	 * @param workerPool
	 */
	public Monitor(WorkerPool workerPool){
		this.workerPool = workerPool;
		this.lower_threshold = DEFAULT_LOWER_THRESHOLD;
		this.upper_threshold = DEFAULT_UPPER_THRESHOLD;
		this.increase_ratio = DEFAULT_INCREASE_RATIO;
		this.decrease_ratio = DEFAULT_DECREASE_RATIO;
		
		mon = new CPUMonitor();
	}
	
	/**
	 * Monitor CPU in thread and act on pool size accordingly.
	 */
	public void run() {
		
		mon = new CPUMonitor();
		int timer = 0;
		
		System.out.print("Waiting for first instance to start.");
		while (mon.getCPUbyType("m1.small", ONE_MINUTE) == 0.0) {
			sleep(15000);
			
			System.out.print(".");
		}
		System.out.println("DONE.");
		
        while(true) {
        	
        	// Get CPU usage averaged over all the "m1.small" instances.
        	double minuteAverageCPU = mon.getCPUbyType("m1.small", ONE_MINUTE);
        	
       		// Shrink worker pool if average CPU usage over a minute is
       		// over threshold.
			if (minuteAverageCPU > this.upper_threshold &&
							workerPool.size() < 20) {
				growPool();
				
				// Wait 5 minutes to allow new instances to launch.
				sleep(5*60000);
				timer += 5;
			}
        	
			double hourAverageCPU = mon.getCPUbyType("m1.small", ONE_HOUR);
			
			// Shrink worker pool if hourly average CPU usage is below threshold.
			// Timer makes sure worker pool is shrunk max 1 time per hour.
			if (hourAverageCPU < this.lower_threshold &&
							workerPool.size() > 1 &&
							timer > 60) {
				shrinkPool();
				
				timer = 0;
			}

			sleep(60000);
			timer += 1;
	    }
	}
    
    /**
     * Set multiple by which instance pool is shrunk if CPU usage under threshold.
     * 
     * @param ratio
     */
    public void setDecreaseRatio(int ratio){
    	if (ratio > 1) this.decrease_ratio = ratio;
    }
    
    /**
     * Set multiple by which instance pool is grown if CPU usage is over threshold.
     * 
     * @param ratio
     */
    public void setIncreaseRatio(int ratio){
    	if (ratio > 1) this.increase_ratio = ratio;
    }
    
    /**
     * Set CPU usage threshold over which pool is grown. To be set, new upper 
     * threshold must be < 100 and > than the lower threshold.
     * 
     * @param threshold
     */
    public void setUpperThreshold(double threshold){
    	if (this.lower_threshold < threshold && threshold < 100.0) 
    		this.upper_threshold = threshold;
    }
    
    /**
     * Set CPU usage threshold under which pool is shrunk. To be set, new lower 
     * threshold must be > 0 and < than the upper threshold.
     * 
     * @param threshold
     */
    public void setLowerThreshold(double threshold){
    	if (0.0 < threshold && threshold < this.upper_threshold) 
    		this.lower_threshold = threshold;
    }
    
    /**
     * Get multiple by which instance pool is shrunk if CPU usage under threshold.
     * 
     * @param ratio
     */
    public int getDecreaseRatio(){
    	return this.decrease_ratio;
    }
    
    /**
     * Set multiple by which instance pool is grown if CPU usage is over threshold.
     * 
     * @param ratio
     */
    public int getIncreaseRatio(int ratio){
    	return this.increase_ratio;
    }
    
    /**
     * Get CPU usage threshold over which pool is grown.
     * 
     * @param threshold
     */
    public double getUpperThreshold(){
    	return this.upper_threshold;
    }
    
    /**
     * Get CPU usage threshold under which pool is shrunk.
     * 
     * @param threshold
     */
    public double getLowerThreshold(){
    	return this.lower_threshold;
    }
    
    /**
     * Decrease pool size by [decrease_ratio]. Floor at minimum size.
     */
    private void shrinkPool()
    {
		int toTerminate = workerPool.size() - workerPool.size()/(this.decrease_ratio);
		
		System.out.println(toTerminate);
		
		// Make sure number of workers never goes below the minimum.
		if (toTerminate >= workerPool.size()){
			toTerminate = workerPool.size() - 1;
		};
		
		System.out.println("Average hourly CPU usage under threshold (" + 
				this.lower_threshold + "%). Terminating " + 
				toTerminate + " instance(s).");
		
		workerPool.terminateInstances(toTerminate);
		
    }
    
    /**
     * Increase pool size by [increase_ratio]. Ceiling at maximum size.
     */
    private void growPool(){
    	
		int toLaunch = workerPool.size()*(this.increase_ratio-1);
		
		// Do not launch more than the maximum number of workers.
		if (toLaunch + workerPool.size() > MAX_WORKERS) 
			toLaunch = MAX_WORKERS - workerPool.size();
		
		System.out.println("CPU usage over threshold (" + this.upper_threshold
				+ "%). Launching " + toLaunch + " new instance(s).");
		
		workerPool.launchInstances(toLaunch);
    }
    
    /**
     * Self-explanatory...
     * 
     * @param time
     */
    public static void sleep(int time){
	    try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}

