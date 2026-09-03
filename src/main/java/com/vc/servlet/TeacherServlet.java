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

@WebServlet("/teacher-list")
public class TeacherServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        List<String[]> teachers =
                new ArrayList<>();

        String sql =
                "SELECT user_id, name " +
                "FROM users " +
                "WHERE role='TEACHER' " +
                "ORDER BY name";

        try (
            Connection conn = DB.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {

            while (rs.next()) {

                teachers.add(
                        new String[]{
                                rs.getString("user_id"),
                                rs.getString("name")
                        }
                );
            }

        } catch (Exception e) {

            throw new ServletException(
                    "Failed to load teachers.",
                    e
            );
        }

        request.setAttribute(
                "teachers",
                teachers
        );

        request.getRequestDispatcher("/register.jsp").forward(
                request,
                response
        );
    }
}