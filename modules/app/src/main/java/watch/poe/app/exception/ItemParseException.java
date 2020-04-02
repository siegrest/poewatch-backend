package watch.poe.app.exception;

import lombok.Getter;
import watch.poe.app.dto.DiscardBasis;
import watch.poe.app.dto.ParseExceptionBasis;

@Getter
public class ItemParseException extends Exception {
  private ParseExceptionBasis parseExceptionBasis;
  private DiscardBasis discardBasis;

  public ItemParseException(Exception ex, ParseExceptionBasis parseExceptionBasis) {
    super(ex);
    this.parseExceptionBasis = parseExceptionBasis;
  }

  public ItemParseException(ParseExceptionBasis parseExceptionBasis) {
    super(parseExceptionBasis.name());
    this.parseExceptionBasis = parseExceptionBasis;
  }

  public ItemParseException(DiscardBasis discardBasis) {
    super(discardBasis.name());
    this.discardBasis = discardBasis;
  }

  public ItemParseException(String msg) {
    super(msg);
  }
}
