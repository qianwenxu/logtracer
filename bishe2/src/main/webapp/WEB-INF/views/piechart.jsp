<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>   
<title>Insert title here</title>  
</head>  
<body>  
<%    
    String graphurl="/pic/tracepie.jpg";  
    String graphurl1="/pic/errorspanbar.jpg";
%>  
<center>  
<td><img  src="/pic/tracepie${cluno}.jpg" height="400" width="600"></td>
<br><br>
<c:choose>
	<c:when test="${errornum>0}">
    <td><img  src="/pic/errorspanbar${cluno}.jpg" height="500" width="650"></td> 
    </c:when>
</c:choose> 
</center>  
</body>  
</html>  