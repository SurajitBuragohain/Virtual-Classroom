package com.vc.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.vc.util.DB;
import com.vc.util.Password;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Admin access required.");
            return;
        }

        try {
            loadStatistics(request);
            loadUsers(request);
            loadTeachers(request);
            loadStudents(request);
            loadClassrooms(request);
            loadAssignments(request);
            loadSubmissions(request);
            loadOnlineClasses(request);
            loadAuditLogs(request);

            request.getRequestDispatcher("/admin.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load admin dashboard."
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Admin access required.");
            return;
        }

        String action = request.getParameter("action");

        if (action == null || action.isBlank()) {
            redirectWithMessage(request, response,
                    "Invalid admin action.");
            return;
        }

        try {
            switch (action) {

                case "updateUser" -> updateUser(request, response);

                case "toggleUser" -> toggleUser(request, response);

                case "resetPassword" -> resetPassword(request, response);

                case "deleteUser" -> deleteUser(request, response);

                case "createClassroom" -> createClassroom(request, response);

                case "updateClassroom" -> updateClassroom(request, response);

                case "deleteClassroom" -> deleteClassroom(request, response);

                case "enrollStudent" -> enrollStudent(request, response);

                case "removeEnrollment" -> removeEnrollment(request, response);

                case "deleteAssignment" -> deleteAssignment(request, response);

                case "deleteSubmission" -> deleteSubmission(request, response);

                case "deleteOnlineClass" -> deleteOnlineClass(request, response);

                default -> response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Unknown admin action."
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
            redirectWithMessage(request, response,
                    "Database error: " + safeMessage(e));
        } catch (IllegalArgumentException e) {
            redirectWithMessage(request, response,
                    "Invalid input: " + safeMessage(e));
        }
    }

    private boolean isAdmin(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return false;
        }

        String role = (String) session.getAttribute("role");

        return "ADMIN".equalsIgnoreCase(role);
    }

    // =========================================================
    // DASHBOARD DATA
    // =========================================================

    private void loadStatistics(HttpServletRequest request)
            throws SQLException {

        int totalStudents = 0;
        int totalTeachers = 0;
        int activeClassrooms = 0;
        int scheduledClasses = 0;

        try (Connection con = DB.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE role = 'STUDENT'")) {

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalStudents = rs.getInt(1);
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE role = 'TEACHER'")) {

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalTeachers = rs.getInt(1);
                    }
                }
            }

            /*
             * classrooms table does not have is_active.
             * Therefore all existing classrooms are counted here.
             */
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM classrooms")) {

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        activeClassrooms = rs.getInt(1);
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    """
                    SELECT COUNT(*)
                    FROM online_classes
                    WHERE TIMESTAMP(class_date, start_time) >= CURRENT_TIMESTAMP
                    """)) {

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        scheduledClasses = rs.getInt(1);
                    }
                }
            }
        }

        request.setAttribute("totalStudents", totalStudents);
        request.setAttribute("totalTeachers", totalTeachers);
        request.setAttribute("activeClassrooms", activeClassrooms);
        request.setAttribute("scheduledClasses", scheduledClasses);
    }

    private void loadUsers(HttpServletRequest request)
            throws SQLException {

        List<UserRow> users = new ArrayList<>();

        String sql = """
                SELECT
                    u.user_id,
                    u.name,
                    u.email,
                    u.role,
                    u.phone,
                    u.is_active,
                    u.teacher_id,
                    t.name AS teacher_name,
                    u.reg_date
                FROM users u
                LEFT JOIN users t
                    ON u.teacher_id = t.user_id
                ORDER BY u.user_id DESC
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                UserRow row = new UserRow();

                row.userId = rs.getInt("user_id");
                row.name = rs.getString("name");
                row.email = rs.getString("email");
                row.role = rs.getString("role");
                row.phone = rs.getString("phone");
                row.active = rs.getBoolean("is_active");
                row.teacherId = (Integer) rs.getObject("teacher_id");
                row.teacherName = rs.getString("teacher_name");
                row.regDate = rs.getTimestamp("reg_date");

                users.add(row);
            }
        }

        request.setAttribute("users", users);
    }

    private void loadTeachers(HttpServletRequest request)
            throws SQLException {

        List<UserRow> teachers = new ArrayList<>();

        String sql = """
                SELECT user_id, name, email, phone, is_active
                FROM users
                WHERE role = 'TEACHER'
                ORDER BY name
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                UserRow row = new UserRow();

                row.userId = rs.getInt("user_id");
                row.name = rs.getString("name");
                row.email = rs.getString("email");
                row.phone = rs.getString("phone");
                row.active = rs.getBoolean("is_active");
                row.role = "TEACHER";

                teachers.add(row);
            }
        }

        request.setAttribute("teachers", teachers);
    }

    private void loadStudents(HttpServletRequest request)
            throws SQLException {

        List<UserRow> students = new ArrayList<>();

        String sql = """
                SELECT
                    u.user_id,
                    u.name,
                    u.email,
                    u.phone,
                    u.is_active,
                    u.teacher_id,
                    t.name AS teacher_name
                FROM users u
                LEFT JOIN users t
                    ON u.teacher_id = t.user_id
                WHERE u.role = 'STUDENT'
                ORDER BY u.name
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                UserRow row = new UserRow();

                row.userId = rs.getInt("user_id");
                row.name = rs.getString("name");
                row.email = rs.getString("email");
                row.phone = rs.getString("phone");
                row.active = rs.getBoolean("is_active");
                row.role = "STUDENT";
                row.teacherId = (Integer) rs.getObject("teacher_id");
                row.teacherName = rs.getString("teacher_name");

                students.add(row);
            }
        }

        request.setAttribute("students", students);
    }

    private void loadClassrooms(HttpServletRequest request)
            throws SQLException {

        List<ClassroomRow> classrooms = new ArrayList<>();

        String sql = """
                SELECT
                    c.classroom_id,
                    c.class_name,
                    c.subject,
                    c.description,
                    c.owner_teacher_id,
                    t.name AS owner_teacher_name,
                    COUNT(DISTINCT e.student_id) AS enrollment_count
                FROM classrooms c
                LEFT JOIN users t
                    ON c.owner_teacher_id = t.user_id
                LEFT JOIN enrollments e
                    ON c.classroom_id = e.classroom_id
                GROUP BY
                    c.classroom_id,
                    c.class_name,
                    c.subject,
                    c.description,
                    c.owner_teacher_id,
                    t.name
                ORDER BY c.classroom_id DESC
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                ClassroomRow row = new ClassroomRow();

                row.classroomId = rs.getInt("classroom_id");
                row.className = rs.getString("class_name");
                row.subject = rs.getString("subject");
                row.description = rs.getString("description");
                row.ownerTeacherId =
                        (Integer) rs.getObject("owner_teacher_id");
                row.ownerTeacherName =
                        rs.getString("owner_teacher_name");
                row.enrollmentCount =
                        rs.getInt("enrollment_count");

                classrooms.add(row);
            }
        }

        request.setAttribute("classrooms", classrooms);
    }

    private void loadAssignments(HttpServletRequest request)
            throws SQLException {

        List<AssignmentRow> assignments = new ArrayList<>();

        String sql = """
                SELECT
                    a.assignment_id,
                    a.classroom_id,
                    a.title,
                    a.description,
                    a.due_date,
                    c.class_name AS classroom_title
                FROM assignments a
                LEFT JOIN classrooms c
                    ON a.classroom_id = c.classroom_id
                ORDER BY a.assignment_id DESC
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                AssignmentRow row = new AssignmentRow();

                row.assignmentId = rs.getInt("assignment_id");
                row.classroomId = rs.getInt("classroom_id");
                row.title = rs.getString("title");
                row.description = rs.getString("description");
                row.dueDate = rs.getTimestamp("due_date");
                row.classroomTitle =
                        rs.getString("classroom_title");

                assignments.add(row);
            }
        }

        request.setAttribute("assignments", assignments);
    }

    private void loadSubmissions(HttpServletRequest request)
            throws SQLException {

        List<SubmissionRow> submissions = new ArrayList<>();

        String sql = """
                SELECT
                    s.submission_id,
                    s.assignment_id,
                    s.student_id,
                    s.answer,
                    s.submitted_at,
                    s.marks,
                    s.feedback,
                    u.name AS student_name,
                    u.email AS student_email,
                    a.title AS assignment_title,
                    c.class_name AS classroom_title
                FROM submissions s
                LEFT JOIN users u
                    ON s.student_id = u.user_id
                LEFT JOIN assignments a
                    ON s.assignment_id = a.assignment_id
                LEFT JOIN classrooms c
                    ON a.classroom_id = c.classroom_id
                ORDER BY s.submission_id DESC
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                SubmissionRow row = new SubmissionRow();

                row.submissionId =
                        rs.getInt("submission_id");

                row.assignmentId =
                        rs.getInt("assignment_id");

                row.studentId =
                        rs.getInt("student_id");

                row.answer =
                        rs.getString("answer");

                row.submittedAt =
                        rs.getTimestamp("submitted_at");

                row.marks =
                        (Integer) rs.getObject("marks");

                row.feedback =
                        rs.getString("feedback");

                row.studentName =
                        rs.getString("student_name");

                row.studentEmail =
                        rs.getString("student_email");

                row.assignmentTitle =
                        rs.getString("assignment_title");

                row.classroomTitle =
                        rs.getString("classroom_title");

                submissions.add(row);
            }
        }

        request.setAttribute("submissions", submissions);
    }

    private void loadOnlineClasses(HttpServletRequest request)
            throws SQLException {

        List<OnlineClassRow> onlineClasses = new ArrayList<>();

        String sql = """
                SELECT
                    oc.online_class_id,
                    oc.classroom_id,
                    oc.teacher_id,
                    oc.topic,
                    oc.class_date,
                    oc.start_time,
                    oc.end_time,
                    oc.meeting_link,
                    c.class_name AS classroom_title,
                    u.name AS teacher_name
                FROM online_classes oc
                LEFT JOIN classrooms c
                    ON oc.classroom_id = c.classroom_id
                LEFT JOIN users u
                    ON oc.teacher_id = u.user_id
                ORDER BY
                    oc.class_date DESC,
                    oc.start_time DESC
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                OnlineClassRow row = new OnlineClassRow();

                row.onlineClassId =
                        rs.getInt("online_class_id");

                row.classroomId =
                        rs.getInt("classroom_id");

                row.teacherId =
                        rs.getInt("teacher_id");

                row.topic =
                        rs.getString("topic");

                row.classDate =
                        rs.getDate("class_date");

                row.startTime =
                        rs.getTime("start_time");

                row.endTime =
                        rs.getTime("end_time");

                row.meetingLink =
                        rs.getString("meeting_link");

                row.classroomTitle =
                        rs.getString("classroom_title");

                row.teacherName =
                        rs.getString("teacher_name");

                onlineClasses.add(row);
            }
        }

        request.setAttribute("onlineClasses", onlineClasses);
    }

    /*
     * IMPORTANT:
     * This now uses your actual audit_logs schema:
     *
     * audit_id
     * user_id
     * action_type
     * entity_type
     * entity_id
     * description
     * created_at
     */
    private void loadAuditLogs(HttpServletRequest request)
            throws SQLException {

        List<AuditRow> auditLogs = new ArrayList<>();

        String sql = """
                SELECT
                    al.audit_id,
                    al.user_id,
                    al.action_type,
                    al.entity_type,
                    al.entity_id,
                    al.description,
                    al.created_at,
                    u.name AS user_name
                FROM audit_logs al
                LEFT JOIN users u
                    ON al.user_id = u.user_id
                ORDER BY al.created_at DESC
                LIMIT 200
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                AuditRow row = new AuditRow();

                row.auditId =
                        rs.getLong("audit_id");

                row.userId =
                        (Integer) rs.getObject("user_id");

                row.actionType =
                        rs.getString("action_type");

                row.entityType =
                        rs.getString("entity_type");

                row.entityId =
                        (Integer) rs.getObject("entity_id");

                row.description =
                        rs.getString("description");

                row.createdAt =
                        rs.getTimestamp("created_at");

                row.userName =
                        rs.getString("user_name");

                auditLogs.add(row);
            }
        }

        request.setAttribute("auditLogs", auditLogs);
    }

    // =========================================================
    // USER MANAGEMENT
    // =========================================================

    private void updateUser(HttpServletRequest request,
                            HttpServletResponse response)
            throws SQLException, IOException {

        int userId = parseInt(request.getParameter("userId"));
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String role = request.getParameter("role");
        String teacherIdParam = request.getParameter("teacherId");

        if (name == null || name.isBlank()
                || email == null || email.isBlank()) {

            redirectWithMessage(request, response,
                    "Name and email are required.");
            return;
        }

        role = role == null ? "" : role.toUpperCase();

        if (!role.equals("STUDENT")
                && !role.equals("TEACHER")
                && !role.equals("ADMIN")) {

            redirectWithMessage(request, response,
                    "Invalid role.");
            return;
        }

        Integer teacherId = null;

        if ("STUDENT".equals(role)
                && teacherIdParam != null
                && !teacherIdParam.isBlank()) {

            teacherId = parseInt(teacherIdParam);
        }

        int currentAdminId = getSessionUserId(request);

        if (userId == currentAdminId
                && !"ADMIN".equals(role)) {

            redirectWithMessage(request, response,
                    "You cannot change your own admin role.");
            return;
        }

        String sql = """
                UPDATE users
                SET name = ?,
                    email = ?,
                    phone = ?,
                    role = ?,
                    teacher_id = ?
                WHERE user_id = ?
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, email.trim());
            ps.setString(3,
                    phone == null || phone.isBlank()
                            ? null
                            : phone.trim());

            ps.setString(4, role);

            if (teacherId == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, teacherId);
            }

            ps.setInt(6, userId);

            ps.executeUpdate();
        }

        audit(request,
                "UPDATE_USER",
                "USER",
                userId,
                "Updated user account.");

        redirectWithMessage(request, response,
                "User updated successfully.");
    }

    private void toggleUser(HttpServletRequest request,
                            HttpServletResponse response)
            throws SQLException, IOException {

        int userId = parseInt(request.getParameter("userId"));

        if (userId == getSessionUserId(request)) {
            redirectWithMessage(request, response,
                    "You cannot deactivate your own account.");
            return;
        }

        String sql = """
                UPDATE users
                SET is_active = NOT is_active
                WHERE user_id = ?
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        }

        audit(request,
                "TOGGLE_USER",
                "USER",
                userId,
                "Changed user active status.");

        redirectWithMessage(request, response,
                "User status updated.");
    }

    private void resetPassword(HttpServletRequest request,
                               HttpServletResponse response)
            throws SQLException, IOException {

        int userId = parseInt(request.getParameter("userId"));
        String newPassword = request.getParameter("newPassword");

        if (newPassword == null || newPassword.length() < 6) {
            redirectWithMessage(request, response,
                    "Password must contain at least 6 characters.");
            return;
        }

        String hash = Password.hash(newPassword);

        String sql = """
                UPDATE users
                SET password = ?
                WHERE user_id = ?
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, hash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }

        audit(request,
                "RESET_PASSWORD",
                "USER",
                userId,
                "Admin reset user password.");

        redirectWithMessage(request, response,
                "Password reset successfully.");
    }

    private void deleteUser(HttpServletRequest request,
                            HttpServletResponse response)
            throws SQLException, IOException {

        int userId = parseInt(request.getParameter("userId"));

        if (userId == getSessionUserId(request)) {
            redirectWithMessage(request, response,
                    "You cannot delete your own account.");
            return;
        }

        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        }

        audit(request,
                "DELETE_USER",
                "USER",
                userId,
                "Deleted user account.");

        redirectWithMessage(request, response,
                "User deleted successfully.");
    }

    // =========================================================
    // CLASSROOM MANAGEMENT
    // =========================================================

    private void createClassroom(HttpServletRequest request,
                                 HttpServletResponse response)
            throws SQLException, IOException {

        String className = request.getParameter("className");
        String subject = request.getParameter("subject");
        String description = request.getParameter("description");
        String ownerTeacherParam =
                request.getParameter("ownerTeacherId");

        if (className == null || className.isBlank()
                || subject == null || subject.isBlank()) {

            redirectWithMessage(request, response,
                    "Class name and subject are required.");
            return;
        }

        Integer ownerTeacherId = null;

        if (ownerTeacherParam != null
                && !ownerTeacherParam.isBlank()) {

            ownerTeacherId = parseInt(ownerTeacherParam);
        }

        int classroomId;

        String sql = """
                INSERT INTO classrooms
                    (class_name, subject, description, owner_teacher_id)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, className.trim());
            ps.setString(2, subject.trim());
            ps.setString(3,
                    description == null
                            ? null
                            : description.trim());

            if (ownerTeacherId == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, ownerTeacherId);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "Unable to create classroom.");
                }

                classroomId = rs.getInt(1);
            }
        }

        if (ownerTeacherId != null) {
            addTeacherToClassroom(
                    ownerTeacherId,
                    classroomId);
        }

        audit(request,
                "CREATE_CLASSROOM",
                "CLASSROOM",
                classroomId,
                "Created classroom: " + className);

        redirectWithMessage(request, response,
                "Classroom created successfully.");
    }

    private void updateClassroom(HttpServletRequest request,
                                 HttpServletResponse response)
            throws SQLException, IOException {

        int classroomId =
                parseInt(request.getParameter("classroomId"));

        String className =
                request.getParameter("className");

        String subject =
                request.getParameter("subject");

        String description =
                request.getParameter("description");

        String ownerTeacherParam =
                request.getParameter("ownerTeacherId");

        if (className == null || className.isBlank()
                || subject == null || subject.isBlank()) {

            redirectWithMessage(request, response,
                    "Class name and subject are required.");
            return;
        }

        Integer ownerTeacherId = null;

        if (ownerTeacherParam != null
                && !ownerTeacherParam.isBlank()) {

            ownerTeacherId = parseInt(ownerTeacherParam);
        }

        String sql = """
                UPDATE classrooms
                SET class_name = ?,
                    subject = ?,
                    description = ?,
                    owner_teacher_id = ?
                WHERE classroom_id = ?
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, className.trim());
            ps.setString(2, subject.trim());
            ps.setString(3,
                    description == null
                            ? null
                            : description.trim());

            if (ownerTeacherId == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, ownerTeacherId);
            }

            ps.setInt(5, classroomId);

            ps.executeUpdate();
        }

        if (ownerTeacherId != null) {
            addTeacherToClassroom(
                    ownerTeacherId,
                    classroomId);
        }

        audit(request,
                "UPDATE_CLASSROOM",
                "CLASSROOM",
                classroomId,
                "Updated classroom: " + className);

        redirectWithMessage(request, response,
                "Classroom updated successfully.");
    }

    private void deleteClassroom(HttpServletRequest request,
                                 HttpServletResponse response)
            throws SQLException, IOException {

        int classroomId =
                parseInt(request.getParameter("classroomId"));

        String sql =
                "DELETE FROM classrooms WHERE classroom_id = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, classroomId);
            ps.executeUpdate();
        }

        audit(request,
                "DELETE_CLASSROOM",
                "CLASSROOM",
                classroomId,
                "Deleted classroom.");

        redirectWithMessage(request, response,
                "Classroom deleted successfully.");
    }

    private void addTeacherToClassroom(int teacherId,
                                       int classroomId)
            throws SQLException {

        String sql = """
                INSERT IGNORE INTO teacher_classroom
                    (teacher_id, classroom_id)
                VALUES (?, ?)
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, teacherId);
            ps.setInt(2, classroomId);
            ps.executeUpdate();
        }
    }

    // =========================================================
    // ENROLLMENT MANAGEMENT
    // =========================================================

    private void enrollStudent(HttpServletRequest request,
                               HttpServletResponse response)
            throws SQLException, IOException {

        int studentId =
                parseInt(request.getParameter("studentId"));

        int classroomId =
                parseInt(request.getParameter("classroomId"));

        String sql = """
                INSERT IGNORE INTO enrollments
                    (student_id, classroom_id)
                VALUES (?, ?)
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, classroomId);
            ps.executeUpdate();
        }

        audit(request,
                "ENROLL_STUDENT",
                "ENROLLMENT",
                classroomId,
                "Enrolled student ID " + studentId
                        + " into classroom.");

        redirectWithMessage(request, response,
                "Student enrolled successfully.");
    }

    private void removeEnrollment(HttpServletRequest request,
                                  HttpServletResponse response)
            throws SQLException, IOException {

        int studentId =
                parseInt(request.getParameter("studentId"));

        int classroomId =
                parseInt(request.getParameter("classroomId"));

        String sql = """
                DELETE FROM enrollments
                WHERE student_id = ?
                  AND classroom_id = ?
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, classroomId);
            ps.executeUpdate();
        }

        audit(request,
                "REMOVE_ENROLLMENT",
                "ENROLLMENT",
                classroomId,
                "Removed student ID " + studentId
                        + " from classroom.");

        redirectWithMessage(request, response,
                "Enrollment removed.");
    }

    // =========================================================
    // CONTENT MANAGEMENT
    // =========================================================

    private void deleteAssignment(HttpServletRequest request,
                                  HttpServletResponse response)
            throws SQLException, IOException {

        int assignmentId =
                parseInt(request.getParameter("assignmentId"));

        String sql =
                "DELETE FROM assignments WHERE assignment_id = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, assignmentId);
            ps.executeUpdate();
        }

        audit(request,
                "DELETE_ASSIGNMENT",
                "ASSIGNMENT",
                assignmentId,
                "Deleted assignment.");

        redirectWithMessage(request, response,
                "Assignment deleted successfully.");
    }

    private void deleteSubmission(HttpServletRequest request,
                                  HttpServletResponse response)
            throws SQLException, IOException {

        int submissionId =
                parseInt(request.getParameter("submissionId"));

        String sql =
                "DELETE FROM submissions WHERE submission_id = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, submissionId);
            ps.executeUpdate();
        }

        audit(request,
                "DELETE_SUBMISSION",
                "SUBMISSION",
                submissionId,
                "Deleted submission.");

        redirectWithMessage(request, response,
                "Submission deleted successfully.");
    }

    private void deleteOnlineClass(HttpServletRequest request,
                                   HttpServletResponse response)
            throws SQLException, IOException {

        int onlineClassId =
                parseInt(request.getParameter("onlineClassId"));

        String sql =
                "DELETE FROM online_classes WHERE online_class_id = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, onlineClassId);
            ps.executeUpdate();
        }

        audit(request,
                "DELETE_ONLINE_CLASS",
                "ONLINE_CLASS",
                onlineClassId,
                "Deleted online class.");

        redirectWithMessage(request, response,
                "Online class deleted successfully.");
    }

    // =========================================================
    // AUDIT LOGGING
    // =========================================================

    private void audit(HttpServletRequest request,
                       String actionType,
                       String entityType,
                       Integer entityId,
                       String description)
            throws SQLException {

        int userId = getSessionUserId(request);

        String sql = """
                INSERT INTO audit_logs
                    (
                        user_id,
                        action_type,
                        entity_type,
                        entity_id,
                        description
                    )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, actionType);
            ps.setString(3, entityType);

            if (entityId == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, entityId);
            }

            ps.setString(5, description);

            ps.executeUpdate();
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private int getSessionUserId(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new IllegalStateException(
                    "Admin session not found.");
        }

        Object value = session.getAttribute("userId");

        if (value instanceof Integer integer) {
            return integer;
        }

        if (value instanceof String string) {
            return parseInt(string);
        }

        throw new IllegalStateException(
                "Admin user ID not found in session.");
    }

    private int parseInt(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Required numeric value is missing.");
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid numeric value: " + value);
        }
    }

    private void redirectWithMessage(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws IOException {

        String encoded = URLEncoder
                .encode(message, StandardCharsets.UTF_8)
                .replace("+", "%20");

        response.sendRedirect(
                request.getContextPath()
                        + "/admin?message="
                        + encoded);
    }

    private String safeMessage(Exception e) {

        String message = e.getMessage();

        if (message == null || message.isBlank()) {
            return e.getClass().getSimpleName();
        }

        return message
                .replace("\r", " ")
                .replace("\n", " ");
    }

    // =========================================================
    // ROW CLASSES
    // =========================================================

    public static class UserRow {

        public int userId;
        public String name;
        public String email;
        public String role;
        public String phone;
        public boolean active;
        public Integer teacherId;
        public String teacherName;
        public Timestamp regDate;
    }

    public static class ClassroomRow {

        public int classroomId;
        public String className;
        public String subject;
        public String description;
        public Integer ownerTeacherId;
        public String ownerTeacherName;
        public int enrollmentCount;
    }

    public static class AssignmentRow {

        public int assignmentId;
        public int classroomId;
        public String title;
        public String description;
        public Timestamp dueDate;
        public String classroomTitle;
    }

    public static class SubmissionRow {

        public int submissionId;
        public int assignmentId;
        public int studentId;
        public String answer;
        public Timestamp submittedAt;
        public Integer marks;
        public String feedback;
        public String studentName;
        public String studentEmail;
        public String assignmentTitle;
        public String classroomTitle;
    }

    public static class OnlineClassRow {

        public int onlineClassId;
        public int classroomId;
        public int teacherId;
        public String topic;
        public Date classDate;
        public Time startTime;
        public Time endTime;
        public String meetingLink;
        public String classroomTitle;
        public String teacherName;
    }

    public static class AuditRow {

        public long auditId;
        public Integer userId;
        public String actionType;
        public String entityType;
        public Integer entityId;
        public String description;
        public Timestamp createdAt;
        public String userName;
    }
}