package org.logicahealth.sandboxmanagerapi.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;

public class GlobalControllerExceptionHandlerTest {

    @Autowired
    private GlobalControllerExceptionHandler globalControllerExceptionHandler;

    private HttpServletResponse response;
    private PrintWriter writer;

    @Before
    public void setup() {
        globalControllerExceptionHandler = new GlobalControllerExceptionHandler();
        response = mock(HttpServletResponse.class);
        writer = mock(PrintWriter.class);
    }

    @Test
    public void handleAuthorizationException() throws IOException {
        exceptionTest();
    }

    @Test
    public void handleExceptionTest() throws IOException {
       exceptionTest();
    }

    private void exceptionTest() throws IOException {
        when(response.getWriter()).thenReturn(writer);

        globalControllerExceptionHandler.handleAuthorizationException(response, null);
        verify(writer).write("Exception is null");

        Exception e = new Exception();
        globalControllerExceptionHandler.handleAuthorizationException(response, e);
        verify(writer).write("Exception has no message: " + e.toString());

        Exception mockException = mock(Exception.class);
        when(mockException.getMessage()).thenReturn("message");
        globalControllerExceptionHandler.handleAuthorizationException(response, mockException);
        verify(writer).write("message");
    }
}
