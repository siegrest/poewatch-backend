package watch.poe.app.exception;

import lombok.Getter;
import watch.poe.app.domain.DiscardBasis;
import watch.poe.app.domain.ParseExceptionBasis;

@Getter
public class ItemParseException extends Exception {
  private ParseExceptionBasis parseExceptionBasis;
  private DiscardBasis discardBasis;

  public ItemParseException(ParseExceptionBasis parseExceptionBasis) {
    super(parseExceptionBasis.name());
    this.parseExceptionBasis = parseExceptionBasis;
  }

  public ItemParseException(DiscardBasis discardBasis) {
    super(discardBasis.name());
    this.discardBasis = discardBasis;
  }
}
