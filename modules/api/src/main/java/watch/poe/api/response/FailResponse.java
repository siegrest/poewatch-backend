package watch.poe.api.response;

import lombok.Getter;

@Getter
public class FailResponse<T> extends BaseResponse<T> {
    private String status = "fail";

    public FailResponse(T data) {
        super(data);
    }
}
