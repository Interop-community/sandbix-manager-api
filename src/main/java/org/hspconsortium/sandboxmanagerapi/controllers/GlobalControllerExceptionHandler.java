package org.hspconsortium.sandboxmanagerapi.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseBody
    @ResponseStatus(code = org.springframework.http.HttpStatus.UNAUTHORIZED)
    public void handleAuthorizationException(HttpServletResponse response, Exception e) throws IOException {
        writeMessage(response, e);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleException(final HttpServletResponse response, Exception e) throws IOException {
        writeMessage(response, e);
    }

    private void writeMessage(final HttpServletResponse response, Exception e) throws IOException {
        PrintWriter writer = response.getWriter();
        if (writer != null) {
            if (e != null) {
                String message = e.getMessage();
                if (message != null) {
                    writer.write(message);
                } else {
                    writer.write("Exception has no message: " + e.toString());
                }
            } else {
                writer.write("Exception is null");
            }

        }
    }
}
