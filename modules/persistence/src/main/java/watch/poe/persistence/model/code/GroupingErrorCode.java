package watch.poe.persistence.model.code;

import lombok.Getter;

@Getter
public enum GroupingErrorCode {
  PARSE("Could not determine group"),
  DEPRECATED("Group definition has been deprecated and moved to another category or group"),
  UNHANDLED_CATEGORY("Category is defined but not handled");

  private String description;

  GroupingErrorCode(String description) {
    this.description = description;
  }
}
