package com.vc.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;

import com.vc.util.DB;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet("/action")
@MultipartConfig(
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 12 * 1024 * 1024,
        fileSizeThreshold = 1024 * 1024
)
public class ActionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.sendRedirect(
                request.getContextPath() + "/index.jsp"
        );
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/index.jsp?error=Please%20login%20first"
            );
            return;
        }

        String action = request.getParameter("action");

        Integer userId =
                (Integer) session.getAttribute("userId");

        String role =
                (String) session.getAttribute("role");

        if (action == null || action.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Action is required."
            );
            return;
        }

        try {

            switch (action) {

                case "createClass":
                    createClass(
                            request,
                            response,
                            userId,
                            role
                    );
                    break;

                case "join":
                    joinClass(
                            request,
                            response,
                            userId,
                            role
                    );
                    break;

                case "addTeacher":
                    addTeacher(
                            request,
                            response,
                            userId,
                            role
                    );
                    break;

                case "removeStudent":
                    removeStudent(
                            request,
                            response,
                            userId,
                            role
                    );
                    break;

                case "assignment":
                case "createAssignment":
                    createAssignment(
                            request,
                            response,
                            userId,
                            role
                    );
                    break;

                case "submit":
                case "submitAssignment":
                    submitAssignment(
                            request,
                            response,
                            userId,
                            role
                    );
                    break;

                case "gradeSubmission":
                    gradeSubmission(
                            request,
                            response,
                            userId,
                            role
                    );
                    break;

                /*
                 * Student asks a question.
                 */
                case "askQuestion":
                    askQuestion(
                            request,
                            response,
                            userId,
                            role
                    );
                    break;

                /*
                 * Teacher answers a question.
                 */
                case "answerQuestion":
                    answerQuestion(
                            request,
                            response,
                            userId,
                            role
                    );
                    break;

                default:
                    response.sendError(
                            HttpServletResponse.SC_BAD_REQUEST,
                            "Unknown action: " + action
                    );
            }

        } catch (IllegalArgumentException e) {

            getServletContext().log(
                    "Invalid action: " + action,
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {

            getServletContext().log(
                    "Database error during action: " + action,
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error. Please try again."
            );
        }
    }

    private void createClass(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId,
            String role)
            throws SQLException, IOException {

        requireRole(role, "TEACHER");

        String name =
                required(
                        request.getParameter("name"),
                        "Class name"
                );

        String subject =
                required(
                        request.getParameter("subject"),
                        "Subject"
                );

        String description =
                request.getParameter("description");

        try (Connection connection =
                     DB.getConnection()) {

            connection.setAutoCommit(false);

            try {

                int classroomId;

                String sql =
                        "INSERT INTO classrooms " +
                        "(class_name, subject, description) " +
                        "VALUES (?, ?, ?)";

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     sql,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {

                    statement.setString(1, name);
                    statement.setString(2, subject);

                    statement.setString(
                            3,
                            description == null
                                    ? null
                                    : description.trim()
                    );

                    statement.executeUpdate();

                    try (ResultSet keys =
                                 statement.getGeneratedKeys()) {

                        if (!keys.next()) {
                            throw new SQLException(
                                    "Classroom ID was not generated."
                            );
                        }

                        classroomId =
                                keys.getInt(1);
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     "INSERT INTO teacher_classroom " +
                                     "(teacher_id, classroom_id) " +
                                     "VALUES (?, ?)"
                             )) {

                    statement.setInt(1, userId);
                    statement.setInt(2, classroomId);

                    statement.executeUpdate();
                }

                connection.commit();

                response.sendRedirect(
                        request.getContextPath()
                                + "/dashboard"
                );

            } catch (SQLException | IOException e) {

                connection.rollback();
                throw e;

            } finally {

                connection.setAutoCommit(true);
            }
        }
    }

    private void joinClass(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId,
            String role)
            throws SQLException, IOException {

        requireRole(role, "STUDENT");

        int classroomId =
                integer(
                        request.getParameter("classroomId"),
                        "Classroom ID"
                );

        String sql =
                "INSERT INTO enrollments " +
                "(student_id, classroom_id) " +
                "VALUES (?, ?)";

        try (
                Connection connection =
                        DB.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, userId);
            statement.setInt(2, classroomId);

            statement.executeUpdate();
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/dashboard?msg=Joined%20classroom"
        );
    }

    private void addTeacher(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId,
            String role)
            throws SQLException, IOException {

        requireRole(role, "TEACHER");

        int classroomId =
                integer(
                        request.getParameter("classroomId"),
                        "Classroom ID"
                );

        int teacherId =
                integer(
                        request.getParameter("teacherId"),
                        "Teacher ID"
                );

        requireTeacherAccess(
                userId,
                classroomId
        );

        try (
                Connection connection =
                        DB.getConnection();

                PreparedStatement checkTeacher =
                        connection.prepareStatement(
                                "SELECT 1 FROM users " +
                                "WHERE user_id=? AND role='TEACHER'"
                        )
        ) {

            checkTeacher.setInt(1, teacherId);

            try (ResultSet rs =
                         checkTeacher.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Selected user is not a teacher."
                    );
                }
            }
        }

        String sql =
                "INSERT IGNORE INTO teacher_classroom " +
                "(teacher_id, classroom_id) " +
                "VALUES (?, ?)";

        try (
                Connection connection =
                        DB.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, teacherId);
            statement.setInt(2, classroomId);

            statement.executeUpdate();
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/classroom?id="
                        + classroomId
                        + "&msg=Teacher%20added"
        );
    }

    private void removeStudent(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId,
            String role)
            throws SQLException, IOException {

        requireRole(role, "TEACHER");

        int classroomId =
                integer(
                        request.getParameter("classroomId"),
                        "Classroom ID"
                );

        int studentId =
                integer(
                        request.getParameter("studentId"),
                        "Student ID"
                );

        /*
         * Only a teacher assigned to this classroom
         * can remove a student from it.
         */
        requireTeacherAccess(
                userId,
                classroomId
        );

        /*
         * Remove only the student's enrollment.
         * The student account is NOT deleted.
         */
        String sql =
                "DELETE FROM enrollments " +
                "WHERE student_id = ? " +
                "AND classroom_id = ?";

        try (
                Connection connection =
                        DB.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, studentId);
            statement.setInt(2, classroomId);

            int removed =
                    statement.executeUpdate();

            if (removed == 0) {
                throw new IllegalArgumentException(
                        "Student is not enrolled in this classroom."
                );
            }
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/classroom?id="
                        + classroomId
                        + "&msg=Student%20removed"
        );
    }


    private void createAssignment(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId,
            String role)
            throws SQLException, IOException {

        requireRole(role, "TEACHER");

        int classroomId =
                integer(
                        request.getParameter("classroomId"),
                        "Classroom ID"
                );

        String title =
                required(
                        request.getParameter("title"),
                        "Title"
                );

        String description =
                required(
                        request.getParameter("description"),
                        "Description"
                );

        String dueDate =
                required(
                        request.getParameter("dueDate"),
                        "Due date"
                );

        requireTeacherAccess(
                userId,
                classroomId
        );

        Date date;

        try {
            date = Date.valueOf(dueDate);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Due date must be in YYYY-MM-DD format."
            );
        }

        /*
         * Deadline feature:
         * Assignment cannot be created with a past due date.
         */
        LocalDate today =
                LocalDate.now(ZoneId.of("Asia/Kolkata"));

        if (date.toLocalDate().isBefore(today)) {
            throw new IllegalArgumentException(
                    "Due date cannot be in the past."
            );
        }

        String sql =
                "INSERT INTO assignments " +
                "(classroom_id, title, description, due_date) " +
                "VALUES (?, ?, ?, ?)";

        try (
                Connection connection =
                        DB.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, classroomId);
            statement.setString(2, title);
            statement.setString(3, description);
            statement.setDate(4, date);

            statement.executeUpdate();
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/classroom?id="
                        + classroomId
                        + "&msg=Assignment%20created"
        );
    }

    private void submitAssignment(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId,
            String role)
            throws SQLException, IOException, ServletException {

        requireRole(role, "STUDENT");

        int assignmentId =
                integer(
                        request.getParameter("assignmentId"),
                        "Assignment ID"
                );

        int classroomId =
                integer(
                        request.getParameter("classroomId"),
                        "Classroom ID"
                );

        /*
         * PDF submission.
         */
        Part pdfPart =
                request.getPart("pdfFile");

        if (pdfPart == null ||
                pdfPart.getSize() == 0) {

            throw new IllegalArgumentException(
                    "Please select a PDF file to submit."
            );
        }

        final long maxPdfSize =
                10L * 1024L * 1024L;

        if (pdfPart.getSize() > maxPdfSize) {

            throw new IllegalArgumentException(
                    "PDF file must be 10 MB or smaller."
            );
        }

        String submittedFileName =
                pdfPart.getSubmittedFileName();

        if (submittedFileName == null ||
                submittedFileName.isBlank()) {

            throw new IllegalArgumentException(
                    "Please select a PDF file to submit."
            );
        }

        /*
         * Remove directory information from filename.
         */
        submittedFileName =
                submittedFileName
                        .replace("\\", "/")
                        .substring(
                                submittedFileName
                                        .replace("\\", "/")
                                        .lastIndexOf('/') + 1
                        );

        if (!submittedFileName
                .toLowerCase()
                .endsWith(".pdf")) {

            throw new IllegalArgumentException(
                    "Only PDF files are allowed."
            );
        }

        /*
         * Verify actual PDF magic bytes.
         * %PDF-
         */
        try (InputStream input =
                     pdfPart.getInputStream()) {

            byte[] header = new byte[5];

            int bytesRead =
                    input.read(header);

            if (bytesRead != 5 ||
                    header[0] != '%' ||
                    header[1] != 'P' ||
                    header[2] != 'D' ||
                    header[3] != 'F' ||
                    header[4] != '-') {

                throw new IllegalArgumentException(
                        "The uploaded file is not a valid PDF."
                );
            }
        }

        /*
         * Check classroom enrollment and deadline.
         */
        String accessSql =
                "SELECT a.due_date " +
                "FROM assignments a " +
                "JOIN enrollments e " +
                "ON e.classroom_id = a.classroom_id " +
                "WHERE a.assignment_id = ? " +
                "AND a.classroom_id = ? " +
                "AND e.student_id = ?";

        try (
                Connection connection =
                        DB.getConnection();

                PreparedStatement access =
                        connection.prepareStatement(accessSql)
        ) {

            access.setInt(1, assignmentId);
            access.setInt(2, classroomId);
            access.setInt(3, userId);

            try (ResultSet result =
                         access.executeQuery()) {

                if (!result.next()) {

                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "You cannot submit this assignment."
                    );

                    return;
                }

                Date assignmentDueDate =
                        result.getDate("due_date");

                /*
                 * Deadline feature:
                 * Submission is blocked after due date.
                 */
                if (assignmentDueDate == null ||
                        assignmentDueDate
                                .toLocalDate()
                                .isBefore(
                                        LocalDate.now(
                                                ZoneId.of("Asia/Kolkata")
                                        )
                                )) {

                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "The submission deadline has passed."
                    );

                    return;
                }
            }

            Integer submissionId = null;

            String findSql =
                    "SELECT submission_id " +
                    "FROM submissions " +
                    "WHERE assignment_id = ? " +
                    "AND student_id = ? " +
                    "ORDER BY submission_id DESC " +
                    "LIMIT 1";

            try (PreparedStatement find =
                         connection.prepareStatement(findSql)) {

                find.setInt(1, assignmentId);
                find.setInt(2, userId);

                try (ResultSet rs =
                             find.executeQuery()) {

                    if (rs.next()) {

                        submissionId =
                                rs.getInt("submission_id");
                    }
                }
            }

            /*
             * First submission.
             */
            if (submissionId == null) {

                String insertSql =
                        "INSERT INTO submissions " +
                        "(assignment_id, student_id, answer, " +
                        "pdf_file_name, pdf_content_type, pdf_data) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

                try (PreparedStatement statement =
                             connection.prepareStatement(insertSql)) {

                    statement.setInt(1, assignmentId);
                    statement.setInt(2, userId);

                    /*
                     * Existing answer column is NOT NULL.
                     * PDF is the actual assignment submission.
                     */
                    statement.setString(3, "");

                    statement.setString(
                            4,
                            submittedFileName
                    );

                    statement.setString(
                            5,
                            "application/pdf"
                    );

                    statement.setBinaryStream(
                            6,
                            pdfPart.getInputStream(),
                            pdfPart.getSize()
                    );

                    statement.executeUpdate();
                }

            } else {

                /*
                 * Resubmission.
                 *
                 * Replace old PDF and clear previous grade.
                 */
                String updateSql =
                        "UPDATE submissions " +
                        "SET answer = ?, " +
                        "pdf_file_name = ?, " +
                        "pdf_content_type = ?, " +
                        "pdf_data = ?, " +
                        "submitted_at = CURRENT_TIMESTAMP, " +
                        "marks = NULL, " +
                        "feedback = NULL " +
                        "WHERE submission_id = ?";

                try (PreparedStatement statement =
                             connection.prepareStatement(updateSql)) {

                    statement.setString(1, "");

                    statement.setString(
                            2,
                            submittedFileName
                    );

                    statement.setString(
                            3,
                            "application/pdf"
                    );

                    statement.setBinaryStream(
                            4,
                            pdfPart.getInputStream(),
                            pdfPart.getSize()
                    );

                    statement.setInt(
                            5,
                            submissionId
                    );

                    statement.executeUpdate();
                }
            }
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/classroom?id="
                        + classroomId
                        + "&msg=Assignment%20submitted"
        );
    }

    private void gradeSubmission(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId,
            String role)
            throws SQLException, IOException {

        requireRole(role, "TEACHER");

        int assignmentId =
                integer(
                        request.getParameter("assignmentId"),
                        "Assignment ID"
                );

        int studentId =
                integer(
                        request.getParameter("studentId"),
                        "Student ID"
                );

        int classroomId =
                integer(
                        request.getParameter("classroomId"),
                        "Classroom ID"
                );

        String marksInput =
                required(
                        request.getParameter("marks"),
                        "Marks"
                );

        String feedback =
                request.getParameter("feedback");

        int marks;

        try {

            marks =
                    Integer.parseInt(
                            marksInput.trim()
                    );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Marks must be a valid number."
            );
        }

        if (marks < 0 || marks > 100) {

            throw new IllegalArgumentException(
                    "Marks must be between 0 and 100."
            );
        }

        requireTeacherAccess(
                userId,
                classroomId
        );

        String checkSql =
                "SELECT 1 " +
                "FROM submissions s " +
                "JOIN assignments a " +
                "ON a.assignment_id = s.assignment_id " +
                "WHERE s.assignment_id = ? " +
                "AND s.student_id = ? " +
                "AND a.classroom_id = ?";

        try (
                Connection connection =
                        DB.getConnection();

                PreparedStatement check =
                        connection.prepareStatement(checkSql)
        ) {

            check.setInt(1, assignmentId);
            check.setInt(2, studentId);
            check.setInt(3, classroomId);

            try (ResultSet result =
                         check.executeQuery()) {

                if (!result.next()) {

                    response.sendError(
                            HttpServletResponse.SC_NOT_FOUND,
                            "Submission not found."
                    );

                    return;
                }
            }

            String updateSql =
                    "UPDATE submissions " +
                    "SET marks = ?, feedback = ? " +
                    "WHERE assignment_id = ? " +
                    "AND student_id = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(updateSql)) {

                statement.setInt(1, marks);

                if (feedback == null ||
                        feedback.isBlank()) {

                    statement.setNull(
                            2,
                            java.sql.Types.LONGVARCHAR
                    );

                } else {

                    statement.setString(
                            2,
                            feedback.trim()
                    );
                }

                statement.setInt(3, assignmentId);
                statement.setInt(4, studentId);

                statement.executeUpdate();
            }
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/classroom?id="
                        + classroomId
                        + "&msg=Grade%20saved"
        );
    }

    /*
     * ============================================================
     * STUDENT HELP FEATURE
     * ============================================================
     */

    private void askQuestion(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId,
            String role)
            throws SQLException, IOException {

        requireRole(role, "STUDENT");

        int classroomId =
                integer(
                        request.getParameter("classroomId"),
                        "Classroom ID"
                );

        String question =
                required(
                        request.getParameter("question"),
                        "Question"
                );

        if (question.length() > 5000) {

            throw new IllegalArgumentException(
                    "Question cannot exceed 5000 characters."
            );
        }

        /*
         * Make sure the student actually belongs
         * to this classroom.
         */
        String enrollmentSql =
                "SELECT 1 " +
                "FROM enrollments " +
                "WHERE student_id = ? " +
                "AND classroom_id = ?";

        try (
                Connection connection =
                        DB.getConnection();

                PreparedStatement enrollment =
                        connection.prepareStatement(
                                enrollmentSql
                        )
        ) {

            enrollment.setInt(1, userId);
            enrollment.setInt(2, classroomId);

            try (ResultSet rs =
                         enrollment.executeQuery()) {

                if (!rs.next()) {

                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "You are not enrolled in this classroom."
                    );

                    return;
                }
            }

            /*
             * Save student's question.
             */
            String insertSql =
                    "INSERT INTO student_questions " +
                    "(student_id, classroom_id, question) " +
                    "VALUES (?, ?, ?)";

            try (PreparedStatement statement =
                         connection.prepareStatement(insertSql)) {

                statement.setInt(1, userId);
                statement.setInt(2, classroomId);
                statement.setString(3, question);

                statement.executeUpdate();
            }
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/classroom?id="
                        + classroomId
                        + "&msg=Question%20submitted"
        );
    }

    /*
     * Teacher answers a student's question.
     */
    private void answerQuestion(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId,
            String role)
            throws SQLException, IOException {

        requireRole(role, "TEACHER");

        int questionId =
                integer(
                        request.getParameter("questionId"),
                        "Question ID"
                );

        String answer =
                required(
                        request.getParameter("answer"),
                        "Answer"
                );

        if (answer.length() > 5000) {

            throw new IllegalArgumentException(
                    "Answer cannot exceed 5000 characters."
            );
        }

        /*
         * The UPDATE itself verifies that the teacher
         * belongs to the question's classroom.
         */
        String updateSql =
                "UPDATE student_questions q " +
                "INNER JOIN teacher_classroom tc " +
                "ON q.classroom_id = tc.classroom_id " +
                "SET q.answer = ?, " +
                "q.answered_at = CURRENT_TIMESTAMP " +
                "WHERE q.question_id = ? " +
                "AND tc.teacher_id = ?";

        int classroomId = -1;

        try (Connection connection =
                     DB.getConnection()) {

            /*
             * Get classroom ID first so we can redirect
             * back to the correct classroom.
             */
            String classroomSql =
                    "SELECT q.classroom_id " +
                    "FROM student_questions q " +
                    "INNER JOIN teacher_classroom tc " +
                    "ON q.classroom_id = tc.classroom_id " +
                    "WHERE q.question_id = ? " +
                    "AND tc.teacher_id = ?";

            try (PreparedStatement find =
                         connection.prepareStatement(
                                 classroomSql
                         )) {

                find.setInt(1, questionId);
                find.setInt(2, userId);

                try (ResultSet rs =
                             find.executeQuery()) {

                    if (!rs.next()) {

                        response.sendError(
                                HttpServletResponse.SC_FORBIDDEN,
                                "You are not authorized to answer this question."
                        );

                        return;
                    }

                    classroomId =
                            rs.getInt("classroom_id");
                }
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 updateSql
                         )) {

                statement.setString(1, answer);
                statement.setInt(2, questionId);
                statement.setInt(3, userId);

                int updated =
                        statement.executeUpdate();

                if (updated == 0) {

                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "You are not authorized to answer this question."
                    );

                    return;
                }
            }
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/classroom?id="
                        + classroomId
                        + "&msg=Answer%20saved"
        );
    }

    /*
     * ============================================================
     * COMMON SECURITY / VALIDATION METHODS
     * ============================================================
     */

    private void requireTeacherAccess(
            int teacherId,
            int classroomId)
            throws SQLException {

        String sql =
                "SELECT 1 " +
                "FROM teacher_classroom " +
                "WHERE teacher_id = ? " +
                "AND classroom_id = ?";

        try (
                Connection connection =
                        DB.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, teacherId);
            statement.setInt(2, classroomId);

            try (ResultSet result =
                         statement.executeQuery()) {

                if (!result.next()) {

                    throw new SQLException(
                            "Teacher is not assigned to this classroom."
                    );
                }
            }
        }
    }

    private static void requireRole(
            String actual,
            String required)
            throws IOException {

        if (!required.equals(actual)) {

            throw new IOException(
                    "Forbidden action."
            );
        }
    }

    private static int integer(
            String value,
            String field) {

        if (value == null ||
                value.isBlank()) {

            throw new IllegalArgumentException(
                    field + " is required."
            );
        }

        try {

            return Integer.parseInt(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    field + " must be a valid number."
            );
        }
    }

    private static String required(
            String value,
            String field) {

        if (value == null ||
                value.isBlank()) {

            throw new IllegalArgumentException(
                    field + " is required."
            );
        }

        return value.trim();
    }
}