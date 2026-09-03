package com.vc.servlet;

import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActionServletTest {

    private ActionServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        servlet = new ActionServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn("/virtual-classroom");
    }

    @Test
    void redirectsUnauthenticatedUser() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect(
                "/virtual-classroom/index.jsp?error=Please%20login%20first");
    }

    @Test
    void rejectsMissingAction() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn("TEACHER");
        when(request.getParameter("action")).thenReturn("");

        servlet.doPost(request, response);

        verify(response).sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Action is required.");
    }

    @Test
    void rejectsUnknownAction() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn("TEACHER");
        when(request.getParameter("action")).thenReturn("wrongAction");

        servlet.doPost(request, response);

        verify(response).sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Unknown action: wrongAction");
    }
}
