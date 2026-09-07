package com.vc.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.vc.util.DB;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/pdf-download")
public class PdfDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // User must be logged in
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Please log in to access this file."
            );
            return;
        }

        Integer userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (role == null) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "User role is not available."
            );
            return;
        }

        String submissionIdParam = request.getParameter("submissionId");

        if (submissionIdParam == null || submissionIdParam.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Submission ID is required."
            );
            return;
        }

        int submissionId;

        try {
            submissionId = Integer.parseInt(submissionIdParam);
        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid submission ID."
            );
            return;
        }

        try (Connection connection = DB.getConnection()) {

            String sql;

            /*
             * STUDENT:
             * A student can access only their own submission.
             *
             * TEACHER:
             * A teacher can access submissions belonging to
             * classrooms assigned to that teacher.
             *
             * ADMIN:
             * Admin can access any submission.
             */
            if ("STUDENT".equalsIgnoreCase(role)) {

                sql = """
                    SELECT
                        s.pdf_file_name,
                        s.pdf_content_type,
                        s.pdf_data
                    FROM submissions s
                    WHERE s.submission_id = ?
                      AND s.student_id = ?
                    """;

            } else if ("TEACHER".equalsIgnoreCase(role)) {

                sql = """
                    SELECT
                        s.pdf_file_name,
                        s.pdf_content_type,
                        s.pdf_data
                    FROM submissions s
                    INNER JOIN assignments a
                        ON s.assignment_id = a.assignment_id
                    INNER JOIN teacher_classroom tc
                        ON a.classroom_id = tc.classroom_id
                    WHERE s.submission_id = ?
                      AND tc.teacher_id = ?
                    """;

            } else if ("ADMIN".equalsIgnoreCase(role)) {

                sql = """
                    SELECT
                        s.pdf_file_name,
                        s.pdf_content_type,
                        s.pdf_data
                    FROM submissions s
                    WHERE s.submission_id = ?
                    """;

            } else {
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "You are not authorized to access this file."
                );
                return;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, submissionId);

                if ("STUDENT".equalsIgnoreCase(role)
                        || "TEACHER".equalsIgnoreCase(role)) {
                    statement.setInt(2, userId);
                }

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (!resultSet.next()) {
                        response.sendError(
                                HttpServletResponse.SC_FORBIDDEN,
                                "You are not authorized to access this submission."
                        );
                        return;
                    }

                    String fileName = resultSet.getString("pdf_file_name");
                    String contentType =
                            resultSet.getString("pdf_content_type");

                    InputStream pdfInputStream =
                            resultSet.getBinaryStream("pdf_data");

                    if (pdfInputStream == null) {
                        response.sendError(
                                HttpServletResponse.SC_NOT_FOUND,
                                "PDF file is not available."
                        );
                        return;
                    }

                    if (fileName == null || fileName.isBlank()) {
                        fileName = "assignment-submission.pdf";
                    }

                    // Prevent unsafe characters in HTTP headers.
                    fileName = sanitizeFileName(fileName);

                    if (contentType == null || contentType.isBlank()) {
                        contentType = "application/pdf";
                    }

                    response.setContentType(contentType);
                    response.setHeader(
                            "Content-Disposition",
                            "inline; filename=\"" + fileName + "\""
                    );

                    response.setHeader(
                            "X-Content-Type-Options",
                            "nosniff"
                    );

                    try (InputStream input = pdfInputStream;
                         OutputStream output = response.getOutputStream()) {

                        byte[] buffer = new byte[8192];
                        int bytesRead;

                        while ((bytesRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }

                        output.flush();
                    }
                }
            }

        } catch (SQLException e) {

            getServletContext().log(
                    "Error downloading PDF submission: "
                            + submissionId,
                    e
            );

            if (!response.isCommitted()) {
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Unable to download the PDF file."
                );
            }
        }
    }

    /**
     * Removes characters that could cause problems in the
     * Content-Disposition HTTP header.
     */
    private String sanitizeFileName(String fileName) {

        fileName = fileName
                .replace("\\", "_")
                .replace("/", "_")
                .replace("\"", "_")
                .replace("\r", "_")
                .replace("\n", "_");

        if (fileName.isBlank()) {
            return "assignment-submission.pdf";
        }

        return fileName;
    }
}