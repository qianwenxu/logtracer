<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Insert title here</title>
<link rel="stylesheet" href="https://cdn.staticfile.org/twitter-bootstrap/3.3.7/css/bootstrap.min.css">
<script src="https://cdn.staticfile.org/jquery/2.1.1/jquery.min.js"></script>
<script src="https://cdn.staticfile.org/twitter-bootstrap/3.3.7/js/bootstrap.min.js"></script>
<script>
	/*function detail(){
		form2.submit();
	}
	function trace(){
		form3.submit();
	}*/
	$(document).ready(function(){
		$("#button1").click(function(){
			$.ajax({
				async: false,
	            type: "POST",
	            url:'${pageContext.request.contextPath}/detail',
	            //contentType: false,  // 告诉jQuery不要去设置Content-Type请求头
	            data:$('#form2').serialize(),
				success:function(result){
				$("#content").html(result);
				}
			});
		});
		$("#button2").click(function(){
			$.ajax({
				async: false,
	            type: "POST",
	            url:'${pageContext.request.contextPath}/trace',
	            //contentType: false,  // 告诉jQuery不要去设置Content-Type请求头
	            data:$('#form3').serialize(),
				success:function(result){
				$("#content").html(result);
				}
			});
		});
	});
	function search(){
		form4.submit();
	}
</script>
</head>
<body>
<nav class="navbar navbar-default" role="navigation">
	<div class="container-fluid"> 
	<div class="navbar-header">
		<a class="navbar-brand" href="#">错误分析</a>
	</div>
	<!--搜索detail  -->
	<form id="form2" class="navbar-form navbar-left">
		<div class="form-group">
			<input type="text" id="clusterNo" name="clusterNo" class="form-control" placeholder="clusterNo" required="required">
		</div>
		<input type="button" value="提交" id="button1" class="btn btn-default">
	</form>
	<div class="navbar-header">
		<a class="navbar-brand" href="#">调用链</a>
	</div>
	<form id="form3" class="navbar-form navbar-left">
		<div class="form-group">
			<input type="text" id="clusterNo" name="clusterNo" class="form-control" placeholder="clusterNo"  required="required">
		</div>
		<input type="button" value="提交" id="button2" class="btn btn-default">
	</form>
	<div class="navbar-header">
		<a class="navbar-brand" href="#">K值</a>
	</div>
	<form id="form4" class="navbar-form navbar-left">
		<div class="form-group">
			<input type="text" id="KValue" name="searchstr" id="searchstr" class="form-control" placeholder="KValue"  required="required">
		</div>
		<input type="button" value="提交" id="button3" class="btn btn-default" onclick="search()">
	</form>
	</div>
</nav>

<!--展示cluster  -->
<div data-spy="scroll" data-target="#navbar-example" data-offset="0" 
	 style="height:550px;width:25%;overflow:auto; position: relative;float: left;">
	<c:set var="i" value="0" /> 
	<c:forEach items="${kmeansres1}" var="onclu">
	<div class="list-group">
		<a href="#" class="list-group-item active">
			<div style="color:white;font-size:24px;font-weight: bold;">Cluster <c:out value="${i}"/></div>
		</a>

		<c:forEach items="${onclu}" var="arr">
		<a href="#" class="list-group-item">
	    <c:choose>
	    <c:when test="${arr.getiserror()>0}">
	    <span style="color:#FF0000;font-size:14px;" class="list-group-item-heading">${arr.gettraceid()}</span>
	    </c:when>
	    <c:otherwise>
	    <span style="color:#009100;font-size:14px;" class="list-group-item-heading">${arr.gettraceid()}</span>
	    </c:otherwise>
	    </c:choose>
	    </a>
    	</c:forEach>
		
	</div>
	<c:set var="i" value="${i+1}" /> 
	</c:forEach>
</div>

<div class="pre-scrollable" style="height:550px;width:73%;float: right;max-height: 550px;" id="div1">
     <!-- 载入左侧菜单指向的jsp（或html等）页面内容 -->
     <div id="content">             
     <h4>                    
           <strong>使用指南：</strong><br>
           <br><br>默认页面内容……
     </h4>                                 
                              
     </div>  
</div> 
</body>
</html>