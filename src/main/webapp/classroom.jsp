<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalTime" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.lang.reflect.Field" %>
<%@ page import="java.lang.reflect.Method" %>

<%!
private String getValue(Object obj, String name) {
    if (obj == null) return "";

    try {
        String methodName =
                "get"
                + Character.toUpperCase(name.charAt(0))
                + name.substring(1);

        Method method =
                obj.getClass().getMethod(methodName);

        Object value =
                method.invoke(obj);

        return value == null
                ? ""
                : String.valueOf(value);

    } catch (Exception ignored) {
    }

    try {
        Field field =
                obj.getClass().getDeclaredField(name);

        field.setAccessible(true);

        Object value =
                field.get(obj);

        return value == null
                ? ""
                : String.valueOf(value);

    } catch (Exception ignored) {
    }

    return "";
}

private String safe(Object value) {
    if (value == null) return "";

    return String.valueOf(value)
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
}
%>

<!DOCTYPE html>
<html lang="en">

<head>

<meta charset="UTF-8">

<meta name="viewport"
      content="width=device-width,initial-scale=1.0">

<title>Virtual Classroom</title>

<link rel="stylesheet"
      href="css/style.css">

<style>

body {
    margin: 0;
    font-family: Arial, sans-serif;
    background: #f3f6fb;
    color: #14213d;
}

.container {
    width: 90%;
    max-width: 1200px;
    margin: 30px auto;
}

h1 {
    font-size: 36px;
    margin-bottom: 25px;
}

h2 {
    margin-top: 0;
}

h3 {
    margin-top: 0;
}

.card {
    background: #fff;
    padding: 25px;
    margin-bottom: 25px;
    border-radius: 14px;
    box-shadow: 0 4px 15px rgba(0,0,0,.08);
}

.teacher-item,
.student-item {
    padding: 12px;
    border-bottom: 1px solid #eee;
}

.online-class-card {
    background: #f8faff;
    border-left: 5px solid #204a87;
    padding: 20px;
    margin-top: 15px;
    border-radius: 8px;
}

input,
textarea,
select {
    width: 100%;
    box-sizing: border-box;
    padding: 12px;
    margin-top: 7px;
    margin-bottom: 15px;
    border: 1px solid #ccc;
    border-radius: 7px;
    font-size: 15px;
}

textarea {
    min-height: 100px;
    resize: vertical;
}

input[type="file"] {
    display: none;
}

.choose-file-button {
    display: inline-block;
    background: #204a87;
    color: #fff;
    padding: 10px 16px;
    border-radius: 7px;
    cursor: pointer;
    font-weight: bold;
    font-size: 14px;
    margin-top: 7px;
}

.choose-file-button:hover {
    background: #16396a;
}

.selected-file-name {
    display: inline-block;
    margin-left: 12px;
    color: #555;
    font-size: 14px;
    vertical-align: middle;
    max-width: 55%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.pdf-upload-hint {
    display: block;
    margin-top: 8px;
    color: #666;
    font-size: 13px;
}

.deadline-closed {
    background: #fff3f3;
    border: 1px solid #efb5b5;
    border-left: 5px solid #c62828;
    padding: 16px;
    margin-top: 18px;
    border-radius: 8px;
    color: #8b1e1e;
}

.deadline-closed h4 {
    margin: 0 0 7px 0;
    color: #b71c1c;
}

.deadline-closed p {
    margin: 0;
    line-height: 1.5;
}

.pdf-view-button {
    display: inline-block;
    padding: 7px 12px;
    background: #204a87;
    color: #ffffff;
    text-decoration: none;
    border-radius: 6px;
    font-weight: bold;
    font-size: 13px;
}

.pdf-view-button:hover {
    background: #16396a;
}

.pdf-file-name {
    margin-top: 6px;
    max-width: 180px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.question-card {
    background: #f8faff;
    border-left: 5px solid #204a87;
    padding: 18px;
    margin-top: 15px;
    border-radius: 8px;
}

.question-card h3 {
    margin-bottom: 8px;
}

.question-text {
    background: #fff;
    border: 1px solid #ddd;
    border-radius: 7px;
    padding: 14px;
    margin: 10px 0 15px;
    white-space: pre-wrap;
    word-break: break-word;
}

.answer-box {
    background: #eef6ff;
    border-radius: 7px;
    padding: 14px;
    margin-top: 12px;
}

.unanswered {
    color: #a33;
    font-weight: bold;
}

.answered {
    color: #256029;
    font-weight: bold;
}

label {
    font-weight: bold;
}

button,
.button {
    display: inline-block;
    background: #204a87;
    color: #fff;
    border: none;
    padding: 11px 18px;
    border-radius: 7px;
    cursor: pointer;
    text-decoration: none;
    font-weight: bold;
    margin-right: 8px;
}

button:hover,
.button:hover {
    background: #16396a;
}

.danger {
    background: #c62828;
}

.danger:hover {
    background: #a51f1f;
}

.empty {
    color: #666;
}

.message {
    background: #e8f5e9;
    color: #256029;
    padding: 12px;
    border-radius: 7px;
    margin-bottom: 20px;
}

.error {
    background: #ffebee;
    color: #b71c1c;
    padding: 12px;
    border-radius: 7px;
    margin-bottom: 20px;
}

.small {
    color: #666;
    font-size: 14px;
}

.pdf-note {
    background: #f1f5f9;
    border-left: 4px solid #204a87;
    padding: 12px;
    margin-bottom: 15px;
    border-radius: 6px;
    font-size: 14px;
}

.file-label {
    display: block;
    margin-top: 10px;
}

.pdf-submission {
    background: #eef6ff;
    border: 1px solid #c9ddf5;
    border-left: 5px solid #204a87;
    padding: 18px;
    margin-top: 18px;
    border-radius: 8px;
}

.pdf-submission span {
    display: inline-block;
    margin-left: 8px;
    font-weight: normal;
    word-break: break-word;
}

.pdf-actions {
    margin-top: 15px;
}

.pdf-actions .button {
    margin-bottom: 8px;
}

/* =========================================================
   STUDENT HELP / ASK QUESTION
   ========================================================= */

.help-card {
    background: #ffffff;
    border-left: 5px solid #204a87;
}

.help-card h2 {
    margin-bottom: 10px;
}

.help-card p {
    color: #555;
    line-height: 1.6;
}

.help-card textarea {
    min-height: 130px;
    resize: vertical;
}

.help-card button {
    margin-top: 5px;
}

table {
    width: 100%;
    border-collapse: collapse;
}

th,
td {
    border: 1px solid #ddd;
    padding: 10px;
    text-align: left;
}

th {
    background: #f1f5f9;
}


.grade-box {
    min-width: 240px;
}

.grade-box input[type="number"] {
    width: 110px;
    margin-bottom: 10px;
}

.grade-box textarea {
    width: 100%;
    min-height: 80px;
    resize: vertical;
    box-sizing: border-box;
    margin-bottom: 10px;
}

.grade-box .grade-label {
    display: block;
    margin-top: 6px;
    margin-bottom: 5px;
    font-weight: bold;
}

.grade-box .save-grade-button {
    background: #204a87;
    color: #fff;
    border: none;
    border-radius: 7px;
    padding: 9px 15px;
    cursor: pointer;
    font-weight: bold;
}

.grade-box .save-grade-button:hover {
    background: #16396a;
}

.grade-current {
    margin-bottom: 10px;
    font-weight: bold;
}

</style>

</head>

<body>

<div class="container">

<%

String role =
        (String) session.getAttribute("role");

String userName =
        (String) session.getAttribute("name");

String[] classroom =
        (String[]) request.getAttribute("classroom");

Integer classroomId =
        (Integer) request.getAttribute("cid");

List<String[]> teachers =
        (List<String[]>)
        request.getAttribute("teachers");

List<String[]> allTeachers =
        (List<String[]>)
        request.getAttribute("allTeachers");

List<String[]> students =
        (List<String[]>)
        request.getAttribute("students");

List<String[]> assignments =
        (List<String[]>)
        request.getAttribute("assignments");

List<String[]> submissionStatus =
        (List<String[]>)
        request.getAttribute("submissionStatus");

List<?> onlineClasses =
        (List<?>)
        request.getAttribute("onlineClasses");

List<String[]> studentQuestions =
        (List<String[]>)
        request.getAttribute("studentQuestions");

DateTimeFormatter timeFormatter =
        DateTimeFormatter.ofPattern("hh:mm a");

String message =
        request.getParameter("msg");

String error =
        request.getParameter("error");

%>


<!-- =========================================================
     MESSAGE
     ========================================================= -->

<% if (message != null && !message.isBlank()) { %>

<div class="message">
    <%= safe(message) %>
</div>

<% } %>


<% if (error != null && !error.isBlank()) { %>

<div class="error">
    <%= safe(error) %>
</div>

<% } %>


<!-- =========================================================
     CLASSROOM HEADER
     ========================================================= -->

<h1>
    <%= classroom != null
            ? safe(classroom[1])
            : "Classroom" %>
</h1>


<% if (classroom != null) { %>

<div class="card">

    <p>
        <strong>Subject:</strong>
        <%= classroom.length > 2
                ? safe(classroom[2])
                : "" %>
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

<% } %>


<!-- =========================================================
     TEACHERS
     ========================================================= -->

<div class="card">

<h2>Teachers</h2>

<%
if (teachers == null || teachers.isEmpty()) {
%>

<p class="empty">
    No teachers assigned.
</p>

<%
} else {

for (String[] teacher : teachers) {
%>

<div class="teacher-item">

    <strong>
        <%= teacher.length > 1
                ? safe(teacher[1])
                : "" %>
    </strong>

    <% if (teacher.length > 2) { %>

    <br>

    <span class="small">
        <%= safe(teacher[2]) %>
    </span>

    <% } %>

</div>

<%
}
}
%>

</div>


<!-- =========================================================
     ADD TEACHER
     ========================================================= -->

<% if ("TEACHER".equals(role)) { %>

<div class="card">

<h2>Add Teacher</h2>

<form
    action="<%=request.getContextPath()%>/action"
    method="post">

<input
    type="hidden"
    name="action"
    value="addTeacher">

<input
    type="hidden"
    name="classroomId"
    value="<%=classroomId%>">

<select
    name="teacherId"
    required>

<option value="">
    -- Select Teacher --
</option>

<%
if (allTeachers != null) {

    for (String[] teacher : allTeachers) {
%>

<option value="<%=safe(teacher[0])%>">
    <%=teacher.length > 1
            ? safe(teacher[1])
            : ""%>
</option>

<%
    }
}
%>

</select>

<button type="submit">
    Add Teacher
</button>

</form>

</div>

<% } %>


<!-- =========================================================
     STUDENTS
     ========================================================= -->

<div class="card">

<h2>Students</h2>

<%
if (students == null || students.isEmpty()) {
%>

<p class="empty">
    No students enrolled.
</p>

<%
} else {

for (String[] student : students) {
%>

<div class="student-item">

    <strong>
        <%=student.length > 1
                ? safe(student[1])
                : ""%>
    </strong>

    <% if (student.length > 2) { %>

    <br>

    <span class="small">
        <%=safe(student[2])%>
    </span>

    <% } %>


    <% if ("TEACHER".equals(role)) { %>

    <form
        action="<%=request.getContextPath()%>/action"
        method="post"
        style="margin-top:10px;">

        <input
            type="hidden"
            name="action"
            value="removeStudent">

        <input
            type="hidden"
            name="classroomId"
            value="<%=classroomId%>">

        <input
            type="hidden"
            name="studentId"
            value="<%=student[0]%>">

        <button
            type="submit"
            class="danger"
            onclick="return confirm('Remove this student from the classroom?');">

            Remove Student

        </button>

    </form>

    <% } %>

</div>

<%
}
}
%>

</div>


<!-- =========================================================
     SCHEDULE ONLINE CLASS
     ========================================================= -->

<% if ("TEACHER".equals(role)) { %>

<div class="card">

<h2>Schedule Online Class</h2>

<p class="small">
    All scheduled times are Indian Standard Time (IST).
</p>

<form
    action="<%=request.getContextPath()%>/action"
    method="post">

<input
    type="hidden"
    name="action"
    value="scheduleOnlineClass">

<input
    type="hidden"
    name="classroomId"
    value="<%=classroomId%>">

<label>Topic</label>

<input
    type="text"
    name="topic"
    placeholder="Example: Java OOP Concepts"
    maxlength="200"
    required>

<label>Date</label>

<input
    type="date"
    name="classDate"
    required>

<label>Start Time (IST)</label>

<input
    type="time"
    name="startTime"
    required>

<label>End Time (IST)</label>

<input
    type="time"
    name="endTime">

<label>Meeting Link</label>

<input
    type="url"
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


<!-- =========================================================
     ONLINE CLASSES
     ========================================================= -->

<div class="card">

<h2>Online Classes</h2>

<%
if (onlineClasses == null ||
    onlineClasses.isEmpty()) {
%>

<p class="empty">
    No online classes scheduled.
</p>

<%
} else {

for (Object onlineClass : onlineClasses) {

String topic =
        getValue(onlineClass, "topic");

String teacherName =
        getValue(onlineClass, "teacherName");

String date =
        getValue(onlineClass, "date");

String startTime =
        getValue(onlineClass, "startTime");

String endTime =
        getValue(onlineClass, "endTime");

String meetingLink =
        getValue(onlineClass, "meetingLink");

String onlineClassId =
        getValue(onlineClass, "id");

String startDisplay =
        startTime;

String endDisplay =
        endTime;

try {

    if (startDisplay != null &&
        !startDisplay.isBlank()) {

        LocalTime start =
                LocalTime.parse(startDisplay);

        startDisplay =
                start.format(timeFormatter);
    }

    if (endDisplay != null &&
        !endDisplay.isBlank()) {

        LocalTime end =
                LocalTime.parse(endDisplay);

        endDisplay =
                end.format(timeFormatter);
    }

} catch (Exception ignored) {
}

%>

<div class="online-class-card">

<h3>
    <%=safe(topic)%>
</h3>

<p>
    <strong>Teacher:</strong>
    <%=safe(teacherName)%>
</p>

<p>
    <strong>Date:</strong>
    <%=safe(date)%>
</p>

<p>

<strong>Time:</strong>

<%=safe(startDisplay)%>

<% if (endDisplay != null &&
       !endDisplay.isBlank()) { %>

-
<%=safe(endDisplay)%>

<% } %>

<strong>(IST)</strong>

</p>


<% if (meetingLink != null &&
       !meetingLink.isBlank()) { %>

<p>

<a
    href="<%=safe(meetingLink)%>"
    target="_blank"
    rel="noopener noreferrer"
    class="button">

    Join Online Class

</a>

</p>

<% } %>


<% if ("TEACHER".equals(role)) { %>

<form
    action="<%=request.getContextPath()%>/action"
    method="post"
    style="margin-top:15px;">

<input
    type="hidden"
    name="action"
    value="deleteOnlineClass">

<input
    type="hidden"
    name="onlineClassId"
    value="<%=safe(onlineClassId)%>">

<input
    type="hidden"
    name="classroomId"
    value="<%=classroomId%>">

<button
    type="submit"
    class="danger"
    onclick="return confirm('Delete this online class?');">

    Delete Online Class

</button>

</form>

<% } %>

</div>

<%
}
}
%>

</div>


<!-- =========================================================
     CREATE ASSIGNMENT
     ========================================================= -->

<% if ("TEACHER".equals(role)) { %>

<div class="card">

<h2>Create Assignment</h2>

<form
    action="<%=request.getContextPath()%>/action"
    method="post">

<input
    type="hidden"
    name="action"
    value="assignment">

<input
    type="hidden"
    name="classroomId"
    value="<%=classroomId%>">

<label>Title</label>

<input
    type="text"
    name="title"
    placeholder="Title"
    maxlength="150"
    required>

<label>Description</label>

<textarea
    name="description"
    placeholder="Description"
    rows="5"></textarea>

<label>Due Date</label>

<input
    type="date"
    name="dueDate"
    required>

<button type="submit">
    Create Assignment
</button>

</form>

</div>

<% } %>


<!-- =========================================================
     ASSIGNMENTS
     ========================================================= -->

<div class="card">

<h2>Assignments</h2>

<%
if (assignments == null ||
    assignments.isEmpty()) {
%>

<p class="empty">
    No assignments available.
</p>

<%
} else {

for (String[] assignment : assignments) {
%>

<div class="online-class-card">

<h3>
    <%=assignment.length > 1
            ? safe(assignment[1])
            : ""%>
</h3>


<p>

<strong>Description:</strong>

<%
if (assignment.length > 2 &&
    assignment[2] != null &&
    !assignment[2].isBlank()) {
%>

<%=safe(assignment[2])%>

<%
} else {
%>

No description

<%
}
%>

</p>


<p>

<strong>Due:</strong>

<%=assignment.length > 3
        ? safe(assignment[3])
        : ""%>

</p>


<!-- =====================================================
     STUDENT PDF SUBMISSION
     ===================================================== -->

<%
boolean deadlinePassed = false;

if (assignment.length > 3 &&
    assignment[3] != null &&
    !assignment[3].isBlank()) {

    try {
        LocalDate dueDate =
                LocalDate.parse(assignment[3]);

        deadlinePassed =
                LocalDate.now().isAfter(dueDate);

    } catch (Exception ignored) {
        deadlinePassed = false;
    }
}
%>

<% if ("STUDENT".equals(role)) { %>

<% if (deadlinePassed) { %>

<div class="deadline-closed">

    <h4>Submission Closed</h4>

    <p>
        The deadline for this assignment has passed.
        You cannot submit a text answer or PDF for this assignment.
    </p>

    <p style="margin-top:8px;">
        <strong>Due date:</strong>
        <%=safe(assignment[3])%>
    </p>

</div>

<% } else { %>

<form
    action="<%=request.getContextPath()%>/action"
    method="post"
    enctype="multipart/form-data">

<input
    type="hidden"
    name="action"
    value="submitAssignment">

<input
    type="hidden"
    name="assignmentId"
    value="<%=assignment[0]%>">

<input
    type="hidden"
    name="classroomId"
    value="<%=classroomId%>">


<label>Your Answer</label>

<textarea
    name="answer"
    placeholder="Optional text answer..."></textarea>


<div class="pdf-note">

<strong>PDF Submission</strong>

<br>

Select your assignment as a PDF file.

<br>

<span class="small">
    PDF only. Maximum file size: 10 MB.
</span>

</div>


<label
    class="file-label"
    for="pdfFile_<%=assignment[0]%>">

    PDF File

</label>


<div class="pdf-upload">

    <input
        id="pdfFile_<%=assignment[0]%>"
        type="file"
        name="pdfFile"
        accept=".pdf,application/pdf"
        required
        onchange="showSelectedFile('<%=assignment[0]%>');">

    <label
        class="choose-file-button"
        for="pdfFile_<%=assignment[0]%>"
        style="display:inline-flex !important; width:fit-content !important; max-width:max-content !important; min-width:0 !important; box-sizing:border-box; align-items:center; justify-content:center; padding:10px 18px !important; margin:7px 0 8px 0 !important; background:#204a87 !important; color:#ffffff !important; border:none !important; border-radius:7px !important; cursor:pointer !important; font-weight:bold !important; font-size:14px !important; line-height:1.2 !important; white-space:nowrap !important;">
        Choose PDF File
    </label>

    <span
        id="selectedFile_<%=assignment[0]%>"
        class="selected-file-name">
        No file selected
    </span>

    <span class="pdf-upload-hint">
        PDF only • Maximum size: 10 MB
    </span>

</div>


<button
    type="submit"
    onclick="return validatePdf('<%=assignment[0]%>');">

    Submit Assignment

</button>

</form>

<% } %>


<%
/* =====================================================
   GET SUBMISSION ID + PDF FILE NAME
   ===================================================== */

Object submissionIdObj = null;
Object pdfFileNameObj = null;

if (assignment.length > 6) {
    submissionIdObj = assignment[6];
}

if (assignment.length > 7) {
    pdfFileNameObj = assignment[7];
}

Integer submissionId = null;

String pdfFileName = null;


if (submissionIdObj != null) {

    try {

        submissionId =
                Integer.valueOf(
                    submissionIdObj.toString()
                );

    } catch (NumberFormatException ignored) {
    }

}


if (pdfFileNameObj != null) {

    pdfFileName =
            pdfFileNameObj.toString();

}

%>


<!-- =====================================================
     SHOW SUBMITTED PDF
     ===================================================== -->

<% if (submissionId != null &&
       pdfFileName != null &&
       !pdfFileName.isBlank()) { %>

<div class="pdf-submission">

<strong>Submitted PDF:</strong>

<span>
    <%=safe(pdfFileName)%>
</span>


<div class="pdf-actions">

<!-- VIEW PDF -->

<a
    href="<%=request.getContextPath()%>/pdf-download?submissionId=<%=submissionId%>"
    target="_blank"
    rel="noopener noreferrer"
    class="button">

    View PDF

</a>


<!-- DOWNLOAD PDF -->

<a
    href="<%=request.getContextPath()%>/pdf-download?submissionId=<%=submissionId%>"
    class="button">

    Download PDF

</a>

</div>

</div>

<% } %>

<% } %>

</div>

<%
}
}
%>

</div>


<!-- =========================================================
     STUDENT HELP / ASK QUESTION
     ========================================================= -->

<% if ("STUDENT".equals(role)) { %>

<div class="card help-card">

    <h2>Student Help</h2>

    <p>
        Have a question about this classroom, assignment, or lesson?
        Ask your teacher here.
    </p>

    <form
        action="<%=request.getContextPath()%>/action"
        method="post">

        <input
            type="hidden"
            name="action"
            value="askQuestion">

        <input
            type="hidden"
            name="classroomId"
            value="<%=classroomId%>">

        <label for="studentQuestion">
            Your Question
        </label>

        <textarea
            id="studentQuestion"
            name="question"
            maxlength="5000"
            placeholder="Type your question here..."
            required></textarea>

        <button
            type="submit"
            style="margin-top: 12px;">

            Ask Question

        </button>

    </form>

</div>

<% } %>


<!-- =========================================================
     TEACHER STUDENT QUESTIONS / HELP
     ========================================================= -->

<% if ("TEACHER".equals(role)) { %>

<div class="card">

    <h2>Student Questions / Help</h2>

    <p class="small">
        Students can ask questions about this classroom, lessons,
        or assignments. You can answer them here.
    </p>

    <%
    if (studentQuestions == null ||
        studentQuestions.isEmpty()) {
    %>

    <p class="empty">
        No student questions have been asked yet.
    </p>

    <%
    } else {
        for (String[] question : studentQuestions) {
    %>

    <div class="question-card">

        <h3>
            <%= question.length > 2
                    ? safe(question[2])
                    : "Student" %>
        </h3>

        <div class="small">
            Asked:
            <%= question.length > 5
                    ? safe(question[5])
                    : "" %>
        </div>

        <div class="question-text">
            <strong>Question:</strong><br>
            <%= question.length > 3
                    ? safe(question[3])
                    : "" %>
        </div>

        <%
        boolean hasAnswer =
                question.length > 4 &&
                question[4] != null &&
                !question[4].isBlank();
        %>

        <% if (hasAnswer) { %>

        <div class="answer-box">

            <div class="answered">
                Answered
            </div>

            <p style="white-space:pre-wrap; word-break:break-word;">
                <%=safe(question[4])%>
            </p>

            <% if (question.length > 6 &&
                   question[6] != null &&
                   !question[6].isBlank()) { %>

            <div class="small">
                Answered:
                <%=safe(question[6])%>
            </div>

            <% } %>

        </div>

        <% } else { %>

        <div class="answer-box">

            <div class="unanswered">
                Awaiting your answer
            </div>

            <form
                action="<%=request.getContextPath()%>/action"
                method="post"
                style="margin-top:12px;">

                <input
                    type="hidden"
                    name="action"
                    value="answerQuestion">

                <input
                    type="hidden"
                    name="questionId"
                    value="<%=question[0]%>">

                <label
                    for="answer_<%=question[0]%>">
                    Your Answer
                </label>

                <textarea
                    id="answer_<%=question[0]%>"
                    name="answer"
                    maxlength="5000"
                    placeholder="Type your answer here..."
                    required></textarea>

                <button type="submit">
                    Answer Question
                </button>

            </form>

        </div>

        <% } %>

    </div>

    <%
        }
    }
    %>

</div>

<% } %>


<!-- =========================================================
     TEACHER SUBMISSION STATUS
     ========================================================= -->

<% if ("TEACHER".equals(role)) { %>

<div class="card">

<h2>Student Submission Status</h2>

<%
if (submissionStatus == null ||
    submissionStatus.isEmpty()) {
%>

<p class="empty">
    No student submission data available
    for this classroom yet.
</p>

<%
} else {
%>

<div style="overflow-x:auto;">

<table>

<thead>

<tr>

<th>Assignment</th>

<th>Student</th>

<th>Submitted</th>

<th>Marks</th>

<th>PDF</th>

<th>Marks & Feedback</th>

</tr>

</thead>

<tbody>

<%

for (String[] status : submissionStatus) {

%>

<tr>

<td>

<%=status.length > 1
        ? safe(status[1])
        : ""%>

</td>


<td>

<%=status.length > 3
        ? safe(status[3])
        : ""%>

</td>


<td>

<%

if (status.length <= 4 ||
    status[4] == null ||
    status[4].isBlank()) {

%>

Not Submitted

<%

} else if (status.length > 5) {

%>

<%=safe(status[5])%>

<%

}

%>

</td>


<td>

<%

if (status.length > 6 &&
    status[6] != null &&
    !status[6].isBlank()) {

%>

<%=safe(status[6])%>

<%

} else {

%>

Not graded

<%

}

%>

</td>

<td>

<%

if (status.length > 7 &&
    status[7] != null &&
    !status[7].isBlank() &&
    status.length > 4 &&
    status[4] != null &&
    !status[4].isBlank()) {

%>

<a
    class="pdf-view-button"
    href="<%=request.getContextPath()%>/pdf-download?submissionId=<%=status[4]%>"
    target="_blank"
    rel="noopener">
    View PDF
</a>

<div class="small pdf-file-name">
    <%=safe(status[7])%>
</div>

<%

} else {

%>

<span class="small">No PDF</span>

<%

}

%>

</td>

<td>

<%
boolean hasSubmission =
        status.length > 4 &&
        status[4] != null &&
        !status[4].isBlank();
%>

<% if (hasSubmission) { %>

<div class="grade-box">

    <%
    if (status.length > 6 &&
        status[6] != null &&
        !status[6].isBlank()) {
    %>

    <div class="grade-current">
        Current Marks:
        <%=safe(status[6])%>/100
    </div>

    <% } else { %>

    <div class="grade-current">
        Not graded yet
    </div>

    <% } %>

    <form
        action="<%=request.getContextPath()%>/action"
        method="post">

        <input
            type="hidden"
            name="action"
            value="gradeSubmission">

        <input
            type="hidden"
            name="classroomId"
            value="<%=classroomId%>">

        <input
            type="hidden"
            name="assignmentId"
            value="<%=status[0]%>">

        <input
            type="hidden"
            name="studentId"
            value="<%=status[2]%>">

        <label
            class="grade-label"
            for="marks_<%=status[4]%>">
            Marks (0-100)
        </label>

        <input
            id="marks_<%=status[4]%>"
            type="number"
            name="marks"
            min="0"
            max="100"
            step="1"
            value="<%=status.length > 6 &&
                      status[6] != null &&
                      !status[6].isBlank()
                      ? safe(status[6])
                      : ""%>"
            required>

        <label
            class="grade-label"
            for="feedback_<%=status[4]%>">
            Feedback
        </label>

        <textarea
            id="feedback_<%=status[4]%>"
            name="feedback"
            maxlength="5000"
            placeholder="Write feedback for the student..."></textarea>

        <button
            type="submit"
            class="save-grade-button">
            Save Marks & Feedback
        </button>

    </form>

</div>

<% } else { %>

<span class="small">
    Student has not submitted yet.
</span>

<% } %>

</td>

</tr>

<%

}

%>

</tbody>

</table>

</div>

<%

}

%>

</div>

<% } %>


</div>


<!-- =========================================================
     PDF VALIDATION
     ========================================================= -->

<script>

function showSelectedFile(assignmentId) {

    const fileInput =
        document.getElementById(
            "pdfFile_" + assignmentId
        );

    const fileName =
        document.getElementById(
            "selectedFile_" + assignmentId
        );

    if (!fileInput || !fileName) {
        return;
    }

    if (fileInput.files &&
        fileInput.files.length > 0) {

        fileName.textContent =
            fileInput.files[0].name;

    } else {

        fileName.textContent =
            "No file selected";
    }
}


function validatePdf(assignmentId) {

    const fileInput =
        document.getElementById(
            "pdfFile_" + assignmentId
        );

    if (!fileInput ||
        !fileInput.files ||
        fileInput.files.length === 0) {

        alert("Please select a PDF file.");

        return false;
    }


    const file =
        fileInput.files[0];


    const fileName =
        file.name.toLowerCase();


    const maxSize =
        10 * 1024 * 1024;


    if (!fileName.endsWith(".pdf")) {

        alert("Only PDF files are allowed.");

        fileInput.value = "";

        return false;
    }


    if (file.size > maxSize) {

        alert(
            "PDF file size must not exceed 10 MB."
        );

        fileInput.value = "";

        return false;
    }


    return true;
}

</script>


</body>

</html>