package ca.utoronto.ece1779.servlet;

import ca.utoronto.ece1779.monitor.CPUMonitor;
import ca.utoronto.ece1779.monitor.WorkerPool;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkerStatus extends HttpServlet {

    private static CPUMonitor cpuMonitor = new CPUMonitor();

    private static final int CPU_PERIOD = 500;
    private static final Gson GSON = new Gson();

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        List<Map> workerList  = new LinkedList<Map>();

        WorkerPool worker = (WorkerPool) this.getServletContext().getAttribute("pool");

        List<String> workerNameList = worker.getList();

        for (String workerName : workerNameList) {
            Map<String, String> workerLoad = new HashMap<String, String>();
            workerLoad.put("name", workerName);
            workerLoad.put("cpu_load", "" + cpuMonitor.getCPUbyId(workerName, CPU_PERIOD));

            workerList.add(workerLoad);
        }

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        out.println(GSON.toJson(workerList));
    }
}
