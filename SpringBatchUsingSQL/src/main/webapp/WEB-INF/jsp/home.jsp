<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Insert title here</title>
</head>
<body>

<h2> AUTO SCHEDULING </h2><br>
<a href="auto">Click Here</a><br>



<h2> MANUAL SCHEDULING </h2><br>
<a href="manual">Click Here</a>


<c:if test="${checkManual eq 'manual'}">
<form action="/manualmode" method="get">
<table>
<c:forEach items="${file}" var="fileName" >
<tr> <td><input type="checkbox" name="sqlfile" value= "${fileName}"></td><td>
   ${fileName}</td>
</tr>
</c:forEach>
  </table>
  
Scheduling Time:  <input type="datetime-local"  value="2020-10-26 22:00:00" step="1" name="ScheTime">
<input type="submit" value="Submit">
</form>
</c:if>
</body>
</html>