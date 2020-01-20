package watch.poe.app.domain;

import lombok.Getter;

@Getter
public enum ParseExceptionBasis {
  MISSING_ITEM("Missing item"),
  MISSING_CATEGORY("Missing category"),
  MISSING_FRAME_TYPE("Invalid frame type"),
  INVALID_ENCHANTMENT_ROLLS("Invalid enchantment rolls"),

  PARSE_CATEGORY("Could not determine category"),
  UNHANDLED_CATEGORY("Category is defined but not handled"),
  PARSE_GROUP("Could not determine group");

  private String description;

  ParseExceptionBasis(String description) {
    this.description = description;
  }
}
