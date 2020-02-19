package watch.poe.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import watch.poe.api.response.ErrorResponse;
import watch.poe.api.response.FailResponse;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {
    @RequestMapping(value = "error", produces = "application/json")
    @ResponseBody
    public Object error(HttpServletRequest request, HttpServletResponse response) {
        Exception ex = (Exception) request.getAttribute("javax.servlet.error.exception");
        int errorCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String message = ex != null
                ? ex.getMessage()
                : (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        if (message.isEmpty() && errorCode == 404) {
            message = "Requested resource was not found.";
        } else if (message.isEmpty()) {
            message = "Unexpected error.";
        }

        log.error(message);
        response.setStatus(errorCode);

        if (errorCode >= 500) {
            return new ErrorResponse<>(message, errorCode, null);
        }

        String finalMessage = message;
        return new FailResponse<Object>(new Object() {
            public String message = finalMessage;
        });
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
