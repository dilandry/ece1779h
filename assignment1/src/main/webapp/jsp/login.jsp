<%
	// If already logged in, redirect to view_images
	if (session.getAttribute("username") != null){
		response.sendRedirect("manager.jsp");
	}

	// Check for parameters, if avail, log them in
	String username = request.getParameter("username");
	String password = request.getParameter("password");
	String realUsername = (String)getServletContext().getInitParameter("username");
	String realPassword = (String)getServletContext().getInitParameter("password");
	if (username != null &&
		password != null &&
		username.equals(realUsername) &&
		password.equals(realPassword)){
		session.setAttribute("username",username);
		response.sendRedirect("manager.jsp");
	}

%>

<!DOCTYPE html>
<html>
	<head>
		<title>Welcome! Please login.</title>
		<meta HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE" />
		<meta charset="utf-8">
		<script>
		</script>
	</head>

	<body>

    <div>
      <h1>ECE1779 Project 1 - Manager Login</h1>
    </div> 

	<form name="sign_in_form" action="/ManagerUI/jsp/login.jsp" method="post">
		<table>
			<tr>
				<td><label>Username</label></td>
				<td><input type="text" name="username"/></td>
			</tr>
			<tr>
				<td><label>Password</label></td>
				<td><input type="password" name="password"/></td>
			</tr>
			<tr>
				<td colspan=2><input type="submit" value="Login" /></td>
			</tr>
		</table>
	</form>
	
	</body>

</html>
