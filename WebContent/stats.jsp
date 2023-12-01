<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>F355 Challenge statistics</title>
</head>
<%@ page import="com.flyinghead.f355.Races" %>
<%@ page import="java.util.Date" %>
<body>
<h3>F355 Challenge statistics</h3><br>
<% Races races = (Races)application.getAttribute("races"); %>
Races: <%=races.getRaceCount() %><br>
<h4>Waiting list</h4>
Novice: <%=races.getWaitingListSize(false) %><br>
Intermediate: <%=races.getWaitingListSize(true) %><br>
</body>
</html>
