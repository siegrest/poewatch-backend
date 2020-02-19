package watch.poe.api.response;

import lombok.Getter;

@Getter
public class SuccessResponse<T> extends BaseResponse<T> {
    private String status = "success";

    public SuccessResponse(T data) {
        super(data);
    }
}
