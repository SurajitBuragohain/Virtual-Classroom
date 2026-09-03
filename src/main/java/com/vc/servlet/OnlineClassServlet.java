package com.vc.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;

import com.vc.util.DB;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/online-class")
public class OnlineClassServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        // Only logged-in teachers can create/delete online classes
      
        if (session == null ||
            session.getAttribute("userId") == null ||
            !"TEACHER".equals(session.getAttribute("role"))) {

            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only teachers can manage online classes."
            );
            return;
        }
        int teacherId;
        try {
            teacherId =
                    (Integer) session.getAttribute("userId");

        } catch (Exception e) {

            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Invalid teacher session."
            );
            return;
        }

        String action =
                req.getParameter("action");

        if ("create".equals(action)) {

            createOnlineClass(
                    req,
                    resp,
                    teacherId
            );

        } else if ("delete".equals(action)) {

            deleteOnlineClass(
                    req,
                    resp,
                    teacherId
            );

        } else {
            resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid action."
            );
        }
    }

    // CREATE ONLINE CLASS
   
    private void createOnlineClass(
            HttpServletRequest req,
            HttpServletResponse resp,
            int teacherId)
            throws ServletException, IOException {

        String classroomIdParam =
                req.getParameter("classroomId");

        String topic =
                req.getParameter("topic");

        String classDate =
                req.getParameter("classDate");

        String startTime =
                req.getParameter("startTime");

        String endTime =
                req.getParameter("endTime");

        String meetingLink =
                req.getParameter("meetingLink");

        // Validate classroom ID
       
        int classroomId;
        try {
                classroomId =
                    Integer.parseInt(classroomIdParam);

        } catch (Exception e) {

            resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid classroom ID."
            );

            return;
        }

        // Validate required fields
        
            if (topic == null ||
            topic.isBlank() ||
            classDate == null ||
            classDate.isBlank() ||
            startTime == null ||
            startTime.isBlank() ||
            meetingLink == null ||
            meetingLink.isBlank()) {

            resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "All required fields must be filled."
            );

            return;
        }

        // Validate date/time
        
        Date sqlDate;

        Time sqlStartTime;

        Time sqlEndTime = null;

        try {

            sqlDate =
                    Date.valueOf(classDate);

            sqlStartTime =
                    Time.valueOf(
                            convertToSqlTime(startTime)
                    );


            if (endTime != null &&
                !endTime.isBlank()) {

                sqlEndTime =
                        Time.valueOf(
                                convertToSqlTime(endTime)
                        );
            }

        } catch (IllegalArgumentException e) {

            resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid date or time."
            );

            return;
        }
        
        // Check start/end time

        if (sqlEndTime != null &&
            !sqlEndTime.after(sqlStartTime)) {

            resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "End time must be after start time."
            );

            return;
        }

        // Check teacher belongs to classroom

        String checkSql =
                "SELECT 1 " +
                "FROM teacher_classroom " +
                "WHERE teacher_id=? " +
                "AND classroom_id=?";


        // Insert online class

        String insertSql =
                "INSERT INTO online_classes " +
                "(classroom_id, teacher_id, topic, " +
                "class_date, start_time, end_time, meeting_link) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";


        try (
                Connection con =
                        DB.getConnection();

                PreparedStatement check =
                        con.prepareStatement(checkSql)
        ) {

            // Check teacher assignment

            check.setInt(
                    1,
                    teacherId
            );

            check.setInt(
                    2,
                    classroomId
            );

            try (
                    ResultSet rs =
                            check.executeQuery()
            ) {

                if (!rs.next()) {

                    resp.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "You are not assigned to this classroom."
                    );

                    return;
                }
            }

            // Insert
          
            try (
                    PreparedStatement ps =
                            con.prepareStatement(insertSql)
            ) {

                ps.setInt(
                        1,
                        classroomId
                );

                ps.setInt(
                        2,
                        teacherId
                );

                ps.setString(
                        3,
                        topic.trim()
                );

                ps.setDate(
                        4,
                        sqlDate
                );

                ps.setTime(
                        5,
                        sqlStartTime
                );

                if (sqlEndTime == null) {

                    ps.setNull(
                            6,
                            Types.TIME
                    );

                } else {

                    ps.setTime(
                            6,
                            sqlEndTime
                    );
                }
                ps.setString(
                        7,
                        meetingLink.trim()
                );
                ps.executeUpdate();
            }

            // Go back to classroom
            
            resp.sendRedirect(
                    req.getContextPath()
                    + "/classroom?id="
                    + classroomId
                    + "&msg=Online+class+scheduled"
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Error creating online class.",
                    e
            );
        }
    }

    // DELETE ONLINE CLASS
    
    private void deleteOnlineClass(
            HttpServletRequest req,
            HttpServletResponse resp,
            int teacherId)
            throws ServletException, IOException {

        String idParam =
                req.getParameter("onlineClassId");

        String classroomIdParam =
                req.getParameter("classroomId");

        int onlineClassId;
        int classroomId;

        try {

            onlineClassId =
                    Integer.parseInt(idParam);

            classroomId =
                    Integer.parseInt(classroomIdParam);

        } catch (Exception e) {

            resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid ID."
            );

            return;
        }

        // Delete only if this teacher owns/teaches this class
        
        String deleteSql =
                "DELETE FROM online_classes " +
                "WHERE online_class_id=? " +
                "AND classroom_id=? " +
                "AND teacher_id=?";

        try (
                Connection con =
                        DB.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(deleteSql)
        ) {

            ps.setInt(
                    1,
                    onlineClassId
            );

            ps.setInt(
                    2,
                    classroomId
            );

            ps.setInt(
                    3,
                    teacherId
            );

            int rows =
                    ps.executeUpdate();

            if (rows == 0) {

                resp.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "You cannot delete this online class."
                );

                return;
            }

            resp.sendRedirect(
                    req.getContextPath()
                    + "/classroom?id="
                    + classroomId
                    + "&msg=Online+class+deleted"
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Error deleting online class.",
                    e
            );
        }
    }

    // CONVERT HTML TIME TO SQL TIME
    
    private String convertToSqlTime(
            String htmlTime) {

        /*
         * HTML <input type="time"> normally sends:
         *
         * 21:39
         *
         * MySQL TIME needs:
         *
         * 21:39:00
         */

        if (htmlTime.matches(
                "^\\d{2}:\\d{2}$")) {

            return htmlTime + ":00";
        }

        return htmlTime;
    }
}