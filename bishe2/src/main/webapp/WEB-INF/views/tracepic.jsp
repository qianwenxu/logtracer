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
    String graphurl="/pic/trace.jpg";  
%>  
<center>  
<td><img  src="<%=graphurl%>" height="300" width="900"></td>
<table border="1" cellspacing="0" cellpadding="0">
<tr>
<th width="500" style="background-color:#6495ED;color:white;font-size:24px;font-weight: bold">trace id</th>
<th width="300" style="background-color:#6495ED;color:white;font-size:24px;font-weight: bold">span sequence</th>
</tr>
<c:forEach items="${clustertrace}" var="arr">
<tr>
<td>${arr.gettraceid()}</td>
<td>${arr.getspanseq()}</td>
</tr>
</c:forEach>
</table>
</center>
<br>
<br>
<br>  
</body>  
</html>  