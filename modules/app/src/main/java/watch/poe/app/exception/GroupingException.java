package watch.poe.app.exception;

import lombok.Getter;
import watch.poe.persistence.model.code.GroupingErrorCode;

@Getter
public class GroupingException extends Exception {
  private GroupingErrorCode basis;

  public GroupingException(GroupingErrorCode basis) {
    super(basis.getDescription());
    this.basis = basis;
  }
}
