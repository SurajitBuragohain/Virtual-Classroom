<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>Login | Virtual Classroom</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260827">
</head>
<body>

<div class="box">

<h1>Virtual Classroom</h1>

<%
String error=request.getParameter("error");
String msg=request.getParameter("msg");

if(error!=null&&!error.isBlank()){
%>
<p class="err"><%=error%></p>
<%
}

if(msg!=null&&!msg.isBlank()){
%>
<p class="success"><%=msg%></p>
<%
}
%>

<form action="${pageContext.request.contextPath}/login" method="post" autocomplete="off">

<input
id="loginEmail"
name="email"
type="email"
placeholder="Email"
autocomplete="username"
required>

<div class="password-box">

<input
id="loginPassword"
name="password"
type="password"
placeholder="Password"
autocomplete="current-password"
required>

<button
type="button"
id="toggleLoginPassword"
class="password-toggle">
Show
</button>

</div>

<button type="submit">
Login
</button>

</form>

<p class="muted">
<a href="${pageContext.request.contextPath}/teacher-list">
Register
</a>
</p>

</div>

<script>
const password=document.getElementById("loginPassword");
const button=document.getElementById("toggleLoginPassword");

button.addEventListener("click",function(){

if(password.type==="password"){
password.type="text";
button.textContent="Hide";
}else{
password.type="password";
button.textContent="Show";
}

});
</script>

</body>
</html>