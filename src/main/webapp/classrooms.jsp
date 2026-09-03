<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.vc.servlet.ClassroomManagementServlet" %>

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

    List<?> classrooms =
            (List<?>) request.getAttribute("classrooms");

    String pageTitle =
            (String) request.getAttribute("pageTitle");

    if (pageTitle == null || pageTitle.trim().isEmpty()) {
        pageTitle = "My Classrooms";
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title><%= pageTitle %> | Virtual Classroom</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">

    <style>
        .classroom-actions {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            margin-top: 18px;
        }

        .classroom-count {
            color: #5d7187;
            margin-top: 10px;
        }

        .empty-classrooms {
            grid-column: 1 / -1;
            text-align: center;
        }
    </style>
</head>

<body>

<nav>
    <span>Virtual Classroom</span>

    <div class="nav-links">
        <a href="<%= request.getContextPath() %>/dashboard">
            Dashboard
        </a>

        <a href="<%= request.getContextPath() %>/logout">
            Logout
        </a>
    </div>
</nav>

<main>

    <h1><%= pageTitle %></h1>

    <% if ("TEACHER".equalsIgnoreCase(role)) { %>

        <p class="muted">
            Open a classroom to manage students, assignments and online classes.
        </p>

    <% } else { %>

        <p class="muted">
            These are the classrooms associated with your student account.
        </p>

    <% } %>

    <div class="grid">

        <% if (classrooms == null || classrooms.isEmpty()) { %>

            <div class="card empty-classrooms">

                <h2>No classrooms found</h2>

                <% if ("TEACHER".equalsIgnoreCase(role)) { %>

                    <p class="muted">
                        You are not assigned to any classroom yet.
                    </p>

                <% } else { %>

                    <p class="muted">
                        You are not enrolled in any classroom yet.
                    </p>

                <% } %>

                <p>
                    <a class="button-link"
                       href="<%= request.getContextPath() %>/dashboard">
                        Back to Dashboard
                    </a>
                </p>

            </div>

        <% } else { %>

            <% for (Object obj : classrooms) {

                ClassroomManagementServlet.ClassroomRow classroom =
                        (ClassroomManagementServlet.ClassroomRow) obj;
            %>

                <div class="card online-card">

                    <h2>
                        <%= classroom.className %>
                    </h2>

                    <p>
                        <b>Subject:</b>
                        <%= classroom.subject %>
                    </p>

                    <p>
                        <b>Description:</b>
                        <%= classroom.description == null ||
                           classroom.description.trim().isEmpty()
                                ? "No description available."
                                : classroom.description %>
                    </p>

                    <% if ("TEACHER".equalsIgnoreCase(role)) { %>

                        <p class="classroom-count">
                            <b>Students enrolled:</b>
                            <%= classroom.studentCount %>
                        </p>

                    <% } %>

                    <div class="classroom-actions">

                        <a class="button-link"
                           href="<%= request.getContextPath() %>/classroom?id=<%= classroom.classroomId %>">
                            Open Classroom
                        </a>

                    </div>

                </div>

            <% } %>

        <% } %>

    </div>

</main>

</body>
</html>
