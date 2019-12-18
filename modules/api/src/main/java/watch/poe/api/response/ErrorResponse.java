package watch.poe.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public class ErrorResponse<T> extends BaseResponse<T> {
    private String status = "error";

    @Setter
    @NonNull
    private String message;

    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer code;

    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public ErrorResponse(String message, Integer code, T data) {
        super(data);

        this.message = message;
        this.code = code;
    }
}
