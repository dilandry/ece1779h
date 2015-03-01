<%@ page import="ca.utoronto.ece1779.monitor.Monitor" %>
<%@ page import="ca.utoronto.ece1779.monitor.WorkerPool" %>

<%
	// If not logged in, redirect to login
	if (session.getAttribute("username") == null){
		response.sendRedirect("login.jsp");
	}
	
	int desired_worker_count = Integer.parseInt(request.getParameter("workercount"));
	
	WorkerPool pool = (WorkerPool) getServletContext().getAttribute("pool");
	
	if (pool.size() > desired_worker_count){
		pool.terminateInstances(pool.size() - desired_worker_count);
	}else if (pool.size() < desired_worker_count){
		pool.launchInstances(desired_worker_count - pool.size());
	}
	
%>


<!DOCTYPE html>
<html>
	<head>
		<title>Number of Workers Set</title>
		<meta HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE" />
		<meta charset="utf-8">
		<script>
		</script>
	</head>

	<body>

    <div>
      <h1>Number of Workers Set</h1>
      <h2>Click <a href="/ManagerUI/jsp/manager.jsp">here</a> to return to manager.</h2>
	  <h3>Note that it may take a minute for the workers to show up!</h3>
    </div> 

	</body>

</html>
