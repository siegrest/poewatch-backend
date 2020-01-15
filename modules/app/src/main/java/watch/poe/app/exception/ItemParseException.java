package watch.poe.app.exception;

public class ItemParseException extends Exception {
  public ItemParseException(Exception ex) {
    super(ex);
  }

  public ItemParseException(String message) {
    super(message);
  }

  public ItemParseException() {
  }
}
