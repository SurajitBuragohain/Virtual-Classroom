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

@WebServlet({"/dashboard", "/teacher"})
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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
            loadTeacherDashboard(request, userId);
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
            return;
        }

        if ("STUDENT".equalsIgnoreCase(role)) {
            loadStudentDashboard(request, userId);
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
            return;
        }

        response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Invalid user role."
        );
    }

    private void loadTeacherDashboard(
            HttpServletRequest request,
            int teacherId)
            throws ServletException {

        List<ClassroomRow> classrooms = new ArrayList<>();

        String sql =
                "SELECT c.classroom_id, c.class_name, c.subject, c.description " +
                "FROM classrooms c " +
                "INNER JOIN teacher_classroom tc " +
                "ON c.classroom_id = tc.classroom_id " +
                "WHERE tc.teacher_id = ? " +
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
                                    rs.getString("description")
                            )
                    );
                }
            }

            request.setAttribute("teacherClassrooms", classrooms);

        } catch (Exception e) {
            throw new ServletException(
                    "Unable to load teacher dashboard.",
                    e
            );
        }
    }

    private void loadStudentDashboard(
            HttpServletRequest request,
            int studentId)
            throws ServletException {

        List<ClassroomRow> classrooms = new ArrayList<>();

        String sql =
                "SELECT c.classroom_id, c.class_name, c.subject, c.description " +
                "FROM classrooms c " +
                "INNER JOIN enrollments e " +
                "ON c.classroom_id = e.classroom_id " +
                "WHERE e.student_id = ? " +
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
                                    rs.getString("description")
                            )
                    );
                }
            }

            request.setAttribute("studentClassrooms", classrooms);

        } catch (Exception e) {
            throw new ServletException(
                    "Unable to load student dashboard.",
                    e
            );
        }
    }

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
}
