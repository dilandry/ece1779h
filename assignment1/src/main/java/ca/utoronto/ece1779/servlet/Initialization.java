package ca.utoronto.ece1779.servlet;

import javax.servlet.http.*;

import ca.utoronto.ece1779.monitor.LoadBalancer;
import ca.utoronto.ece1779.monitor.Monitor;
import ca.utoronto.ece1779.monitor.WorkerPool;

public class Initialization extends HttpServlet {

    public static final String LOAD_BALANCER = "test";
    public static final String IMAGE_ID = "ami-800658e8";
    public static final String KEY_NAME = "dustin-home";

    public void init() {
        LoadBalancer balancer = new LoadBalancer(LOAD_BALANCER);
        WorkerPool pool = new WorkerPool(balancer, IMAGE_ID, KEY_NAME);
        Monitor monitor = new Monitor(pool);

        Thread t = new Thread(monitor);
        t.start();

        this.getServletContext().setAttribute("monitor", monitor);
        this.getServletContext().setAttribute("pool", pool);
    }
}
