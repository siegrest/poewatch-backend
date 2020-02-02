package watch.poe.app.exception.river;

import lombok.Getter;

@Getter
public enum RiverDownloadBasis {
  READ_TIMEOUT,
  CONNECT_TIMEOUT,
  CONNECTION_RESET,
  HTTP_5XX,
  HTTP_4XX,
  UNKNOWN
}
