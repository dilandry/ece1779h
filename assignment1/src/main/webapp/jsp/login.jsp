<%
	// If already logged in, redirect to view_images
	if (session.getAttribute("username") != null){
		response.sendRedirect("manager.jsp");
		return;
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
		return;
	}

%>

<!DOCTYPE html>
<html>
	<head>
		<title>Welcome! Please login.</title>
		<meta HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE" />
		<meta charset="utf-8">
        <!-- Custom styles for this template -->
		<!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
        <link href="../css/signin.css" rel="stylesheet">
   		<script src="//code.jquery.com/jquery-1.11.2.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js"></script>
	</head>

	<body>
	    <div class="container">
            <form class="form-signin" name="sign_in_form" action="/ManagerUI/jsp/login.jsp" method="post">
                <h1 class="form-signin-heading">Manager Login</h1>
                <label for="username" class-"sr-only">Username</label>
                <input type="text" id="username" class="form-control" placeholder="Username" name="username" required autofocus>
                <label for="inputPassword" class="sr-only">Password</label>
                <input for="inputPassword" class="form-control" placeholder="Password" type="password" name="password" required>
                <button class="btn btn-lg btn-primary btn-block" type="submit">Login</button>
            </form>
        </div>
	</body>
</html>
