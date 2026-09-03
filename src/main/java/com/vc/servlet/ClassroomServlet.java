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

@WebServlet("/classroom")
public class ClassroomServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static class OnlineClass {
        public int id;
        public String topic;
        public String date;
        public String startTime;
        public String endTime;
        public String meetingLink;
        public String teacherName;
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session =
                req.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            resp.sendRedirect(
                    req.getContextPath() +
                    "/index.jsp"
            );

            return;
        }

        int userId =
                (Integer) session.getAttribute("userId");

        String role =
                (String) session.getAttribute("role");

        String idParameter =
                req.getParameter("id");

        if (idParameter == null ||
            idParameter.isBlank()) {

            resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Classroom ID is required."
            );

            return;
        }

        int classroomId;

        try {

            classroomId =
                    Integer.parseInt(
                            idParameter.trim()
                    );

        } catch (NumberFormatException e) {

            resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid classroom ID."
            );

            return;
        }

        try (Connection con =
                     DB.getConnection()) {

            if ("STUDENT".equals(role)) {

                if (!checkStudentAccess(
                        con,
                        userId,
                        classroomId
                )) {

                    resp.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "You are not enrolled in this classroom."
                    );

                    return;
                }

            } else if ("TEACHER".equals(role)) {

                if (!checkTeacherAccess(
                        con,
                        userId,
                        classroomId
                )) {

                    resp.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "You are not assigned to this classroom."
                    );

                    return;
                }

            } else {

                resp.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Invalid user role."
                );

                return;
            }

            String[] classroom =
                    getClassroom(
                            con,
                            classroomId
                    );

            if (classroom == null) {

                resp.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Classroom not found."
                );

                return;
            }

            List<String[]> teachers =
                    getTeachers(
                            con,
                            classroomId
                    );

            List<String[]> allTeachers =
                    getAllTeachers(con);

            List<String[]> students =
                    getStudents(
                            con,
                            classroomId
                    );

            List<String[]> assignments =
                    getAssignments(
                            con,
                            classroomId
                    );

            List<String[]> submissionStatus =
                    new ArrayList<>();

            List<String[]> studentSubmissions =
                    new ArrayList<>();

            if ("TEACHER".equals(role)) {

                submissionStatus =
                        getSubmissionStatus(
                                con,
                                classroomId
                        );

            } else if ("STUDENT".equals(role)) {

                studentSubmissions =
                        getStudentSubmissionStatus(
                                con,
                                classroomId,
                                userId
                        );
            }

            List<OnlineClass> onlineClasses =
                    getOnlineClasses(
                            con,
                            classroomId
                    );

            req.setAttribute(
                    "cid",
                    classroomId
            );

            req.setAttribute(
                    "classroom",
                    classroom
            );

            req.setAttribute(
                    "teachers",
                    teachers
            );

            req.setAttribute(
                    "allTeachers",
                    allTeachers
            );

            req.setAttribute(
                    "students",
                    students
            );

            req.setAttribute(
                    "assignments",
                    assignments
            );

            req.setAttribute(
                    "submissionStatus",
                    submissionStatus
            );

            req.setAttribute(
                    "studentSubmissions",
                    studentSubmissions
            );

            req.setAttribute(
                    "onlineClasses",
                    onlineClasses
            );

            req.getRequestDispatcher(
                    "/classroom.jsp"
            ).forward(req, resp);

        } catch (Exception e) {

            throw new ServletException(
                    "Error loading classroom.",
                    e
            );
        }
    }

    private boolean checkStudentAccess(
            Connection con,
            int studentId,
            int classroomId)
            throws Exception {

        String sql =
                "SELECT 1 " +
                "FROM enrollments " +
                "WHERE student_id=? " +
                "AND classroom_id=?";

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, classroomId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                return rs.next();
            }
        }
    }

    private boolean checkTeacherAccess(
            Connection con,
            int teacherId,
            int classroomId)
            throws Exception {

        String sql =
                "SELECT 1 " +
                "FROM teacher_classroom " +
                "WHERE teacher_id=? " +
                "AND classroom_id=?";

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, teacherId);
            ps.setInt(2, classroomId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                return rs.next();
            }
        }
    }

    private String[] getClassroom(
            Connection con,
            int classroomId)
            throws Exception {

        String sql =
                "SELECT classroom_id, " +
                "class_name, subject, description " +
                "FROM classrooms " +
                "WHERE classroom_id=?";

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, classroomId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (rs.next()) {

                    return new String[]{
                            rs.getString("classroom_id"),
                            rs.getString("class_name"),
                            rs.getString("subject"),
                            rs.getString("description")
                    };
                }
            }
        }

        return null;
    }

    private List<String[]> getTeachers(
            Connection con,
            int classroomId)
            throws Exception {

        List<String[]> teachers =
                new ArrayList<>();

        String sql =
                "SELECT u.user_id, " +
                "u.name, u.email " +
                "FROM users u " +
                "JOIN teacher_classroom tc " +
                "ON u.user_id=tc.teacher_id " +
                "WHERE tc.classroom_id=? " +
                "AND u.role='TEACHER' " +
                "ORDER BY u.name";

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, classroomId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    teachers.add(
                            new String[]{
                                    rs.getString("user_id"),
                                    rs.getString("name"),
                                    rs.getString("email")
                            }
                    );
                }
            }
        }

        return teachers;
    }

    private List<String[]> getAllTeachers(
            Connection con)
            throws Exception {

        List<String[]> allTeachers =
                new ArrayList<>();

        String sql =
                "SELECT user_id, name " +
                "FROM users " +
                "WHERE role='TEACHER' " +
                "ORDER BY name";

        try (
                PreparedStatement ps =
                        con.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                allTeachers.add(
                        new String[]{
                                rs.getString("user_id"),
                                rs.getString("name")
                        }
                );
            }
        }

        return allTeachers;
    }

    private List<String[]> getStudents(
            Connection con,
            int classroomId)
            throws Exception {

        List<String[]> students =
                new ArrayList<>();

        String sql =
                "SELECT u.user_id, " +
                "u.name, u.email " +
                "FROM users u " +
                "JOIN enrollments e " +
                "ON u.user_id=e.student_id " +
                "WHERE e.classroom_id=? " +
                "AND u.role='STUDENT' " +
                "ORDER BY u.name";

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, classroomId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    students.add(
                            new String[]{
                                    rs.getString("user_id"),
                                    rs.getString("name"),
                                    rs.getString("email")
                            }
                    );
                }
            }
        }

        return students;
    }

    private List<String[]> getAssignments(
            Connection con,
            int classroomId)
            throws Exception {

        List<String[]> assignments =
                new ArrayList<>();

        String sql =
                "SELECT assignment_id, " +
                "title, description, due_date " +
                "FROM assignments " +
                "WHERE classroom_id=? " +
                "ORDER BY due_date";

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, classroomId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    assignments.add(
                            new String[]{
                                    rs.getString("assignment_id"),
                                    rs.getString("title"),
                                    rs.getString("description"),
                                    rs.getString("due_date")
                            }
                    );
                }
            }
        }

        return assignments;
    }

    private List<String[]> getSubmissionStatus(
            Connection con,
            int classroomId)
            throws Exception {

        List<String[]> submissionStatus =
                new ArrayList<>();

        String sql =
                "SELECT " +
                "a.assignment_id, " +
                "a.title, " +
                "u.name AS student_name, " +
                "s.submitted_at, " +
                "s.answer, " +
                "s.marks, " +
                "s.feedback, " +
                "u.user_id AS student_id " +
                "FROM assignments a " +
                "JOIN enrollments e " +
                "ON a.classroom_id=e.classroom_id " +
                "JOIN users u " +
                "ON e.student_id=u.user_id " +
                "LEFT JOIN submissions s " +
                "ON s.assignment_id=a.assignment_id " +
                "AND s.student_id=u.user_id " +
                "WHERE a.classroom_id=? " +
                "AND u.role='STUDENT' " +
                "ORDER BY a.assignment_id, u.name";

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, classroomId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    String submittedAt = "";

                    if (rs.getTimestamp(
                            "submitted_at"
                    ) != null) {

                        submittedAt =
                                rs.getTimestamp(
                                        "submitted_at"
                                ).toString();
                    }

                    String answer =
                            rs.getString("answer");

                    if (answer == null) {
                        answer = "";
                    }

                    String marks = "";

                    if (rs.getObject("marks") != null) {

                        marks =
                                String.valueOf(
                                        rs.getInt("marks")
                                );
                    }

                    String feedback =
                            rs.getString("feedback");

                    if (feedback == null) {
                        feedback = "";
                    }

                    submissionStatus.add(
                            new String[]{
                                    rs.getString(
                                            "assignment_id"
                                    ),
                                    rs.getString(
                                            "title"
                                    ),
                                    rs.getString(
                                            "student_name"
                                    ),
                                    submittedAt,
                                    answer,
                                    marks,
                                    feedback,
                                    rs.getString(
                                            "student_id"
                                    )
                            }
                    );
                }
            }
        }

        return submissionStatus;
    }

    private List<String[]> getStudentSubmissionStatus(
            Connection con,
            int classroomId,
            int studentId)
            throws Exception {

        List<String[]> submissions =
                new ArrayList<>();

        String sql =
                "SELECT " +
                "a.assignment_id, " +
                "s.answer, " +
                "s.submitted_at, " +
                "s.marks, " +
                "s.feedback " +
                "FROM assignments a " +
                "LEFT JOIN submissions s " +
                "ON s.assignment_id=a.assignment_id " +
                "AND s.student_id=? " +
                "WHERE a.classroom_id=? " +
                "ORDER BY a.due_date, a.assignment_id";

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, classroomId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    String submittedAt = "";

                    if (rs.getTimestamp("submitted_at") != null) {
                        submittedAt =
                                rs.getTimestamp("submitted_at").toString();
                    }

                    String answer =
                            rs.getString("answer");

                    if (answer == null) {
                        answer = "";
                    }

                    String marks = "";

                    if (rs.getObject("marks") != null) {
                        marks =
                                String.valueOf(
                                        rs.getInt("marks")
                                );
                    }

                    String feedback =
                            rs.getString("feedback");

                    if (feedback == null) {
                        feedback = "";
                    }

                    submissions.add(
                            new String[]{
                                    rs.getString("assignment_id"),
                                    answer,
                                    submittedAt,
                                    marks,
                                    feedback
                            }
                    );
                }
            }
        }

        return submissions;
    }

    private List<OnlineClass> getOnlineClasses(
            Connection con,
            int classroomId)
            throws Exception {

        List<OnlineClass> onlineClasses =
                new ArrayList<>();

        String sql =
                "SELECT " +
                "oc.online_class_id, " +
                "oc.topic, " +
                "oc.class_date, " +
                "oc.start_time, " +
                "oc.end_time, " +
                "oc.meeting_link, " +
                "u.name AS teacher_name " +
                "FROM online_classes oc " +
                "JOIN users u " +
                "ON oc.teacher_id=u.user_id " +
                "WHERE oc.classroom_id=? " +
                "ORDER BY oc.class_date, oc.start_time";

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, classroomId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    OnlineClass onlineClass =
                            new OnlineClass();

                    onlineClass.id =
                            rs.getInt(
                                    "online_class_id"
                            );

                    onlineClass.topic =
                            rs.getString(
                                    "topic"
                            );

                    if (rs.getDate("class_date") != null) {

                        onlineClass.date =
                                rs.getDate(
                                        "class_date"
                                ).toString();

                    } else {

                        onlineClass.date = "";
                    }

                    if (rs.getTime("start_time") != null) {

                        onlineClass.startTime =
                                rs.getTime(
                                        "start_time"
                                ).toString();

                    } else {

                        onlineClass.startTime = "";
                    }

                    if (rs.getTime("end_time") != null) {

                        onlineClass.endTime =
                                rs.getTime(
                                        "end_time"
                                ).toString();

                    } else {

                        onlineClass.endTime = "";
                    }

                    onlineClass.meetingLink =
                            rs.getString(
                                    "meeting_link"
                            );

                    onlineClass.teacherName =
                            rs.getString(
                                    "teacher_name"
                            );

                    onlineClasses.add(
                            onlineClass
                    );
                }
            }
        }

        return onlineClasses;
    }
}