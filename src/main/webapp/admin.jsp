<%@ page import="java.util.List" %>
<%@ page import="com.vc.servlet.AdminServlet.UserRow" %>
<%@ page import="com.vc.servlet.AdminServlet.ClassroomRow" %>
<%@ page import="com.vc.servlet.AdminServlet.AssignmentRow" %>
<%@ page import="com.vc.servlet.AdminServlet.SubmissionRow" %>
<%@ page import="com.vc.servlet.AdminServlet.OnlineClassRow" %>
<%@ page import="com.vc.servlet.AdminServlet.AuditRow" %>

<%
    String role = (String) session.getAttribute("role");

    if (!"ADMIN".equalsIgnoreCase(role)) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    Integer totalStudents = (Integer) request.getAttribute("totalStudents");
    Integer totalTeachers = (Integer) request.getAttribute("totalTeachers");
    Integer activeClassrooms = (Integer) request.getAttribute("activeClassrooms");
    Integer scheduledClasses = (Integer) request.getAttribute("scheduledClasses");

    List<UserRow> users = (List<UserRow>) request.getAttribute("users");
    List<UserRow> teachers = (List<UserRow>) request.getAttribute("teachers");
    List<UserRow> students = (List<UserRow>) request.getAttribute("students");

    List<ClassroomRow> classrooms =
            (List<ClassroomRow>) request.getAttribute("classrooms");

    List<AssignmentRow> assignments =
            (List<AssignmentRow>) request.getAttribute("assignments");

    List<SubmissionRow> submissions =
            (List<SubmissionRow>) request.getAttribute("submissions");

    List<OnlineClassRow> onlineClasses =
            (List<OnlineClassRow>) request.getAttribute("onlineClasses");

    List<AuditRow> auditLogs =
            (List<AuditRow>) request.getAttribute("auditLogs");

    if (totalStudents == null) totalStudents = 0;
    if (totalTeachers == null) totalTeachers = 0;
    if (activeClassrooms == null) activeClassrooms = 0;
    if (scheduledClasses == null) scheduledClasses = 0;

    String context = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Admin Dashboard - Virtual Classroom</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, Helvetica, sans-serif;
            background: #f4f6f9;
            color: #222;
        }

        .navbar {
            background: #1f2937;
            color: white;
            padding: 16px 28px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            position: sticky;
            top: 0;
            z-index: 100;
        }

        .navbar h1 {
            margin: 0;
            font-size: 22px;
        }

        .navbar-right {
            display: flex;
            align-items: center;
            gap: 15px;
        }

        .admin-badge {
            background: #dc2626;
            padding: 7px 12px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: bold;
        }

        .logout {
            color: white;
            text-decoration: none;
            background: #374151;
            padding: 8px 14px;
            border-radius: 6px;
        }

        .logout:hover {
            background: #4b5563;
        }

        .container {
            width: 95%;
            max-width: 1500px;
            margin: 25px auto 60px;
        }

        .page-title {
            margin-bottom: 20px;
        }

        .page-title h2 {
            margin: 0 0 5px;
            font-size: 28px;
        }

        .page-title p {
            margin: 0;
            color: #666;
        }

        .message {
            padding: 13px 16px;
            margin-bottom: 20px;
            border-radius: 7px;
            font-weight: bold;
        }

        .success {
            background: #dcfce7;
            color: #166534;
            border: 1px solid #86efac;
        }

        .error {
            background: #fee2e2;
            color: #991b1b;
            border: 1px solid #fca5a5;
        }

        .stats {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 18px;
            margin-bottom: 30px;
        }

        .stat-card {
            background: white;
            border-radius: 10px;
            padding: 22px;
            box-shadow: 0 2px 8px rgba(0,0,0,.08);
        }

        .stat-card .number {
            font-size: 32px;
            font-weight: bold;
            margin-bottom: 5px;
        }

        .stat-card .label {
            color: #666;
            font-size: 14px;
        }

        .section {
            background: white;
            border-radius: 10px;
            margin-bottom: 25px;
            box-shadow: 0 2px 8px rgba(0,0,0,.08);
            overflow: hidden;
        }

        .section-header {
            padding: 18px 20px;
            border-bottom: 1px solid #e5e7eb;
            background: #fafafa;
        }

        .section-header h3 {
            margin: 0;
            font-size: 20px;
        }

        .section-body {
            padding: 20px;
        }

        .form-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 15px;
        }

        .form-group {
            margin-bottom: 12px;
        }

        .form-group.full {
            grid-column: 1 / -1;
        }

        label {
            display: block;
            margin-bottom: 6px;
            font-weight: bold;
            font-size: 14px;
        }

        input,
        select,
        textarea {
            width: 100%;
            padding: 9px 10px;
            border: 1px solid #d1d5db;
            border-radius: 6px;
            font-size: 14px;
            background: white;
        }

        textarea {
            min-height: 80px;
            resize: vertical;
        }

        button {
            border: 0;
            border-radius: 6px;
            padding: 8px 12px;
            cursor: pointer;
            font-weight: bold;
            font-size: 13px;
        }

        .btn-primary {
            background: #2563eb;
            color: white;
        }

        .btn-primary:hover {
            background: #1d4ed8;
        }

        .btn-success {
            background: #16a34a;
            color: white;
        }

        .btn-warning {
            background: #d97706;
            color: white;
        }

        .btn-danger {
            background: #dc2626;
            color: white;
        }

        .btn-secondary {
            background: #6b7280;
            color: white;
        }

        .btn-small {
            padding: 6px 9px;
            font-size: 12px;
        }

        .table-wrapper {
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            min-width: 800px;
        }

        th,
        td {
            padding: 11px 10px;
            border-bottom: 1px solid #e5e7eb;
            text-align: left;
            vertical-align: top;
            font-size: 13px;
        }

        th {
            background: #f9fafb;
            font-weight: bold;
            white-space: nowrap;
        }

        tr:hover td {
            background: #fafafa;
        }

        .badge {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: bold;
        }

        .badge-admin {
            background: #fee2e2;
            color: #991b1b;
        }

        .badge-teacher {
            background: #dbeafe;
            color: #1e40af;
        }

        .badge-student {
            background: #dcfce7;
            color: #166534;
        }

        .badge-active {
            background: #dcfce7;
            color: #166534;
        }

        .badge-inactive {
            background: #fee2e2;
            color: #991b1b;
        }

        .actions {
            display: flex;
            flex-wrap: wrap;
            gap: 6px;
        }

        .inline-form {
            display: inline;
        }

        .edit-form {
            background: #f9fafb;
            padding: 12px;
            border-radius: 7px;
            min-width: 420px;
        }

        .edit-form .row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 8px;
            margin-bottom: 8px;
        }

        .edit-form .full {
            grid-column: 1 / -1;
        }

        .small-text {
            color: #6b7280;
            font-size: 12px;
        }

        .answer-box,
        .feedback-box {
            max-width: 350px;
            max-height: 120px;
            overflow-y: auto;
            white-space: pre-wrap;
            word-break: break-word;
            padding: 8px;
            background: #f9fafb;
            border-radius: 5px;
        }

        .empty {
            text-align: center;
            color: #777;
            padding: 25px;
        }

        .two-column {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 25px;
        }

        .danger-zone {
            border-left: 4px solid #dc2626;
        }

        .section-note {
            color: #666;
            font-size: 13px;
            margin: 5px 0 15px;
        }

        @media (max-width: 1000px) {
            .stats {
                grid-template-columns: repeat(2, 1fr);
            }

            .two-column {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 700px) {
            .stats {
                grid-template-columns: 1fr;
            }

            .form-grid {
                grid-template-columns: 1fr;
            }

            .form-group.full {
                grid-column: auto;
            }

            .navbar {
                padding: 13px 15px;
            }

            .navbar h1 {
                font-size: 18px;
            }

            .container {
                width: 94%;
            }
        }
    </style>
</head>

<body>

<nav class="navbar">
    <h1>Virtual Classroom - Admin</h1>

    <div class="navbar-right">
        <span class="admin-badge">ADMIN</span>

        <a class="logout"
           href="<%= context %>/logout">
            Logout
        </a>
    </div>
</nav>

<div class="container">

    <div class="page-title">
        <h2>Admin Dashboard</h2>
        <p>Manage users, classrooms, assignments, submissions and live classes.</p>
    </div>

    <% if (success != null && !success.isBlank()) { %>
        <div class="message success">
            <%= success %>
        </div>
    <% } %>

    <% if (error != null && !error.isBlank()) { %>
        <div class="message error">
            <%= error %>
        </div>
    <% } %>


    <!-- =========================
         STATISTICS
         ========================= -->

    <div class="stats">

        <div class="stat-card">
            <div class="number"><%= totalStudents %></div>
            <div class="label">Total Students</div>
        </div>

        <div class="stat-card">
            <div class="number"><%= totalTeachers %></div>
            <div class="label">Total Teachers</div>
        </div>

        <div class="stat-card">
            <div class="number"><%= activeClassrooms %></div>
            <div class="label">Active Classrooms</div>
        </div>

        <div class="stat-card">
            <div class="number"><%= scheduledClasses %></div>
            <div class="label">Scheduled Live Classes</div>
        </div>

    </div>


    <!-- =========================
         CREATE CLASSROOM
         ========================= -->

    <div class="section">

        <div class="section-header">
            <h3>Create Classroom / Course</h3>
        </div>

        <div class="section-body">

            <form method="post"
                  action="<%= context %>/admin">

                <input type="hidden"
                       name="action"
                       value="createClassroom">

                <div class="form-grid">

                    <div class="form-group">
                        <label>Classroom Name</label>

                        <input type="text"
                               name="className"
                               placeholder="Example: Java Programming"
                               required>
                    </div>

                    <div class="form-group">
                        <label>Subject Code / Subject</label>

                        <input type="text"
                               name="subject"
                               placeholder="Example: CS101">
                    </div>

                    <div class="form-group">
                        <label>Owner Teacher</label>

                        <select name="ownerTeacherId">

                            <option value="">
                                -- No Owner --
                            </option>

                            <% if (teachers != null) {
                                for (UserRow teacher : teachers) { %>

                                    <option value="<%= teacher.userId %>">
                                        <%= teacher.name %>
                                        -
                                        <%= teacher.email %>
                                    </option>

                            <%  }
                            } %>

                        </select>
                    </div>

                    <div class="form-group">
                        <label>Description</label>

                        <textarea name="description"
                                  placeholder="Classroom description"></textarea>
                    </div>

                </div>

                <button type="submit"
                        class="btn-primary">
                    Create Classroom
                </button>

            </form>

        </div>
    </div>


    <!-- =========================
         USER MANAGEMENT
         ========================= -->

    <div class="section">

        <div class="section-header">
            <h3>Manage Users</h3>

            <p class="section-note">
                Edit roles, assign teachers to students, activate/deactivate
                accounts, reset passwords and delete accounts.
            </p>
        </div>

        <div class="table-wrapper">

            <% if (users == null || users.isEmpty()) { %>

                <div class="empty">
                    No users found.
                </div>

            <% } else { %>

            <table>

                <thead>
                <tr>
                    <th>ID</th>
                    <th>User</th>
                    <th>Phone</th>
                    <th>Role</th>
                    <th>Status</th>
                    <th>Assigned Teacher</th>
                    <th>Actions</th>
                </tr>
                </thead>

                <tbody>

                <% for (UserRow user : users) { %>

                <tr>

                    <td>
                        <%= user.userId %>
                    </td>

                    <td>
                        <strong><%= user.name %></strong>
                        <br>
                        <span class="small-text">
                            <%= user.email %>
                        </span>
                    </td>

                    <td>
                        <%= user.phone == null ? "" : user.phone %>
                    </td>

                    <td>

                        <% if ("ADMIN".equalsIgnoreCase(user.role)) { %>

                            <span class="badge badge-admin">
                                ADMIN
                            </span>

                        <% } else if ("TEACHER".equalsIgnoreCase(user.role)) { %>

                            <span class="badge badge-teacher">
                                TEACHER
                            </span>

                        <% } else { %>

                            <span class="badge badge-student">
                                STUDENT
                            </span>

                        <% } %>

                    </td>

                    <td>

                        <% if (user.active) { %>

                            <span class="badge badge-active">
                                ACTIVE
                            </span>

                        <% } else { %>

                            <span class="badge badge-inactive">
                                INACTIVE
                            </span>

                        <% } %>

                    </td>

                    <td>
                        <%= user.teacherName == null
                                ? "-"
                                : user.teacherName %>
                    </td>

                    <td>

                        <div class="actions">

                            <!-- EDIT USER -->

                            <button type="button"
                                    class="btn-primary btn-small"
                                    onclick="toggleEdit(<%= user.userId %>)">
                                Edit
                            </button>


                            <!-- ACTIVATE / DEACTIVATE -->

                            <form method="post"
                                  action="<%= context %>/admin"
                                  class="inline-form">

                                <input type="hidden"
                                       name="action"
                                       value="toggleUser">

                                <input type="hidden"
                                       name="userId"
                                       value="<%= user.userId %>">

                                <button type="submit"
                                        class="<%= user.active
                                            ? "btn-warning"
                                            : "btn-success" %> btn-small">

                                    <%= user.active
                                            ? "Deactivate"
                                            : "Activate" %>

                                </button>

                            </form>


                            <!-- RESET PASSWORD -->

                            <button type="button"
                                    class="btn-secondary btn-small"
                                    onclick="resetPassword(<%= user.userId %>)">
                                Reset Password
                            </button>


                            <!-- DELETE -->

                            <form method="post"
                                  action="<%= context %>/admin"
                                  class="inline-form"
                                  onsubmit="return confirm('Delete this user permanently?');">

                                <input type="hidden"
                                       name="action"
                                       value="deleteUser">

                                <input type="hidden"
                                       name="userId"
                                       value="<%= user.userId %>">

                                <button type="submit"
                                        class="btn-danger btn-small">
                                    Delete
                                </button>

                            </form>

                        </div>


                        <!-- EDIT FORM -->

                        <div id="edit-<%= user.userId %>"
                             style="display:none; margin-top:12px;">

                            <form method="post"
                                  action="<%= context %>/admin"
                                  class="edit-form">

                                <input type="hidden"
                                       name="action"
                                       value="updateUser">

                                <input type="hidden"
                                       name="userId"
                                       value="<%= user.userId %>">

                                <div class="row">

                                    <div>
                                        <label>Name</label>

                                        <input type="text"
                                               name="name"
                                               value="<%= user.name == null ? "" : user.name %>"
                                               required>
                                    </div>

                                    <div>
                                        <label>Email</label>

                                        <input type="email"
                                               name="email"
                                               value="<%= user.email == null ? "" : user.email %>"
                                               required>
                                    </div>

                                </div>

                                <div class="row">

                                    <div>
                                        <label>Phone</label>

                                        <input type="text"
                                               name="phone"
                                               value="<%= user.phone == null ? "" : user.phone %>"
                                               maxlength="10">
                                    </div>

                                    <div>
                                        <label>Role</label>

                                        <select name="role"
                                                onchange="toggleTeacherSelect(<%= user.userId %>, this.value)">

                                            <option value="STUDENT"
                                                <%= "STUDENT".equalsIgnoreCase(user.role)
                                                    ? "selected" : "" %>>
                                                STUDENT
                                            </option>

                                            <option value="TEACHER"
                                                <%= "TEACHER".equalsIgnoreCase(user.role)
                                                    ? "selected" : "" %>>
                                                TEACHER
                                            </option>

                                            <option value="ADMIN"
                                                <%= "ADMIN".equalsIgnoreCase(user.role)
                                                    ? "selected" : "" %>>
                                                ADMIN
                                            </option>

                                        </select>
                                    </div>

                                </div>

                                <div id="teacher-select-<%= user.userId %>"
                                     class="form-group"
                                     style="<%= "STUDENT".equalsIgnoreCase(user.role)
                                             ? ""
                                             : "display:none;" %>">

                                    <label>Assign Teacher</label>

                                    <select name="teacherId">

                                        <option value="">
                                            -- No Teacher --
                                        </option>

                                        <% if (teachers != null) {
                                            for (UserRow teacher : teachers) { %>

                                            <option value="<%= teacher.userId %>"
                                                <%= user.teacherId != null
                                                    && user.teacherId == teacher.userId
                                                    ? "selected"
                                                    : "" %>>

                                                <%= teacher.name %>
                                                -
                                                <%= teacher.email %>

                                            </option>

                                        <%  }
                                        } %>

                                    </select>

                                </div>

                                <button type="submit"
                                        class="btn-success">
                                    Save Changes
                                </button>

                                <button type="button"
                                        class="btn-secondary"
                                        onclick="toggleEdit(<%= user.userId %>)">
                                    Cancel
                                </button>

                            </form>

                        </div>

                    </td>

                </tr>

                <% } %>

                </tbody>

            </table>

            <% } %>

        </div>

    </div>


    <!-- =========================
         CLASSROOM MANAGEMENT
         ========================= -->

    <div class="section">

        <div class="section-header">
            <h3>Manage Classrooms</h3>

            <p class="section-note">
                Edit classroom information, change the owner teacher,
                view enrollment counts or delete classrooms.
            </p>
        </div>

        <div class="table-wrapper">

            <% if (classrooms == null || classrooms.isEmpty()) { %>

                <div class="empty">
                    No classrooms found.
                </div>

            <% } else { %>

            <table>

                <thead>
                <tr>
                    <th>ID</th>
                    <th>Classroom</th>
                    <th>Subject</th>
                    <th>Owner</th>
                    <th>Students</th>
                    <th>Actions</th>
                </tr>
                </thead>

                <tbody>

                <% for (ClassroomRow classroom : classrooms) { %>

                <tr>

                    <td>
                        <%= classroom.classroomId %>
                    </td>

                    <td>
                        <strong>
                            <%= classroom.className %>
                        </strong>

                        <% if (classroom.description != null
                                && !classroom.description.isBlank()) { %>

                            <br>
                            <span class="small-text">
                                <%= classroom.description %>
                            </span>

                        <% } %>
                    </td>

                    <td>
                        <%= classroom.subject == null
                                ? ""
                                : classroom.subject %>
                    </td>

                    <td>
                        <%= classroom.ownerTeacherName == null
                                ? "Not assigned"
                                : classroom.ownerTeacherName %>
                    </td>

                    <td>
                        <strong>
                            <%= classroom.enrollmentCount %>
                        </strong>
                    </td>

                    <td>

                        <div class="actions">

                            <button type="button"
                                    class="btn-primary btn-small"
                                    onclick="toggleClassroomEdit(<%= classroom.classroomId %>)">
                                Edit
                            </button>

                            <form method="post"
                                  action="<%= context %>/admin"
                                  class="inline-form"
                                  onsubmit="return confirm('Delete this classroom? Related records may also be affected.');">

                                <input type="hidden"
                                       name="action"
                                       value="deleteClassroom">

                                <input type="hidden"
                                       name="classroomId"
                                       value="<%= classroom.classroomId %>">

                                <button type="submit"
                                        class="btn-danger btn-small">
                                    Delete
                                </button>

                            </form>

                        </div>


                        <div id="classroom-edit-<%= classroom.classroomId %>"
                             style="display:none; margin-top:12px;">

                            <form method="post"
                                  action="<%= context %>/admin"
                                  class="edit-form">

                                <input type="hidden"
                                       name="action"
                                       value="updateClassroom">

                                <input type="hidden"
                                       name="classroomId"
                                       value="<%= classroom.classroomId %>">

                                <div class="row">

                                    <div>
                                        <label>Classroom Name</label>

                                        <input type="text"
                                               name="className"
                                               value="<%= classroom.className == null ? "" : classroom.className %>"
                                               required>
                                    </div>

                                    <div>
                                        <label>Subject</label>

                                        <input type="text"
                                               name="subject"
                                               value="<%= classroom.subject == null ? "" : classroom.subject %>">
                                    </div>

                                </div>

                                <div class="form-group">

                                    <label>Owner Teacher</label>

                                    <select name="ownerTeacherId">

                                        <option value="">
                                            -- No Owner --
                                        </option>

                                        <% if (teachers != null) {
                                            for (UserRow teacher : teachers) { %>

                                            <option value="<%= teacher.userId %>"
                                                <%= classroom.ownerTeacherId != null
                                                    && classroom.ownerTeacherId == teacher.userId
                                                    ? "selected"
                                                    : "" %>>

                                                <%= teacher.name %>
                                                -
                                                <%= teacher.email %>

                                            </option>

                                        <%  }
                                        } %>

                                    </select>

                                </div>

                                <div class="form-group">

                                    <label>Description</label>

                                    <textarea name="description"><%= classroom.description == null ? "" : classroom.description %></textarea>

                                </div>

                                <button type="submit"
                                        class="btn-success">
                                    Save Classroom
                                </button>

                            </form>

                        </div>

                    </td>

                </tr>

                <% } %>

                </tbody>

            </table>

            <% } %>

        </div>

    </div>


    <!-- =========================
         MANUAL ENROLLMENT
         ========================= -->

    <div class="section">

        <div class="section-header">
            <h3>Manual Student Enrollment</h3>

            <p class="section-note">
                Admin can manually add or remove students from classrooms.
            </p>
        </div>

        <div class="section-body">

            <div class="two-column">

                <!-- ENROLL -->

                <div>

                    <h4>Enroll Student</h4>

                    <form method="post"
                          action="<%= context %>/admin">

                        <input type="hidden"
                               name="action"
                               value="enrollStudent">

                        <div class="form-group">

                            <label>Student</label>

                            <select name="studentId"
                                    required>

                                <option value="">
                                    -- Select Student --
                                </option>

                                <% if (students != null) {
                                    for (UserRow student : students) { %>

                                    <option value="<%= student.userId %>">
                                        <%= student.name %>
                                        -
                                        <%= student.email %>
                                    </option>

                                <%  }
                                } %>

                            </select>

                        </div>

                        <div class="form-group">

                            <label>Classroom</label>

                            <select name="classroomId"
                                    required>

                                <option value="">
                                    -- Select Classroom --
                                </option>

                                <% if (classrooms != null) {
                                    for (ClassroomRow classroom : classrooms) { %>

                                    <option value="<%= classroom.classroomId %>">
                                        <%= classroom.className %>
                                    </option>

                                <%  }
                                } %>

                            </select>

                        </div>

                        <button type="submit"
                                class="btn-success">
                            Enroll Student
                        </button>

                    </form>

                </div>


                <!-- REMOVE -->

                <div>

                    <h4>Remove Enrollment</h4>

                    <form method="post"
                          action="<%= context %>/admin">

                        <input type="hidden"
                               name="action"
                               value="removeEnrollment">

                        <div class="form-group">

                            <label>Student</label>

                            <select name="studentId"
                                    required>

                                <option value="">
                                    -- Select Student --
                                </option>

                                <% if (students != null) {
                                    for (UserRow student : students) { %>

                                    <option value="<%= student.userId %>">
                                        <%= student.name %>
                                        -
                                        <%= student.email %>
                                    </option>

                                <%  }
                                } %>

                            </select>

                        </div>

                        <div class="form-group">

                            <label>Classroom</label>

                            <select name="classroomId"
                                    required>

                                <option value="">
                                    -- Select Classroom --
                                </option>

                                <% if (classrooms != null) {
                                    for (ClassroomRow classroom : classrooms) { %>

                                    <option value="<%= classroom.classroomId %>">
                                        <%= classroom.className %>
                                    </option>

                                <%  }
                                } %>

                            </select>

                        </div>

                        <button type="submit"
                                class="btn-danger"
                                onclick="return confirm('Remove this student from the classroom?');">
                            Remove Enrollment
                        </button>

                    </form>

                </div>

            </div>

        </div>

    </div>


    <!-- =========================
         ASSIGNMENTS
         ========================= -->

    <div class="section">

        <div class="section-header">
            <h3>Manage Assignments</h3>

            <p class="section-note">
                Admin can view assignment information and remove assignments.
                Grading remains the responsibility of teachers.
            </p>
        </div>

        <div class="table-wrapper">

            <% if (assignments == null || assignments.isEmpty()) { %>

                <div class="empty">
                    No assignments found.
                </div>

            <% } else { %>

            <table>

                <thead>
                <tr>
                    <th>ID</th>
                    <th>Assignment</th>
                    <th>Classroom</th>
                    <th>Due Date</th>
                    <th>Submissions</th>
                    <th>Actions</th>
                </tr>
                </thead>

                <tbody>

                <% for (AssignmentRow assignment : assignments) { %>

                <tr>

                    <td>
                        <%= assignment.assignmentId %>
                    </td>

                    <td>

                        <strong>
                            <%= assignment.title %>
                        </strong>

                        <% if (assignment.description != null
                                && !assignment.description.isBlank()) { %>

                            <br>

                            <span class="small-text">
                                <%= assignment.description %>
                            </span>

                        <% } %>

                    </td>

                    <td>
                        <%= assignment.classroomTitle == null
                                ? "-"
                                : assignment.classroomTitle %>
                    </td>

                    <td>
                        <%= assignment.dueDate == null
                                ? "-"
                                : assignment.dueDate %>
                    </td>

                    <td>
                        -
                    </td>

                    <td>

                        <form method="post"
                              action="<%= context %>/admin"
                              onsubmit="return confirm('Delete this assignment?');">

                            <input type="hidden"
                                   name="action"
                                   value="deleteAssignment">

                            <input type="hidden"
                                   name="assignmentId"
                                   value="<%= assignment.assignmentId %>">

                            <button type="submit"
                                    class="btn-danger btn-small">
                                Delete
                            </button>

                        </form>

                    </td>

                </tr>

                <% } %>

                </tbody>

            </table>

            <% } %>

        </div>

    </div>


    <!-- =========================
         SUBMISSIONS
         ========================= -->

    <div class="section">

        <div class="section-header">
            <h3>View Submissions</h3>

            <p class="section-note">
                Admin can inspect answers, marks and feedback.
                Admin does not grade submissions.
            </p>
        </div>

        <div class="table-wrapper">

            <% if (submissions == null || submissions.isEmpty()) { %>

                <div class="empty">
                    No submissions found.
                </div>

            <% } else { %>

            <table>

                <thead>
                <tr>
                    <th>ID</th>
                    <th>Assignment</th>
                    <th>Student</th>
                    <th>Answer</th>
                    <th>Marks</th>
                    <th>Feedback</th>
                    <th>Submitted</th>
                    <th>Action</th>
                </tr>
                </thead>

                <tbody>

                <% for (SubmissionRow submission : submissions) { %>

                <tr>

                    <td>
                        <%= submission.submissionId %>
                    </td>

                    <td>
                        <%= submission.assignmentTitle == null
                                ? "-"
                                : submission.assignmentTitle %>
                    </td>

                    <td>
                        <strong>
                            <%= submission.studentName == null
                                    ? "-"
                                    : submission.studentName %>
                        </strong>

                        <br>

                        <span class="small-text">
                            ID: <%= submission.studentId %>
                        </span>
                    </td>

                    <td>

                        <div class="answer-box">
                            <%= submission.answer == null
                                    ? "(No answer)"
                                    : submission.answer %>
                        </div>

                    </td>

                    <td>

                        <% if (submission.marks == null) { %>

                            <span class="small-text">
                                Not graded
                            </span>

                        <% } else { %>

                            <strong>
                                <%= submission.marks %>/100
                            </strong>

                        <% } %>

                    </td>

                    <td>

                        <div class="feedback-box">

                            <%= submission.feedback == null
                                    || submission.feedback.isBlank()
                                    ? "(No feedback)"
                                    : submission.feedback %>

                        </div>

                    </td>

                    <td>
                        <%= submission.submittedAt == null
                                ? "-"
                                : submission.submittedAt %>
                    </td>

                    <td>

                        <form method="post"
                              action="<%= context %>/admin"
                              onsubmit="return confirm('Delete this submission?');">

                            <input type="hidden"
                                   name="action"
                                   value="deleteSubmission">

                            <input type="hidden"
                                   name="submissionId"
                                   value="<%= submission.submissionId %>">

                            <button type="submit"
                                    class="btn-danger btn-small">
                                Delete
                            </button>

                        </form>

                    </td>

                </tr>

                <% } %>

                </tbody>

            </table>

            <% } %>

        </div>

    </div>


    <!-- =========================
         ONLINE CLASSES
         ========================= -->

    <div class="section">

        <div class="section-header">
            <h3>Manage Online Classes</h3>

            <p class="section-note">
                View scheduled live classes and remove incorrect or cancelled
                schedules.
            </p>
        </div>

        <div class="table-wrapper">

            <% if (onlineClasses == null || onlineClasses.isEmpty()) { %>

                <div class="empty">
                    No online classes found.
                </div>

            <% } else { %>

            <table>

                <thead>
                <tr>
                    <th>ID</th>
                    <th>Classroom</th>
                    <th>Title</th>
                    <th>Meeting URL</th>
                    <th>Date / Time</th>
                    <th>Action</th>
                </tr>
                </thead>

                <tbody>

                <% for (OnlineClassRow onlineClass : onlineClasses) { %>

                <tr>

                    <td>
                        <%= onlineClass.onlineClassId %>
                    </td>

                    <td>
                        <%= onlineClass.classroomTitle == null
                                ? "-"
                                : onlineClass.classroomTitle %>
                    </td>

                    <td>
                        <%= onlineClass.topic == null
                                ? "-"
                                : onlineClass.topic %>
                    </td>

                    <td>

                        <% if (onlineClass.meetingLink != null
                                && !onlineClass.meetingLink.isBlank()) { %>

                            <a href="<%= onlineClass.meetingLink %>"
                               target="_blank"
                               rel="noopener noreferrer">
                                Open Meeting
                            </a>

                        <% } else { %>

                            -

                        <% } %>

                    </td>

                    <td>
                        <%= onlineClass.classDate == null
                                ? "-"
                                : onlineClass.classDate %>
                    </td>

                    <td>

                        <form method="post"
                              action="<%= context %>/admin"
                              onsubmit="return confirm('Delete this online class?');">

                            <input type="hidden"
                                   name="action"
                                   value="deleteOnlineClass">

                            <input type="hidden"
                                   name="onlineClassId"
                                   value="<%= onlineClass.onlineClassId %>">

                            <button type="submit"
                                    class="btn-danger btn-small">
                                Delete
                            </button>

                        </form>

                    </td>

                </tr>

                <% } %>

                </tbody>

            </table>

            <% } %>

        </div>

    </div>


    <!-- =========================
         AUDIT LOGS
         ========================= -->

    <div class="section">

        <div class="section-header">
            <h3>Activity & Audit Logs</h3>

            <p class="section-note">
                The latest 200 system activity records are displayed.
            </p>
        </div>

        <div class="table-wrapper">

            <% if (auditLogs == null || auditLogs.isEmpty()) { %>

                <div class="empty">
                    No audit records found.
                </div>

            <% } else { %>

            <table>

                <thead>
                <tr>
                    <th>ID</th>
                    <th>Actor</th>
                    <th>Target</th>
                    <th>Target ID</th>
                    <th>Details</th>
                    <th>Date / Time</th>
                </tr>
                </thead>

                <tbody>

                <% for (AuditRow audit : auditLogs) { %>

                <tr>

                    <td>
                        <%= audit.auditId %>
                    </td>

                    <td>
                        <%= audit.userName == null
                                ? "System"
                                : audit.userName %>
                    </td>

                    <td>
                        <%= audit.entityType == null
                                ? "-"
                                : audit.entityType %>
                    </td>

                    <td>
                        <%= audit.entityId == null
                                ? "-"
                                : audit.entityId %>
                    </td>

                    <td>
                        <%= audit.description == null
                                ? ""
                                : audit.description %>
                    </td>

                    <td>
                        <%= audit.createdAt == null
                                ? "-"
                                : audit.createdAt %>
                    </td>

                </tr>

                <% } %>

                </tbody>

            </table>

            <% } %>

        </div>

    </div>

</div>


<!-- =========================
     JAVASCRIPT
     ========================= -->

<script>

    function toggleEdit(userId) {

        const element =
            document.getElementById("edit-" + userId);

        if (!element) {
            return;
        }

        if (element.style.display === "none"
                || element.style.display === "") {

            element.style.display = "block";

        } else {

            element.style.display = "none";

        }
    }


    function toggleClassroomEdit(classroomId) {

        const element =
            document.getElementById(
                "classroom-edit-" + classroomId
            );

        if (!element) {
            return;
        }

        if (element.style.display === "none"
                || element.style.display === "") {

            element.style.display = "block";

        } else {

            element.style.display = "none";

        }
    }


    function toggleTeacherSelect(userId, role) {

        const element =
            document.getElementById(
                "teacher-select-" + userId
            );

        if (!element) {
            return;
        }

        if (role === "STUDENT") {
            element.style.display = "block";
        } else {
            element.style.display = "none";
        }
    }


    function resetPassword(userId) {

        const password =
            prompt(
                "Enter the new password (minimum 6 characters):"
            );

        if (password === null) {
            return;
        }

        if (password.length < 6) {

            alert(
                "Password must contain at least 6 characters."
            );

            return;
        }

        const form =
            document.createElement("form");

        form.method = "POST";
        form.action = "<%= context %>/admin";

        const action =
            document.createElement("input");

        action.type = "hidden";
        action.name = "action";
        action.value = "resetPassword";

        const id =
            document.createElement("input");

        id.type = "hidden";
        id.name = "userId";
        id.value = userId;

        const newPassword =
            document.createElement("input");

        newPassword.type = "hidden";
        newPassword.name = "newPassword";
        newPassword.value = password;

        form.appendChild(action);
        form.appendChild(id);
        form.appendChild(newPassword);

        document.body.appendChild(form);

        form.submit();
    }

</script>

</body>
</html>