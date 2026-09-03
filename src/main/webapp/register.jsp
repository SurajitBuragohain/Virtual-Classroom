<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Register | Virtual Classroom</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260827">
</head>
<body>
<div class="box">
<h1>Register</h1>
<% if (error != null && !error.isBlank()) { %>
<p class="err"><%= error %></p>
<% } %>
<form action="${pageContext.request.contextPath}/register" method="post" autocomplete="off">
<input type="text" name="name" placeholder="Name" maxlength="100" required>
<input type="email" name="email" placeholder="Email" maxlength="100" required>
<div class="password-box">
<input id="registerPassword" name="password" type="password" placeholder="Password" autocomplete="new-password" required>
<button type="button" id="registerPasswordBtn" class="password-toggle">Show</button>
</div>
<input type="tel" name="phone" placeholder="Phone (10 digits)" maxlength="10" minlength="10" pattern="[0-9]{10}" inputmode="numeric" title="Enter exactly 10 digits">
<select name="role" id="role" required>
<option value="">Select Role</option>
<option value="TEACHER">Teacher</option>
<option value="STUDENT">Student</option>
</select>
<div id="tb" style="display:none;">
<select name="teacherId" id="teacherSelect">
<option value="">Choose Assigned Teacher</option>
<%
java.util.List<String[]> teachers = (java.util.List<String[]>) request.getAttribute("teachers");
if (teachers != null) {
    for (String[] teacher : teachers) {
%>
<option value="<%= teacher[0] %>"><%= teacher[1] %></option>
<%
    }
}
%>
</select>
</div>
<button type="submit">Register</button>
</form>
<p class="muted">Already have an account? <a href="${pageContext.request.contextPath}/index.jsp">Login</a></p>
</div>
<script>
const role = document.getElementById('role');
const teacherBox = document.getElementById('tb');
const teacherSelect = document.getElementById('teacherSelect');
function updateTeacherField() {
    const student = role.value === 'STUDENT';
    teacherBox.style.display = student ? 'block' : 'none';
    teacherSelect.required = student;
    if (!student) teacherSelect.value = '';
}
role.addEventListener('change', updateTeacherField);
const password = document.getElementById('registerPassword');
const button = document.getElementById('registerPasswordBtn');
button.addEventListener('click', function () {
    const show = password.type === 'password';
    password.type = show ? 'text' : 'password';
    button.textContent = show ? 'Hide' : 'Show';
});
</script>
</body>
</html>
