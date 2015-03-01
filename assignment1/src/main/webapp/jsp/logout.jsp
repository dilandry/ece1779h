<%
	// If already logged in, redirect to view_images
	if (session.getAttribute("username") != null){
		session.setAttribute("username",null);
	}

	response.sendRedirect("login.jsp");
%>