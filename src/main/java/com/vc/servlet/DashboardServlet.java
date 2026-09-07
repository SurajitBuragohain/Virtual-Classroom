package com.vc.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.vc.util.DB;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet({"/dashboard", "/teacher"})
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("userId") == null
                || session.getAttribute("role") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/index.jsp"
            );
            return;
        }

        Object userIdObject = session.getAttribute("userId");

        if (!(userIdObject instanceof Number)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Invalid user session."
            );
            return;
        }

        int userId = ((Number) userIdObject).intValue();

        String role = String.valueOf(
                session.getAttribute("role")
        );

        /*
         * =========================================================
         * ADMIN
         * =========================================================
         */
        if ("ADMIN".equalsIgnoreCase(role)) {

            response.sendRedirect(
                    request.getContextPath() + "/admin"
            );
            return;
        }

        /*
         * =========================================================
         * TEACHER
         * =========================================================
         */
        if ("TEACHER".equalsIgnoreCase(role)) {

            loadTeacherDashboard(
                    request,
                    userId
            );

            request.getRequestDispatcher(
                    "/dashboard.jsp"
            ).forward(request, response);

            return;
        }

        /*
         * =========================================================
         * STUDENT
         * =========================================================
         */
        if ("STUDENT".equalsIgnoreCase(role)) {

            loadStudentDashboard(
                    request,
                    userId
            );

            request.getRequestDispatcher(
                    "/dashboard.jsp"
            ).forward(request, response);

            return;
        }

        /*
         * =========================================================
         * INVALID ROLE
         * =========================================================
         */
        response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Invalid user role."
        );
    }

    /*
     * =============================================================
     * TEACHER DASHBOARD
     * =============================================================
     */
    private void loadTeacherDashboard(
            HttpServletRequest request,
            int teacherId)
            throws ServletException {

        List<ClassroomRow> classrooms =
                new ArrayList<>();

        String sql =
                "SELECT " +
                "c.classroom_id, " +
                "c.class_name, " +
                "c.subject, " +
                "c.description " +
                "FROM classrooms c " +
                "INNER JOIN teacher_classroom tc " +
                "ON c.classroom_id = tc.classroom_id " +
                "WHERE tc.teacher_id = ? " +
                "ORDER BY c.class_name";

        try (Connection con = DB.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, teacherId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    classrooms.add(
                            new ClassroomRow(
                                    rs.getInt("classroom_id"),
                                    rs.getString("class_name"),
                                    rs.getString("subject"),
                                    rs.getString("description")
                            )
                    );
                }
            }

            request.setAttribute(
                    "teacherClassrooms",
                    classrooms
            );

        } catch (Exception e) {

            throw new ServletException(
                    "Unable to load teacher dashboard.",
                    e
            );
        }
    }

    /*
     * =============================================================
     * STUDENT DASHBOARD
     * =============================================================
     */
    private void loadStudentDashboard(
            HttpServletRequest request,
            int studentId)
            throws ServletException {

        /*
         * ---------------------------------------------------------
         * STUDENT CLASSROOMS
         * ---------------------------------------------------------
         */
        List<ClassroomRow> classrooms =
                new ArrayList<>();

        String classroomSql =
                "SELECT " +
                "c.classroom_id, " +
                "c.class_name, " +
                "c.subject, " +
                "c.description " +
                "FROM classrooms c " +
                "INNER JOIN enrollments e " +
                "ON c.classroom_id = e.classroom_id " +
                "WHERE e.student_id = ? " +
                "ORDER BY c.class_name";

        /*
         * ---------------------------------------------------------
         * STUDENT SUBMISSIONS
         * ---------------------------------------------------------
         */
        List<SubmissionRow> submissions =
                new ArrayList<>();

        String submissionSql =
                "SELECT " +
                "s.submission_id, " +
                "s.assignment_id, " +
                "a.title AS assignment_title, " +
                "c.classroom_id, " +
                "c.class_name, " +
                "s.submitted_at, " +
                "s.marks, " +
                "s.feedback, " +
                "s.pdf_file_name " +
                "FROM submissions s " +
                "INNER JOIN assignments a " +
                "ON s.assignment_id = a.assignment_id " +
                "INNER JOIN classrooms c " +
                "ON a.classroom_id = c.classroom_id " +
                "WHERE s.student_id = ? " +
                "ORDER BY s.submitted_at DESC";

        try (Connection con = DB.getConnection()) {

            /*
             * -----------------------------------------------------
             * LOAD CLASSROOMS
             * -----------------------------------------------------
             */
            try (PreparedStatement ps =
                         con.prepareStatement(classroomSql)) {

                ps.setInt(1, studentId);

                try (ResultSet rs =
                             ps.executeQuery()) {

                    while (rs.next()) {

                        classrooms.add(
                                new ClassroomRow(
                                        rs.getInt("classroom_id"),
                                        rs.getString("class_name"),
                                        rs.getString("subject"),
                                        rs.getString("description")
                                )
                        );
                    }
                }
            }

            /*
             * -----------------------------------------------------
             * LOAD STUDENT SUBMISSIONS
             * -----------------------------------------------------
             */
            try (PreparedStatement ps =
                         con.prepareStatement(submissionSql)) {

                ps.setInt(1, studentId);

                try (ResultSet rs =
                             ps.executeQuery()) {

                    while (rs.next()) {

                        submissions.add(
                                new SubmissionRow(
                                        rs.getInt("submission_id"),
                                        rs.getInt("assignment_id"),
                                        rs.getString("assignment_title"),
                                        rs.getInt("classroom_id"),
                                        rs.getString("class_name"),
                                        rs.getTimestamp("submitted_at"),
                                        rs.getObject("marks") != null
                                                ? rs.getInt("marks")
                                                : null,
                                        rs.getString("feedback"),
                                        rs.getString("pdf_file_name")
                                )
                        );
                    }
                }
            }

            /*
             * -----------------------------------------------------
             * SEND DATA TO dashboard.jsp
             * -----------------------------------------------------
             */
            request.setAttribute(
                    "studentClassrooms",
                    classrooms
            );

            request.setAttribute(
                    "studentSubmissions",
                    submissions
            );

        } catch (Exception e) {

            throw new ServletException(
                    "Unable to load student dashboard.",
                    e
            );
        }
    }

    /*
     * =============================================================
     * CLASSROOM ROW
     * =============================================================
     */
    public static class ClassroomRow {

        public final int classroomId;
        public final String className;
        public final String subject;
        public final String description;

        public ClassroomRow(
                int classroomId,
                String className,
                String subject,
                String description) {

            this.classroomId = classroomId;
            this.className = className;
            this.subject = subject;
            this.description = description;
        }
    }

    /*
     * =============================================================
     * STUDENT SUBMISSION ROW
     * =============================================================
     *
     * This object carries all submission information
     * from DashboardServlet to dashboard.jsp.
     * =============================================================
     */
    public static class SubmissionRow {

        public final int submissionId;
        public final int assignmentId;
        public final String assignmentTitle;
        public final int classroomId;
        public final String classroomName;
        public final Timestamp submittedAt;
        public final Integer marks;
        public final String feedback;
        public final String pdfFileName;

        public SubmissionRow(
                int submissionId,
                int assignmentId,
                String assignmentTitle,
                int classroomId,
                String classroomName,
                Timestamp submittedAt,
                Integer marks,
                String feedback,
                String pdfFileName) {

            this.submissionId = submissionId;
            this.assignmentId = assignmentId;
            this.assignmentTitle = assignmentTitle;
            this.classroomId = classroomId;
            this.classroomName = classroomName;
            this.submittedAt = submittedAt;
            this.marks = marks;
            this.feedback = feedback;
            this.pdfFileName = pdfFileName;
        }
    }
}