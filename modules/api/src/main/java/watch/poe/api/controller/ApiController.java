package watch.poe.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import watch.poe.api.response.FailResponse;
import watch.poe.api.response.SuccessResponse;
import watch.poe.api.service.ApiService;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class ApiController {

    private final ApiService apiService;

    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @Cacheable("calculations")
    @ResponseBody
    @RequestMapping(
            value = "/calculate",
            method = {RequestMethod.GET, RequestMethod.POST},
            params = {"type=averageWithoutMinmax"}
    )
    public SuccessResponse<Double> getAverage(@RequestParam(value = "values") int[] values) {
        String loggedValues = Arrays.stream(values)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(", "));
        log.info("GET request on \"/calculate\" value=[" + loggedValues + "] was initiated.");

        double average = 5.8d;

        log.info("GET request on \"/calculate\" value=[" + loggedValues + "] resulted with " + average + ".");
        return new SuccessResponse<>(average);
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
