package watch.poe.app.exception;

import lombok.Getter;
import watch.poe.app.domain.DiscardBasis;

@Getter
public class ItemParseException extends Exception {
  private DiscardBasis discardBasis;

  public ItemParseException(Exception ex) {
    super(ex);
  }

  public ItemParseException(String message) {
    super(message);
  }

  public ItemParseException(DiscardBasis discardBasis) {
    super(discardBasis.name());
    this.discardBasis = discardBasis;
  }

  public ItemParseException() {
  }
}
