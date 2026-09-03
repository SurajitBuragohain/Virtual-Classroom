<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.vc.servlet.DashboardServlet" %>

<%
    String role = (String) session.getAttribute("role");

    if (session.getAttribute("userId") == null || role == null) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }

    if ("ADMIN".equalsIgnoreCase(role)) {
        response.sendRedirect(request.getContextPath() + "/admin");
        return;
    }

    List<?> classrooms = null;

    if ("TEACHER".equalsIgnoreCase(role)) {
        classrooms = (List<?>) request.getAttribute("teacherClassrooms");
    } else if ("STUDENT".equalsIgnoreCase(role)) {
        classrooms = (List<?>) request.getAttribute("studentClassrooms");
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard | Virtual Classroom</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>

<nav>
    <span>Virtual Classroom</span>

    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</nav>

<main>

    <h1>
        Welcome, <%= session.getAttribute("name") %>!
    </h1>

    <% if ("TEACHER".equalsIgnoreCase(role)) { %>

        <div class="card">
            <h2>Teacher Dashboard</h2>

            <p class="muted">
                Manage your classrooms, assignments and online classes.
            </p>

            <p>
                <a class="button-link"
                   href="<%= request.getContextPath() %>/classrooms">
                    Manage Classrooms
                </a>
            </p>
        </div>

    <% } else { %>

        <div class="card">
            <h2>Student Dashboard</h2>

            <p class="muted">
                View your enrolled classrooms and course work.
            </p>

            <p>
                <a class="button-link"
                   href="<%= request.getContextPath() %>/classrooms">
                    Browse Classrooms
                </a>
            </p>
        </div>

    <% } %>

    <h2>My Classrooms</h2>

    <div class="grid">

        <% if (classrooms != null && !classrooms.isEmpty()) { %>

            <% for (Object obj : classrooms) {
                DashboardServlet.ClassroomRow classroom =
                        (DashboardServlet.ClassroomRow) obj;
            %>

                <div class="card online-card">

                    <h3>
                        <%= classroom.className %>
                    </h3>

                    <p>
                        <b>Subject:</b>
                        <%= classroom.subject %>
                    </p>

                    <p>
                        <b>Description:</b>
                        <%= classroom.description == null
                                ? "No description available."
                                : classroom.description %>
                    </p>

                    <a href="<%= request.getContextPath() %>/classroom?id=<%= classroom.classroomId %>"
                       class="button-link">
                        Open Classroom
                    </a>

                </div>

            <% } %>

        <% } else { %>

            <div class="card"
                 style="grid-column: 1 / -1; text-align: center;">

                <p class="muted">
                    You are not currently associated with any classrooms.
                </p>

            </div>

        <% } %>

    </div>

</main>

</body>
</html>
