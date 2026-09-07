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

    List<?> studentSubmissions = null;

    if ("STUDENT".equalsIgnoreCase(role)) {
        studentSubmissions =
                (List<?>) request.getAttribute("studentSubmissions");
    }
%>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Dashboard | Virtual Classroom</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">

    <style>

        /* =====================================================
           STUDENT SUBMISSION RESULTS
           ===================================================== */

        .results-section {
            margin-top: 40px;
        }

        .results-section h2 {
            margin-bottom: 8px;
        }

        .results-intro {
            color: #666;
            margin-bottom: 20px;
        }

        .submission-card {
            background: #ffffff;
            border-radius: 12px;
            padding: 22px;
            margin-bottom: 20px;
            box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
            border: 1px solid #e5e7eb;
        }

        .submission-card h3 {
            margin-top: 0;
            margin-bottom: 8px;
        }

        .submission-classroom {
            color: #666;
            margin-bottom: 18px;
        }

        .submission-info {
            display: grid;
            grid-template-columns:
                repeat(auto-fit, minmax(180px, 1fr));
            gap: 14px;
            margin-bottom: 18px;
        }

        .submission-info-box {
            background: #f7f9fc;
            border-radius: 8px;
            padding: 12px;
        }

        .submission-info-box strong {
            display: block;
            margin-bottom: 5px;
            color: #333;
        }

        .marks-value {
            font-size: 20px;
            font-weight: 700;
        }

        .feedback-box {
            background: #f7f9fc;
            border-left: 4px solid #28558f;
            padding: 14px;
            margin-top: 15px;
            border-radius: 6px;
        }

        .feedback-box strong {
            display: block;
            margin-bottom: 7px;
        }

        .pdf-result {
            margin-top: 18px;
            padding-top: 16px;
            border-top: 1px solid #e5e7eb;
        }

        .pdf-result-name {
            display: inline-block;
            margin-right: 12px;
            color: #555;
        }

        .view-pdf-button {
            display: inline-block;
            padding: 9px 16px;
            background: #28558f;
            color: white !important;
            text-decoration: none;
            border-radius: 7px;
            font-weight: 600;
        }

        .view-pdf-button:hover {
            background: #1d426f;
        }

        .not-graded {
            color: #a16207;
            font-weight: 600;
        }

        .no-feedback {
            color: #777;
            font-style: italic;
        }

        .no-submissions {
            text-align: center;
            padding: 30px;
        }

        @media (max-width: 600px) {

            .submission-info {
                grid-template-columns: 1fr;
            }

            .view-pdf-button {
                margin-top: 10px;
            }

        }

    </style>

</head>

<body>

<nav>

    <span>Virtual Classroom</span>

    <div class="nav-links">

        <a href="${pageContext.request.contextPath}/logout">
            Logout
        </a>

    </div>

</nav>


<main>

    <h1>
        Welcome,
        <%= session.getAttribute("name") %>!
    </h1>


    <!-- =====================================================
         TEACHER DASHBOARD
         ===================================================== -->

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


    <!-- =====================================================
         STUDENT DASHBOARD
         ===================================================== -->

    <% } else if ("STUDENT".equalsIgnoreCase(role)) { %>

        <div class="card">

            <h2>Student Dashboard</h2>

            <p class="muted">
                View your enrolled classrooms, assignments,
                submissions, marks and teacher feedback.
            </p>

            <p>

                <a class="button-link"
                   href="<%= request.getContextPath() %>/classrooms">

                    Browse Classrooms

                </a>

            </p>

        </div>

    <% } %>


    <!-- =====================================================
         MY CLASSROOMS
         ===================================================== -->

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

                    <a
                        href="<%= request.getContextPath() %>/classroom?id=<%= classroom.classroomId %>"
                        class="button-link">

                        Open Classroom

                    </a>

                </div>

            <% } %>

        <% } else { %>

            <div
                class="card"
                style="grid-column: 1 / -1; text-align: center;">

                <p class="muted">

                    You are not currently associated
                    with any classrooms.

                </p>

            </div>

        <% } %>

    </div>


    <!-- =====================================================
         STUDENT SUBMISSIONS / MARKS / FEEDBACK
         ===================================================== -->

    <% if ("STUDENT".equalsIgnoreCase(role)) { %>

        <section class="results-section">

            <h2>
                My Assignment Results
            </h2>

            <p class="results-intro">

                View your submitted assignments,
                marks, teacher feedback and submitted PDFs.

            </p>


            <% if (studentSubmissions != null
                    && !studentSubmissions.isEmpty()) { %>


                <% for (Object obj : studentSubmissions) {

                    DashboardServlet.SubmissionRow submission =
                            (DashboardServlet.SubmissionRow) obj;

                %>


                    <div class="submission-card">

                        <!-- Assignment title -->

                        <h3>

                            <%= submission.assignmentTitle %>

                        </h3>


                        <!-- Classroom -->

                        <div class="submission-classroom">

                            <b>Classroom:</b>

                            <%= submission.classroomName %>

                        </div>


                        <!-- Submission information -->

                        <div class="submission-info">


                            <div class="submission-info-box">

                                <strong>
                                    Submitted
                                </strong>

                                <%
                                    if (submission.submittedAt != null) {
                                %>

                                    <%= submission.submittedAt %>

                                <%
                                    } else {
                                %>

                                    Not available

                                <%
                                    }
                                %>

                            </div>


                            <div class="submission-info-box">

                                <strong>
                                    Marks
                                </strong>

                                <%
                                    if (submission.marks != null) {
                                %>

                                    <span class="marks-value">

                                        <%= submission.marks %> / 100

                                    </span>

                                <%
                                    } else {
                                %>

                                    <span class="not-graded">

                                        Not graded yet

                                    </span>

                                <%
                                    }
                                %>

                            </div>


                            <div class="submission-info-box">

                                <strong>
                                    Submission ID
                                </strong>

                                #<%= submission.submissionId %>

                            </div>


                        </div>


                        <!-- =================================================
                             TEACHER FEEDBACK
                             ================================================= -->

                        <div class="feedback-box">

                            <strong>
                                Teacher Feedback
                            </strong>

                            <%
                                if (submission.feedback != null
                                        && !submission.feedback.trim().isEmpty()) {
                            %>

                                <%= submission.feedback %>

                            <%
                                } else {
                            %>

                                <span class="no-feedback">

                                    No feedback yet.

                                </span>

                            <%
                                }
                            %>

                        </div>


                        <!-- =================================================
                             PDF SUBMISSION
                             ================================================= -->

                        <%
                            if (submission.pdfFileName != null
                                    && !submission.pdfFileName.trim().isEmpty()) {
                        %>

                            <div class="pdf-result">

                                <span class="pdf-result-name">

                                    <b>Submitted PDF:</b>

                                    <%= submission.pdfFileName %>

                                </span>


                                <a
                                    class="view-pdf-button"
                                    href="<%= request.getContextPath() %>/pdf-download?submissionId=<%= submission.submissionId %>"
                                    target="_blank">

                                    View PDF

                                </a>

                            </div>

                        <%
                            } else {
                        %>

                            <div class="pdf-result">

                                <span class="no-feedback">

                                    No PDF submitted.

                                </span>

                            </div>

                        <%
                            }
                        %>


                    </div>


                <% } %>


            <% } else { %>


                <div class="card no-submissions">

                    <h3>
                        No Submissions Yet
                    </h3>

                    <p class="muted">

                        You have not submitted any assignments yet.

                    </p>

                </div>


            <% } %>

        </section>

    <% } %>


</main>

</body>

</html>