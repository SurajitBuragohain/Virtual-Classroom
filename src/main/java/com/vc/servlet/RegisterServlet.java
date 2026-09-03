package com.vc.servlet;

import com.vc.util.DB;
import com.vc.util.Password;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        String phone = request.getParameter("phone");
        String teacherIdParam = request.getParameter("teacherId");
        String context = request.getContextPath();

        if (blank(name) || blank(email) || blank(password) || blank(role)) {
            response.sendRedirect(context + "/teacher-list?error=All%20mandatory%20fields%20are%20required.");
            return;
        }

        String cleanRole = role.trim().toUpperCase();
        if (!"STUDENT".equals(cleanRole) && !"TEACHER".equals(cleanRole)) {
            response.sendRedirect(context + "/teacher-list?error=Invalid%20role.");
            return;
        }

        String cleanPhone = phone == null ? "" : phone.trim();
        if (!cleanPhone.isEmpty() && !cleanPhone.matches("\\d{10}")) {
            response.sendRedirect(context + "/teacher-list?error=Phone%20number%20must%20contain%20exactly%2010%20digits.");
            return;
        }

        Integer teacherId = null;
        if ("STUDENT".equals(cleanRole)) {
            if (blank(teacherIdParam)) {
                response.sendRedirect(context + "/teacher-list?error=Students%20must%20select%20a%20teacher.");
                return;
            }
            try {
                teacherId = Integer.valueOf(teacherIdParam);
            } catch (NumberFormatException e) {
                response.sendRedirect(context + "/teacher-list?error=Invalid%20teacher%20selection.");
                return;
            }
        }

        String sql = "INSERT INTO users(name,email,password,role,phone,teacher_id) VALUES(?,?,?,?,?,?)";

        try (Connection connection = DB.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name.trim());
            statement.setString(2, email.trim().toLowerCase());
            statement.setString(3, Password.hash(password));
            statement.setString(4, cleanRole);
            statement.setString(5, cleanPhone.isEmpty() ? null : cleanPhone);
            if (teacherId == null) {
                statement.setNull(6, java.sql.Types.INTEGER);
            } else {
                String teacherSql = "SELECT 1 FROM users WHERE user_id=? AND role='TEACHER'";
                try (PreparedStatement teacherStatement = connection.prepareStatement(teacherSql)) {
                    teacherStatement.setInt(1, teacherId);
                    try (ResultSet result = teacherStatement.executeQuery()) {
                        if (!result.next()) {
                            response.sendRedirect(context + "/teacher-list?error=Selected%20teacher%20does%20not%20exist.");
                            return;
                        }
                    }
                }
                statement.setInt(6, teacherId);
            }
            statement.executeUpdate();
            response.sendRedirect(context + "/index.jsp?msg=Registration%20successful.%20Please%20login.");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                response.sendRedirect(context + "/teacher-list?error=An%20account%20with%20that%20email%20already%20exists.");
            } else {
                getServletContext().log("Registration failed", e);
                response.sendRedirect(context + "/teacher-list?error=Database%20error%20during%20registration.");
            }
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
