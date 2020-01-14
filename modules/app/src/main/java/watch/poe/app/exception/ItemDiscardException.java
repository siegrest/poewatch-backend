package watch.poe.app.exception;

public class ItemDiscardException extends Exception {
  public ItemDiscardException(Exception ex) {
    super(ex);
  }

  public ItemDiscardException(String message) {
    super(message);
  }

  public ItemDiscardException() {
  }
}
