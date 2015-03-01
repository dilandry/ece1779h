<%@ page import="ca.utoronto.ece1779.monitor.Monitor" %>

<%
	// If not logged in, redirect to login
	if (session.getAttribute("username") == null){
		response.sendRedirect("login.jsp");
	}
	
	double growthreshold = Double.parseDouble(request.getParameter("growthreshold"));
	double shrinkthreshold = Double.parseDouble(request.getParameter("shrinkthreshold"));
	int growratio = Integer.parseInt(request.getParameter("growratio"));
	int shrinkratio = Integer.parseInt(request.getParameter("shrinkratio"));
	
	Monitor monitor = (Monitor) getServletContext().getAttribute("monitor");
	
	monitor.setDecreaseRatio(shrinkratio);
	monitor.setIncreaseRatio(growratio);
	monitor.setUpperThreshold(growthreshold);
	monitor.setLowerThreshold(shrinkthreshold);
	
%>

<!DOCTYPE html>
<html>
	<head>
		<title>Policy Configured</title>
		<meta HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE" />
		<meta charset="utf-8">
		<script>
		</script>
	</head>

	<body>

    <div>
      <h1>Policy Configured</h1>
      <h2>Click <a href="/ManagerUI/jsp/manager.jsp">here</a> to return to manager.</h2>
    </div> 

	</body>

</html>
