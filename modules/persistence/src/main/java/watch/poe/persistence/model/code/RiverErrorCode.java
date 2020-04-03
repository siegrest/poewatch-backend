package watch.poe.persistence.model.code;

import lombok.Getter;

@Getter
public enum RiverErrorCode {
  READ_TIMEOUT,
  CONNECT_TIMEOUT,
  CONNECTION_RESET,
  HTTP_5XX,
  HTTP_4XX,
  UNKNOWN
}
