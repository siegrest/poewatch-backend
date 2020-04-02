package watch.poe.persistence.model.code;

import lombok.Getter;

@Getter
public enum ParseErrorCode {
  MISSING_ITEM("Missing item"),
  MISSING_CATEGORY("Missing category"),
  INVALID_ENCHANTMENT_ROLLS("Invalid enchantment rolls"),

  DEV("Development"),

  PARSE_MAP_ICON("Could not parse a map's icon"),
  PARSE_MAP_ICON_PARAM_F("Could not find param 'f' (icon path) in the map's icon"),
  PARSE_MAP_ICON_PARAM_MN("Could not find param 'mn' (series) in the map's icon"),

  PARSE_UNID_UNIQUE_ITEM("Cannot parse unidentified unique items"),
  PARSE_UNID_UNIQUE_MAP("Unimplemented unidentified unique map encountered"),
  PARSE_CATEGORY("Could not determine category"),
  UNHANDLED_CATEGORY("Category is defined but not handled"),
  MISSING_CURRENCY("Currency is defined in whitelist but does not appear in database"),
  DUPLICATE_CURRENCY_ITEM("Found multiple matching currency items"),
  PARSE_GROUP("Could not determine group");

  private String description;

  ParseErrorCode(String description) {
    this.description = description;
  }
}
