package watch.poe.app.exception;

import lombok.Getter;
import watch.poe.persistence.model.code.DiscardErrorCode;
import watch.poe.persistence.model.code.ParseErrorCode;

@Getter
public class ItemParseException extends Exception {
  private ParseErrorCode parseErrorCode;
  private DiscardErrorCode discardErrorCode;

  public ItemParseException(Exception ex, ParseErrorCode parseErrorCode) {
    super(ex);
    this.parseErrorCode = parseErrorCode;
  }

  public ItemParseException(ParseErrorCode parseErrorCode) {
    super(parseErrorCode.name());
    this.parseErrorCode = parseErrorCode;
  }

  public ItemParseException(DiscardErrorCode discardErrorCode) {
    super(discardErrorCode.name());
    this.discardErrorCode = discardErrorCode;
  }

  public ItemParseException(String msg) {
    super(msg);
  }
}
