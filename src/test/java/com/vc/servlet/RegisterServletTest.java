package com.vc.servlet;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegisterServletTest {

    private RegisterServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        servlet = new RegisterServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn("/virtual-classroom");
    }

    @Test
    void rejectsMissingMandatoryFields() throws Exception {
        when(request.getParameter("name")).thenReturn("");
        when(request.getParameter("email")).thenReturn("student@example.com");
        when(request.getParameter("password")).thenReturn("Password@123");
        when(request.getParameter("role")).thenReturn("STUDENT");

        servlet.doPost(request, response);

        verify(response).sendRedirect(
                "/virtual-classroom/teacher-list?error=All%20mandatory%20fields%20are%20required.");
    }

    @Test
    void rejectsInvalidRole() throws Exception {
        when(request.getParameter("name")).thenReturn("Student");
        when(request.getParameter("email")).thenReturn("student@example.com");
        when(request.getParameter("password")).thenReturn("Password@123");
        when(request.getParameter("role")).thenReturn("ADMIN");

        servlet.doPost(request, response);

        verify(response).sendRedirect(
                "/virtual-classroom/teacher-list?error=Invalid%20role.");
    }

    @Test
    void rejectsPhoneThatIsNotExactlyTenDigits() throws Exception {
        when(request.getParameter("name")).thenReturn("Student");
        when(request.getParameter("email")).thenReturn("student@example.com");
        when(request.getParameter("password")).thenReturn("Password@123");
        when(request.getParameter("role")).thenReturn("TEACHER");
        when(request.getParameter("phone")).thenReturn("987654321");

        servlet.doPost(request, response);

        verify(response).sendRedirect(
                "/virtual-classroom/teacher-list?error=Phone%20number%20must%20contain%20exactly%2010%20digits.");
    }

    @Test
    void rejectsStudentWithoutAssignedTeacher() throws Exception {
        when(request.getParameter("name")).thenReturn("Student");
        when(request.getParameter("email")).thenReturn("student@example.com");
        when(request.getParameter("password")).thenReturn("Password@123");
        when(request.getParameter("role")).thenReturn("STUDENT");
        when(request.getParameter("phone")).thenReturn("9876543210");
        when(request.getParameter("teacherId")).thenReturn("");

        servlet.doPost(request, response);

        verify(response).sendRedirect(
                "/virtual-classroom/teacher-list?error=Students%20must%20select%20a%20teacher.");
    }
}
