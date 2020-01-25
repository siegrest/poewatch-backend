package watch.poe.app.domain;

import lombok.Getter;

@Getter
public enum ParseExceptionBasis {
  MISSING_ITEM("Missing item"),
  MISSING_CATEGORY("Missing category"),
  MISSING_FRAME_TYPE("Invalid frame type"),
  INVALID_ENCHANTMENT_ROLLS("Invalid enchantment rolls"),

  DEV("Development"),

  PARSE_UNID_UNIQUE_ITEM("Cannot parse unidentified unique items"),
  PARSE_UNID_UNIQUE_MAP("Unimplemented unidentified unique map encountered"),
  PARSE_CATEGORY("Could not determine category"),
  UNHANDLED_CATEGORY("Category is defined but not handled"),
  DEPRECATED_GROUP("Group definition has been deprecated and moved to other categories/groups"),
  MISSING_CURRENCY("Currency is defined in whitelist but does not appear in database"),
  DUPLICATE_CURRENCY_ITEM("Found multiple matching currency items"),
  PARSE_GROUP("Could not determine group");

  private String description;

  ParseExceptionBasis(String description) {
    this.description = description;
  }
}
