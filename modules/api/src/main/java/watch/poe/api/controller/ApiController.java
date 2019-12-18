package watch.poe.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import watch.poe.api.response.FailResponse;
import watch.poe.api.service.ApiService;
import watch.poe.persistence.model.League;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
public class ApiController {

    // todo: set up caching
    // todo: add stats repository

    @Autowired
    private ApiService apiService;

    @ResponseBody
    @RequestMapping(method = {RequestMethod.GET}, value = "leagues", produces = "application/json")
    public List<League> getLeagues(HttpServletRequest request) {
        log.info("GET request on '{}' from {}", request.getServletPath(), request.getRemoteAddr());
        return apiService.leagueRepository.findAll();
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public FailResponse<Object> handleException(Throwable throwable) {
        String finalMessage = throwable instanceof MethodArgumentTypeMismatchException
                ? "Invalid argument type. Expected 'values' of type int[]."
                : throwable.getMessage();

        log.error(finalMessage);

        return new FailResponse<>(new Object() {
            public String message = finalMessage;
        });
    }
}
