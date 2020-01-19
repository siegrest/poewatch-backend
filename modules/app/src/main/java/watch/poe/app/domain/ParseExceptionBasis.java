package watch.poe.app.domain;

import lombok.Getter;

@Getter
public enum ParseExceptionBasis {
  MISSING_ITEM("Missing item"),
  MISSING_CATEGORY("Missing category"),
  MISSING_FRAME_TYPE("Invalid frame type"),

  PARSE_CATEGORY("Could not determine category"),
  PARSE_GROUP("Could not determine group");

  private String description;

  ParseExceptionBasis(String description) {
    this.description = description;
  }
}
