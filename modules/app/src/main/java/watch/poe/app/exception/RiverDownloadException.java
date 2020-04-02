package watch.poe.app.exception;

import lombok.Getter;
import watch.poe.stats.model.code.RiverDownloadBasis;

import java.util.regex.Pattern;

public class RiverDownloadException extends RuntimeException {

  private static final Pattern exceptionPattern5xx = Pattern.compile("^.+ 5\\d\\d .+$");
  private static final Pattern exceptionPattern4xx = Pattern.compile("^.+ 4\\d\\d .+$");

  @Getter
  private RiverDownloadBasis basis;

  public RiverDownloadException(RiverDownloadBasis basis) {
    super(basis.name());
    this.basis = basis;
  }

  public RiverDownloadException(Exception ex) {
    super(ex);
    this.basis = getErrorType(ex);
  }

  private RiverDownloadBasis getErrorType(Exception ex) {
    if (ex.getMessage().contains("Read timed out")) {
      return RiverDownloadBasis.READ_TIMEOUT;
    }

    if (ex.getMessage().contains("connect timed out")) {
      return RiverDownloadBasis.CONNECT_TIMEOUT;
    }

    if (ex.getMessage().contains("Connection reset")) {
      return RiverDownloadBasis.CONNECTION_RESET;
    }

    if (exceptionPattern5xx.matcher(ex.getMessage()).matches()) {
      return RiverDownloadBasis.HTTP_5XX;
    }

    if (exceptionPattern4xx.matcher(ex.getMessage()).matches()) {
      return RiverDownloadBasis.HTTP_4XX;
    }

    return RiverDownloadBasis.UNKNOWN;
  }

}
