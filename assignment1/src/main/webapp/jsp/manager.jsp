<%@ page import="ca.utoronto.ece1779.monitor.Monitor" %>
<%@ page import="ca.utoronto.ece1779.monitor.WorkerPool" %>

<%
	// If not logged in, redirect to login
	if (session.getAttribute("username") == null){
		response.sendRedirect("login.jsp");
	}
	
	Monitor monitor = (Monitor) getServletContext().getAttribute("monitor");
	WorkerPool pool = (WorkerPool) getServletContext().getAttribute("pool");
	
	if (monitor == null) out.println("monitor is null... <br />");
%>

<!DOCTYPE html>
<html>
	<head>
		<title>Manager</title>
		<meta HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE" />
		<meta charset="utf-8">
		<script>
		function incrementValue()
		{
			var value = parseInt(document.getElementById('desiredworkercount').value, 10);
			value = isNaN(value) ? 0 : value;
			value++;
			document.getElementById('desiredworkercount').value = value;
		}
		function decrementValue()
		{
			var value = parseInt(document.getElementById('desiredworkercount').value, 10);
			value = isNaN(value) ? 1 : value;
			value--;
			document.getElementById('desiredworkercount').value = value;
		}
		
		function refreshWorkerInfo(){
			var xmlhttp=new XMLHttpRequest();
			xmlhttp.onreadystatechange=function()
			{
				if (xmlhttp.readyState==4 && xmlhttp.status==200)
				{
					var arr = JSON.parse(xmlhttp.responseText);
					var averageLoad = 0.0;
					var numWorkers = 0;
					document.getElementById("workerslist").innerHTML = "";
					for(var i=0;i<arr.length;i++){
					
						var ul = document.getElementById("workerslist");
						var li = document.createElement("li");
						li.appendChild(document.createTextNode(arr[i].name + " (load: " + arr[i].cpu_load + "%)"));
						ul.appendChild(li);
						numWorkers += 1;
						averageLoad += parseFloat(arr[i].cpu_load);
					}
					
					averageLoad = averageLoad/numWorkers;
					document.getElementById("avg_load_value_div").innerHTML = averageLoad + "%";
					document.getElementById("avg_load_bar_div").style.width = averageLoad + "%";
				}
			}
			xmlhttp.open("GET","/ManagerUI/cpuload",true);
			xmlhttp.send();
			
			// Call again in 1 minute
			setTimeout(refreshWorkerInfo, 60*1000);
		}
		window.onload = function() {
			refreshWorkerInfo();
		}
		</script>
		<style>
		</style>
	</head>

	<body>

    <div>
      <h1>Manager (<a href="/ManagerUI/jsp/logout.jsp" style="text-decoration:none;">logout</a>)</h1>
    </div> 
	
	<div>
		<h3>Current Workers (auto-refreshes every minute):</h3>
		<ul id="workerslist">
			<% 
				// if (pool.size() > 0)
				// for (String id : pool.getList()){
					// out.println("<li>"+id+"</li>");
				// }
				// else
					// out.println("No workers<br />");
			%>
		</ul>
	</div>
	
	<div>
		<h3>Average Load (auto-refreshes every minute): </h3>
		<div style="width:300px;border:1px solid black;padding:3px;"><div id="avg_load_bar_div" style="width:0%;height:30px;background-color:green;"></div></div>
		<div style="padding-left:142px;" id="avg_load_value_div">(0%)</div>
	</div>
	
	<div>
		<h3>Grow/Shrink Worker Pool</h3>
		<form action="/ManagerUI/jsp/growshrink.jsp">
			<table>
				<tr>
					<td><label>Desired number of workers:</label></td>
					<td style="padding-left:51px;"><a href="javascript:void(0)" onclick="decrementValue();" style="text-decoration: none">--</a> <input type="text" id="desiredworkercount" name="workercount" value="<%=pool.size() %>" /> <a href="javascript:void(0)" onclick="incrementValue();" style="text-decoration: none">++</a></td>
				</tr>
				<tr>
					<td colspan=2><input type="submit" value="Set" /></td>
				</tr>
			</table>
		</form>
	</div>
	
	<div>
		<h3>Configure Auto-scaling Policy</h3>
		<form action="/ManagerUI/jsp/configurepolicy.jsp">
			<table>
				<tr>
					<td><label>Threshold at which to grow pool(%):</label></td><td><input type="text" name="growthreshold" value="<%=monitor.getUpperThreshold() %>" /></td>
				</tr>
				<tr>
					<td><label>Threshold at which to shrink pool(%):</label></td><td><input type="text" name="shrinkthreshold" value="<%=monitor.getLowerThreshold() %>" /></td>
				</tr>
				<tr>
					<td><label>Ratio by which to grow pool:</label></td><td><input type="text" name="growratio" value="<%=monitor.getIncreaseRatio(0) %>" /></td>
				</tr>
				<tr>
					<td><label>Ratio by which to shrink pool:</label></td><td><input type="text" name="shrinkratio" value="<%=monitor.getDecreaseRatio() %>" /></td>
				</tr>
				<tr>
					<td colspan=2><input type="submit" value="Save" /></td>
				</tr>
			</table>
		</form>
	</div>
	
	<div>
		<h3>Clear Data</h3>
		<a href="/ManagerUI/jsp/deleteeverything.jsp" style="color:red;" onclick="javascript:return confirm('Are you sure you want to delete everything?')">Delete Everything</a>
	</div>
	
	</body>

</html>
