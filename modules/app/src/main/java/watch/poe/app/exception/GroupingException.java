package watch.poe.app.exception;

import lombok.Getter;
import watch.poe.app.dto.GroupingExceptionBasis;

@Getter
public class GroupingException extends Exception {
  private GroupingExceptionBasis basis;

  public GroupingException(GroupingExceptionBasis basis) {
    super(basis.getDescription());
    this.basis = basis;
  }
}
