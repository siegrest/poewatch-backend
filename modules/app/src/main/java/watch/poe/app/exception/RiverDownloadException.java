package watch.poe.app.exception;

import lombok.Getter;
import watch.poe.persistence.model.code.RiverErrorCode;

import java.util.regex.Pattern;

public class RiverDownloadException extends RuntimeException {

  private static final Pattern exceptionPattern5xx = Pattern.compile("^.+ 5\\d\\d .+$");
  private static final Pattern exceptionPattern4xx = Pattern.compile("^.+ 4\\d\\d .+$");

  @Getter
  private RiverErrorCode basis;

  public RiverDownloadException(RiverErrorCode basis) {
    super(basis.name());
    this.basis = basis;
  }

  public RiverDownloadException(Exception ex) {
    super(ex);
    this.basis = getErrorType(ex);
  }

  private RiverErrorCode getErrorType(Exception ex) {
    if (ex.getMessage().contains("Read timed out")) {
      return RiverErrorCode.READ_TIMEOUT;
    }

    if (ex.getMessage().contains("connect timed out")) {
      return RiverErrorCode.CONNECT_TIMEOUT;
    }

    if (ex.getMessage().contains("Connection reset")) {
      return RiverErrorCode.CONNECTION_RESET;
    }

    if (exceptionPattern5xx.matcher(ex.getMessage()).matches()) {
      return RiverErrorCode.HTTP_5XX;
    }

    if (exceptionPattern4xx.matcher(ex.getMessage()).matches()) {
      return RiverErrorCode.HTTP_4XX;
    }

    return RiverErrorCode.UNKNOWN;
  }

}
