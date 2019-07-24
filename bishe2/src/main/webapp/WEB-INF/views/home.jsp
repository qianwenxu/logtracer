<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Home</title>
	<meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://cdn.staticfile.org/twitter-bootstrap/4.1.0/css/bootstrap.min.css">
    <script src="https://cdn.staticfile.org/jquery/3.2.1/jquery.min.js"></script>
    <script src="https://cdn.staticfile.org/popper.js/1.12.5/umd/popper.min.js"></script>
    <script src="https://cdn.staticfile.org/twitter-bootstrap/4.1.0/js/bootstrap.min.js"></script>
	<script>
	function search(){
		form1.submit();
	}
	</script>
</head>
<body style="background:url(/pic/back1.jpg);background-size:contain;">
<div style="padding: 150px 360px;">
<form action="search" id="form1" style="width:500;height:300;">
<h6>
	Please enter which index to search:
</h6>
<div class="input-group mb-3">
      <input type="text" class="form-control" id="index" name="index">
</div>
<br>
<h6>
	Please enter how many clusters are divided into:
</h6>
<div class="input-group mb-3">
<input type="text" class="form-control" id="searchstr" name="searchstr">
</div>
<br>
<input class="btn btn-large btn-primary " type="button" value="Search" onclick="search()">
</form>
</div>
</body>
</html>
