package watch.poe.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
abstract class BaseResponse<T> {
    private T data;
}
