<%@ page import="ca.utoronto.ece1779.database.DeleteAll" %>

<%
	// If not logged in, redirect to login
	if (session.getAttribute("username") == null){
		response.sendRedirect("login.jsp");
	}
	
	DeleteAll.deleteAll();
%>

<!DOCTYPE html>
<html>
	<head>
		<title>database and s3 contents deleted</title>
		<meta HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE" />
		<meta charset="utf-8">
		<script>
		</script>
	</head>

	<body>

    <div>
      <h1>Database and s3 contents deleted!</h1>
      <h2>Click <a href="/ManagerUI/jsp/manager.jsp">here</a> to return to manager.</h2>
    </div> 

	</body>

</html>
