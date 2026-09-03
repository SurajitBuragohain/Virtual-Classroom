package com.vc.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.vc.util.DB;
import com.vc.util.Password;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String emailInput = request.getParameter("email");
        String passwordInput = request.getParameter("password");

        if (emailInput == null ||
            passwordInput == null ||
            emailInput.isBlank() ||
            passwordInput.isBlank()) {

            response.sendRedirect(
                    "index.jsp?error=Missing%20credentials"
            );
            return;
        }

        String cleanEmail =
                emailInput.trim().toLowerCase();

        String sql =
                "SELECT user_id, name, email, role, password " +
                "FROM users WHERE email=?";

        try (
            Connection conn = DB.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, cleanEmail);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    response.sendRedirect(
                            request.getContextPath() + "/index.jsp?error=Invalid%20login"
                    );
                    return;
                }

                String storedHash =
                        rs.getString("password");

                if (!Password.verify(passwordInput, storedHash)) {

                    response.sendRedirect(
                            request.getContextPath() + "/index.jsp?error=Invalid%20login"
                    );
                    return;
                }

                if (Password.isLegacyHash(storedHash)) {
                    try (PreparedStatement upgrade = conn.prepareStatement("UPDATE users SET password=? WHERE user_id=?")) {
                        upgrade.setString(1, Password.hash(passwordInput));
                        upgrade.setInt(2, rs.getInt("user_id"));
                        upgrade.executeUpdate();
                    }
                }

                HttpSession session =
                        request.getSession(true);

                session.setAttribute(
                        "userId",
                        rs.getInt("user_id")
                );

                session.setAttribute(
                        "name",
                        rs.getString("name")
                );

                session.setAttribute(
                        "email",
                        rs.getString("email")
                );

                session.setAttribute(
                        "role",
                        rs.getString("role")
                );

                response.sendRedirect(request.getContextPath() + "/dashboard");
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Database authentication failed.",
                    e
            );
        }
    }
}