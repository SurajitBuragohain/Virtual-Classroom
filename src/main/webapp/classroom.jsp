<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.vc.servlet.ClassroomServlet.OnlineClass" %>

<%!
    private String safe(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>

<%
    String role = (String) session.getAttribute("role");
    String userName = (String) session.getAttribute("name");

    String[] classroom =
            (String[]) request.getAttribute("classroom");

    List<String[]> teachers =
            (List<String[]>) request.getAttribute("teachers");

    List<String[]> allTeachers =
            (List<String[]>) request.getAttribute("allTeachers");

    List<String[]> students =
            (List<String[]>) request.getAttribute("students");

    List<String[]> assignments =
            (List<String[]>) request.getAttribute("assignments");

    List<String[]> submissionStatus =
            (List<String[]>) request.getAttribute("submissionStatus");

    List<String[]> studentSubmissions =
            (List<String[]>) request.getAttribute("studentSubmissions");

    List<OnlineClass> onlineClasses =
            (List<OnlineClass>) request.getAttribute("onlineClasses");

    Integer classroomId =
            (Integer) request.getAttribute("cid");

    String message =
            request.getParameter("msg");
%>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Classroom | Virtual Classroom</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css?v=20260827">

    <style>

        nav {
            width: 100%;
            background: #204a87;
            color: #ffffff;
            padding: 16px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-sizing: border-box;
        }

        .nav-title {
            font-size: 22px;
            font-weight: bold;
        }

        nav a {
            color: #ffffff;
            text-decoration: none;
        }

        nav a:hover {
            text-decoration: underline;
        }

        main {
            width: 90%;
            max-width: 1100px;
            margin: 30px auto;
        }

        .classroom-header,
        .card {
            background: #ffffff;
            border-radius: 12px;
            padding: 25px;
            margin-bottom: 25px;
            box-shadow: 0 4px 18px rgba(0, 0, 0, 0.10);
        }

        .classroom-header h1,
        .card h2,
        .card h3 {
            color: #14213d;
        }

        .classroom-header h1 {
            margin-top: 0;
        }

        .card h2 {
            margin-top: 0;
        }

        .card label {
            display: block;
            margin: 12px 0 6px;
            font-weight: bold;
        }

        .card input[type="text"],
        .card input[type="date"],
        .card input[type="time"],
        .card input[type="url"],
        .card input[type="number"],
        .card select,
        .card textarea {
            width: 100%;
            padding: 12px;
            margin-bottom: 14px;
            border: 1px solid #cccccc;
            border-radius: 8px;
            font-size: 16px;
            font-family: Arial, sans-serif;
            box-sizing: border-box;
        }

        .card textarea {
            resize: vertical;
        }

        .card input:focus,
        .card select:focus,
        .card textarea:focus {
            outline: none;
            border-color: #204a87;
            box-shadow: 0 0 0 2px rgba(32, 74, 135, 0.10);
        }

        .card button,
        .button {
            display: inline-block;
            padding: 12px 18px;
            border: none;
            border-radius: 8px;
            background: #204a87;
            color: #ffffff;
            font-size: 16px;
            font-weight: bold;
            text-decoration: none;
            cursor: pointer;
        }

        .card button:hover,
        .button:hover {
            background: #16396a;
        }

        .danger {
            background: #b71c1c !important;
        }

        .danger:hover {
            background: #8e1515 !important;
        }

        .online-class-card,
        .assignment-card,
        .submission-card {
            background: #f8fafc;
            border: 1px solid #d9e1ec;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 18px;
        }

        .student-answer {
            background: #ffffff;
            border: 1px solid #d9e1ec;
            border-radius: 8px;
            padding: 15px;
            margin: 10px 0 20px;
            white-space: pre-wrap;
            line-height: 1.6;
            color: #222222;
        }

        .feedback-box {
            background: #eef4ff;
            border-left: 4px solid #204a87;
            padding: 12px;
            margin: 10px 0 20px;
            border-radius: 5px;
        }

        .submitted {
            color: #008a35;
            font-weight: bold;
        }

        .not-submitted {
            color: #b71c1c;
            font-weight: bold;
        }

        .graded {
            color: #008a35;
            font-weight: bold;
        }

        .not-graded {
            color: #b26a00;
            font-weight: bold;
        }

        .success-message {
            background: #d9f7e5;
            color: #087a35;
            border-radius: 8px;
            padding: 14px;
            margin-bottom: 20px;
            font-weight: bold;
        }

        .empty {
            color: #666666;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        table th,
        table td {
            border: 1px solid #dddddd;
            padding: 12px;
            text-align: left;
            vertical-align: top;
        }

        table th {
            background: #204a87;
            color: #ffffff;
        }

        table tr:nth-child(even) {
            background: #f8fafc;
        }

        ul {
            padding-left: 20px;
        }

        li {
            margin-bottom: 10px;
        }

        @media (max-width: 700px) {

            nav {
                padding: 14px;
                flex-direction: column;
                gap: 10px;
                align-items: flex-start;
            }

            main {
                width: calc(100% - 20px);
                margin: 15px auto;
            }

            .card,
            .classroom-header {
                padding: 18px;
            }

            table {
                min-width: 750px;
            }

        }

    </style>

</head>

<body>

<nav>

    <div class="nav-title">
        Virtual Classroom
    </div>

    <div>

        <span>
            Welcome,
            <%= safe(userName == null ? "User" : userName) %>
        </span>

        &nbsp;&nbsp;

        <a href="${pageContext.request.contextPath}/dashboard">
            Dashboard
        </a>

        &nbsp;&nbsp;

        <a href="${pageContext.request.contextPath}/logout">
            Logout
        </a>

    </div>

</nav>

<main>

<%
    if (classroom == null) {
%>

    <div class="card">

        <h2>
            Classroom not found.
        </h2>

        <a class="button"
           href="${pageContext.request.contextPath}/dashboard">
            Back to Dashboard
        </a>

    </div>

<%
    } else {
%>

    <% if (message != null && !message.isBlank()) { %>

        <div class="success-message">
            <%= safe(message) %>
        </div>

    <% } %>


    <!-- CLASSROOM -->

    <div class="classroom-header">

        <h1>
            <%= safe(classroom[1]) %>
        </h1>

        <p>
            <strong>Subject:</strong>
            <%= safe(classroom[2]) %>
        </p>

        <% if (classroom.length > 3 &&
               classroom[3] != null &&
               !classroom[3].isBlank()) { %>

            <p>
                <strong>Description:</strong>
                <%= safe(classroom[3]) %>
            </p>

        <% } %>

    </div>


    <!-- TEACHERS -->

    <div class="card">

        <h2>Teachers</h2>

        <% if (teachers == null || teachers.isEmpty()) { %>

            <p class="empty">
                No teachers assigned to this classroom.
            </p>

        <% } else { %>

            <ul>

                <% for (String[] teacher : teachers) { %>

                    <li>

                        <strong>
                            <%= safe(teacher[1]) %>
                        </strong>

                        <% if (teacher.length > 2 &&
                               teacher[2] != null &&
                               !teacher[2].isBlank()) { %>

                            -
                            <%= safe(teacher[2]) %>

                        <% } %>

                    </li>

                <% } %>

            </ul>

        <% } %>

    </div>


    <!-- ADD TEACHER -->

    <% if ("TEACHER".equals(role)) { %>

        <div class="card">

            <h2>Add Teacher</h2>

            <% if (allTeachers != null &&
                   !allTeachers.isEmpty()) { %>

                <form
                    action="${pageContext.request.contextPath}/action"
                    method="post">

                    <input
                        type="hidden"
                        name="action"
                        value="addTeacher">

                    <input
                        type="hidden"
                        name="classroomId"
                        value="<%= classroomId %>">

                    <label>
                        Select Teacher
                    </label>

                    <select
                        name="teacherId"
                        required>

                        <option value="">
                            -- Select Teacher --
                        </option>

                        <% for (String[] teacher : allTeachers) { %>

                            <option value="<%= safe(teacher[0]) %>">
                                <%= safe(teacher[1]) %>
                            </option>

                        <% } %>

                    </select>

                    <button type="submit">
                        Add Teacher
                    </button>

                </form>

            <% } else { %>

                <p class="empty">
                    No teachers available.
                </p>

            <% } %>

        </div>

    <% } %>


    <!-- STUDENTS -->

    <div class="card">

        <h2>Students</h2>

        <% if (students == null || students.isEmpty()) { %>

            <p class="empty">
                No students enrolled in this classroom yet.
            </p>

        <% } else { %>

            <ul>

                <% for (String[] student : students) { %>

                    <li>

                        <strong>
                            <%= safe(student[1]) %>
                        </strong>

                        <% if (student.length > 2 &&
                               student[2] != null &&
                               !student[2].isBlank()) { %>

                            -
                            <%= safe(student[2]) %>

                        <% } %>

                    </li>

                <% } %>

            </ul>

        <% } %>

    </div>


    <!-- CREATE ASSIGNMENT -->

    <% if ("TEACHER".equals(role)) { %>

        <div class="card">

            <h2>Create Assignment</h2>

            <form
                action="${pageContext.request.contextPath}/action"
                method="post">

                <input
                    type="hidden"
                    name="action"
                    value="assignment">

                <input
                    type="hidden"
                    name="classroomId"
                    value="<%= classroomId %>">

                <label for="title">
                    Title
                </label>

                <input
                    type="text"
                    id="title"
                    name="title"
                    placeholder="Assignment title"
                    maxlength="150"
                    required>

                <label for="description">
                    Description
                </label>

                <textarea
                    id="description"
                    name="description"
                    rows="5"
                    placeholder="Assignment description"
                    required></textarea>

                <label for="dueDate">
                    Due Date
                </label>

                <input
                    type="date"
                    id="dueDate"
                    name="dueDate"
                    required>

                <button type="submit">
                    Create Assignment
                </button>

            </form>

        </div>

    <% } %>


    <!-- ONLINE CLASS -->

    <% if ("TEACHER".equals(role)) { %>

        <div class="card">

            <h2>Schedule Online Class</h2>

            <form
                action="${pageContext.request.contextPath}/online-class"
                method="post">

                <input
                    type="hidden"
                    name="action"
                    value="create">

                <input
                    type="hidden"
                    name="classroomId"
                    value="<%= classroomId %>">

                <label for="topic">
                    Class Topic
                </label>

                <input
                    type="text"
                    id="topic"
                    name="topic"
                    placeholder="Example: Introduction to Java"
                    maxlength="200"
                    required>

                <label for="classDate">
                    Class Date
                </label>

                <input
                    type="date"
                    id="classDate"
                    name="classDate"
                    required>

                <label for="startTime">
                    Start Time
                </label>

                <input
                    type="time"
                    id="startTime"
                    name="startTime"
                    required>

                <label for="endTime">
                    End Time
                </label>

                <input
                    type="time"
                    id="endTime"
                    name="endTime">

                <label for="meetingLink">
                    Meeting Link
                </label>

                <input
                    type="url"
                    id="meetingLink"
                    name="meetingLink"
                    placeholder="https://meet.google.com/..."
                    maxlength="500"
                    required>

                <button type="submit">
                    Schedule Online Class
                </button>

            </form>

        </div>

    <% } %>


    <!-- ONLINE CLASSES -->

    <div class="card">

        <h2>Online Classes</h2>

        <% if (onlineClasses == null ||
               onlineClasses.isEmpty()) { %>

            <p class="empty">
                No online classes scheduled for this classroom.
            </p>

        <% } else { %>

            <% for (OnlineClass onlineClass : onlineClasses) { %>

                <div class="online-class-card">

                    <h3>
                        <%= safe(onlineClass.topic) %>
                    </h3>

                    <p>
                        <strong>Teacher:</strong>
                        <%= safe(onlineClass.teacherName) %>
                    </p>

                    <p>
                        <strong>Date:</strong>
                        <%= safe(onlineClass.date) %>
                    </p>

                    <p>
                        <strong>Time:</strong>
                        <%= safe(onlineClass.startTime) %>

                        <% if (onlineClass.endTime != null &&
                               !onlineClass.endTime.isBlank()) { %>

                            -
                            <%= safe(onlineClass.endTime) %>

                        <% } %>
                    </p>

                    <p>

                        <a
                            href="<%= safe(onlineClass.meetingLink) %>"
                            target="_blank"
                            rel="noopener noreferrer"
                            class="button">

                            Join Online Class

                        </a>

                    </p>


                    <% if ("TEACHER".equals(role)) { %>

                        <form
                            action="${pageContext.request.contextPath}/online-class"
                            method="post">

                            <input
                                type="hidden"
                                name="action"
                                value="delete">

                            <input
                                type="hidden"
                                name="onlineClassId"
                                value="<%= onlineClass.id %>">

                            <input
                                type="hidden"
                                name="classroomId"
                                value="<%= classroomId %>">

                            <button
                                type="submit"
                                class="danger"
                                onclick="return confirm('Are you sure you want to delete this online class?');">

                                Delete Online Class

                            </button>

                        </form>

                    <% } %>

                </div>

            <% } %>

        <% } %>

    </div>


    <!-- ASSIGNMENTS -->

    <div class="card">

        <h2>Assignments</h2>

        <% if (assignments == null ||
               assignments.isEmpty()) { %>

            <p class="empty">
                No assignments available.
            </p>

        <% } else { %>

            <% for (String[] assignment : assignments) { %>

                <div class="assignment-card">

                    <h3>
                        <%= safe(assignment[1]) %>
                    </h3>

                    <p>
                        <strong>Description:</strong>
                        <%= assignment[2] != null
                                ? safe(assignment[2])
                                : "No description" %>
                    </p>

                    <p>
                        <strong>Due Date:</strong>
                        <%= safe(assignment[3]) %>
                    </p>


                    <!-- STUDENT SUBMISSION / RESULT -->

                    <% if ("STUDENT".equals(role)) { %>

                        <%
                            String currentAssignmentId = assignment[0];

                            String existingAnswer = "";
                            String existingSubmittedAt = "";
                            String existingMarks = "";
                            String existingFeedback = "";

                            if (studentSubmissions != null) {

                                for (String[] studentSubmission :
                                        studentSubmissions) {

                                    if (studentSubmission != null &&
                                        studentSubmission.length >= 5 &&
                                        currentAssignmentId.equals(
                                                studentSubmission[0])) {

                                        existingAnswer =
                                                studentSubmission[1];

                                        existingSubmittedAt =
                                                studentSubmission[2];

                                        existingMarks =
                                                studentSubmission[3];

                                        existingFeedback =
                                                studentSubmission[4];

                                        break;
                                    }
                                }
                            }

                            boolean hasSubmission =
                                    existingSubmittedAt != null &&
                                    !existingSubmittedAt.isBlank();
                        %>

                        <% if (!hasSubmission) { %>

                            <form
                                action="${pageContext.request.contextPath}/action"
                                method="post">

                                <input
                                    type="hidden"
                                    name="action"
                                    value="submit">

                                <input
                                    type="hidden"
                                    name="assignmentId"
                                    value="<%= safe(assignment[0]) %>">

                                <input
                                    type="hidden"
                                    name="classroomId"
                                    value="<%= classroomId %>">

                                <label>
                                    Your Answer
                                </label>

                                <textarea
                                    name="answer"
                                    rows="6"
                                    placeholder="Write your answer here..."
                                    required></textarea>

                                <button type="submit">
                                    Submit Assignment
                                </button>

                            </form>

                        <% } else { %>

                            <div class="submission-card">

                                <h3>
                                    Your Submission
                                </h3>

                                <p>
                                    <strong>Submitted:</strong>
                                    <span class="submitted">
                                        <%= safe(existingSubmittedAt) %>
                                    </span>
                                </p>

                                <p>
                                    <strong>Your Answer:</strong>
                                </p>

                                <div class="student-answer">
                                    <%= safe(existingAnswer) %>
                                </div>

                                <p>
                                    <strong>Marks:</strong>

                                    <% if (existingMarks == null ||
                                           existingMarks.isBlank()) { %>

                                        <span class="not-graded">
                                            Not Graded Yet
                                        </span>

                                    <% } else { %>

                                        <span class="graded">
                                            <%= safe(existingMarks) %> / 100
                                        </span>

                                    <% } %>
                                </p>

                                <div class="feedback-box">

                                    <strong>
                                        Teacher Feedback
                                    </strong>

                                    <% if (existingFeedback == null ||
                                           existingFeedback.isBlank()) { %>

                                        <p>
                                            Your teacher has not provided
                                            feedback yet.
                                        </p>

                                    <% } else { %>

                                        <p>
                                            <%= safe(existingFeedback) %>
                                        </p>

                                    <% } %>

                                </div>

                                <form
                                    action="${pageContext.request.contextPath}/action"
                                    method="post">

                                    <input
                                        type="hidden"
                                        name="action"
                                        value="submit">

                                    <input
                                        type="hidden"
                                        name="assignmentId"
                                        value="<%= safe(assignment[0]) %>">

                                    <input
                                        type="hidden"
                                        name="classroomId"
                                        value="<%= classroomId %>">

                                    <label>
                                        Update Your Answer
                                    </label>

                                    <textarea
                                        name="answer"
                                        rows="6"
                                        required><%= safe(existingAnswer) %></textarea>

                                    <button type="submit">
                                        Resubmit Assignment
                                    </button>

                                </form>

                            </div>

                        <% } %>

                    <% } %>

                </div>

            <% } %>

        <% } %>

    </div>


    <!-- TEACHER SUBMISSIONS -->

    <% if ("TEACHER".equals(role)) { %>

        <div class="card">

            <h2>Student Submissions</h2>

            <% if (submissionStatus == null ||
                   submissionStatus.isEmpty()) { %>

                <p class="empty">
                    No student submission data available yet.
                </p>

            <% } else { %>

                <% for (String[] status : submissionStatus) {

                    String assignmentId = status[0];
                    String assignmentTitle = status[1];
                    String studentName = status[2];
                    String submittedAt = status[3];
                    String answer = status[4];
                    String marks = status[5];
                    String feedback = status[6];
                    String studentId = status[7];
                %>

                    <div class="submission-card">

                        <h3>
                            <%= safe(assignmentTitle) %>
                        </h3>

                        <p>
                            <strong>Student:</strong>
                            <%= safe(studentName) %>
                        </p>

                        <p>
                            <strong>Submitted:</strong>

                            <% if (submittedAt == null ||
                                   submittedAt.isBlank()) { %>

                                <span class="not-submitted">
                                    Not Submitted
                                </span>

                            <% } else { %>

                                <span class="submitted">
                                    <%= safe(submittedAt) %>
                                </span>

                            <% } %>

                        </p>


                        <!-- ANSWER -->

                        <p>
                            <strong>Student Answer:</strong>
                        </p>

                        <div class="student-answer">

                            <% if (answer == null ||
                                   answer.isBlank()) { %>

                                <span class="not-submitted">
                                    Student has not submitted an answer.
                                </span>

                            <% } else { %>

                                <%= safe(answer) %>

                            <% } %>

                        </div>


                        <!-- CURRENT MARKS -->

                        <p>

                            <strong>
                                Current Marks:
                            </strong>

                            <% if (marks == null ||
                                   marks.isBlank()) { %>

                                <span class="not-graded">
                                    Not Graded
                                </span>

                            <% } else { %>

                                <span class="graded">
                                    <%= safe(marks) %> / 100
                                </span>

                            <% } %>

                        </p>


                        <!-- CURRENT FEEDBACK -->

                        <% if (feedback != null &&
                               !feedback.isBlank()) { %>

                            <div class="feedback-box">

                                <strong>
                                    Current Feedback:
                                </strong>

                                <p>
                                    <%= safe(feedback) %>
                                </p>

                            </div>

                        <% } %>


                        <!-- GRADING FORM -->

                        <% if (submittedAt != null &&
                               !submittedAt.isBlank()) { %>

                            <form
                                action="${pageContext.request.contextPath}/action"
                                method="post">

                                <input
                                    type="hidden"
                                    name="action"
                                    value="gradeSubmission">

                                <input
                                    type="hidden"
                                    name="assignmentId"
                                    value="<%= assignmentId %>">

                                <input
                                    type="hidden"
                                    name="studentId"
                                    value="<%= studentId %>">

                                <input
                                    type="hidden"
                                    name="classroomId"
                                    value="<%= classroomId %>">


                                <label
                                    for="marks_<%= assignmentId %>_<%= studentId %>">

                                    Marks (0 - 100)

                                </label>

                                <input
                                    type="number"
                                    id="marks_<%= assignmentId %>_<%= studentId %>"
                                    name="marks"
                                    min="0"
                                    max="100"
                                    value="<%= marks != null ? safe(marks) : "" %>"
                                    required>


                                <label
                                    for="feedback_<%= assignmentId %>_<%= studentId %>">

                                    Feedback

                                </label>

                                <textarea
                                    id="feedback_<%= assignmentId %>_<%= studentId %>"
                                    name="feedback"
                                    rows="5"
                                    placeholder="Write feedback for the student..."><%= feedback != null ? safe(feedback) : "" %></textarea>


                                <button type="submit">
                                    Save Marks &amp; Feedback
                                </button>

                            </form>

                        <% } %>

                    </div>

                <% } %>

            <% } %>

        </div>

    <% } %>

<%
    }
%>

</main>

</body>

</html>