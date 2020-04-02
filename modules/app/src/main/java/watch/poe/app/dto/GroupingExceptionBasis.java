package watch.poe.app.dto;

import lombok.Getter;

@Getter
public enum GroupingExceptionBasis {
  PARSE("Could not determine group"),
  DEPRECATED("Group definition has been deprecated and moved to another category or group"),
  UNHANDLED_CATEGORY("Category is defined but not handled");

  private String description;

  GroupingExceptionBasis(String description) {
    this.description = description;
  }
}
