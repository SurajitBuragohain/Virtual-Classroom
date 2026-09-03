package com.vc.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.vc.util.DB;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/classrooms")
public class ClassroomManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null ||
            session.getAttribute("role") == null) {

            response.sendRedirect(request.getContextPath() + "/index.jsp");
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
        String role = (String) session.getAttribute("role");

        if ("ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        if ("TEACHER".equalsIgnoreCase(role)) {
            loadTeacherClassrooms(request, userId);
            request.setAttribute("pageTitle", "Manage Classrooms");
        } else if ("STUDENT".equalsIgnoreCase(role)) {
            loadStudentClassrooms(request, userId);
            request.setAttribute("pageTitle", "My Classrooms");
        } else {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Invalid user role."
            );
            return;
        }

        request.getRequestDispatcher("/classrooms.jsp")
                .forward(request, response);
    }

    private void loadTeacherClassrooms(
            HttpServletRequest request,
            int teacherId)
            throws ServletException {

        List<ClassroomRow> classrooms = new ArrayList<>();

        String sql =
                "SELECT c.classroom_id, c.class_name, c.subject, c.description, " +
                "COUNT(DISTINCT e.student_id) AS student_count " +
                "FROM classrooms c " +
                "INNER JOIN teacher_classroom tc " +
                "ON c.classroom_id = tc.classroom_id " +
                "LEFT JOIN enrollments e " +
                "ON c.classroom_id = e.classroom_id " +
                "WHERE tc.teacher_id = ? " +
                "GROUP BY c.classroom_id, c.class_name, c.subject, c.description " +
                "ORDER BY c.class_name";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, teacherId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    classrooms.add(
                            new ClassroomRow(
                                    rs.getInt("classroom_id"),
                                    rs.getString("class_name"),
                                    rs.getString("subject"),
                                    rs.getString("description"),
                                    rs.getInt("student_count")
                            )
                    );
                }
            }

            request.setAttribute("classrooms", classrooms);

        } catch (Exception e) {
            throw new ServletException(
                    "Unable to load teacher classrooms.",
                    e
            );
        }
    }

    private void loadStudentClassrooms(
            HttpServletRequest request,
            int studentId)
            throws ServletException {

        List<ClassroomRow> classrooms = new ArrayList<>();

        String sql =
                "SELECT c.classroom_id, c.class_name, c.subject, c.description, " +
                "COUNT(DISTINCT e2.student_id) AS student_count " +
                "FROM classrooms c " +
                "INNER JOIN enrollments e " +
                "ON c.classroom_id = e.classroom_id " +
                "LEFT JOIN enrollments e2 " +
                "ON c.classroom_id = e2.classroom_id " +
                "WHERE e.student_id = ? " +
                "GROUP BY c.classroom_id, c.class_name, c.subject, c.description " +
                "ORDER BY c.class_name";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    classrooms.add(
                            new ClassroomRow(
                                    rs.getInt("classroom_id"),
                                    rs.getString("class_name"),
                                    rs.getString("subject"),
                                    rs.getString("description"),
                                    rs.getInt("student_count")
                            )
                    );
                }
            }

            request.setAttribute("classrooms", classrooms);

        } catch (Exception e) {
            throw new ServletException(
                    "Unable to load student classrooms.",
                    e
            );
        }
    }

    public static class ClassroomRow {
        public final int classroomId;
        public final String className;
        public final String subject;
        public final String description;
        public final int studentCount;

        public ClassroomRow(
                int classroomId,
                String className,
                String subject,
                String description,
                int studentCount) {

            this.classroomId = classroomId;
            this.className = className;
            this.subject = subject;
            this.description = description;
            this.studentCount = studentCount;
        }
    }
}
