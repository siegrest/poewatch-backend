package watch.poe.app.domain;

import lombok.Getter;

@Getter
public enum DiscardBasis {
  PARSE_COMPLEX_RARE("Cannot parse rare items"),
  PARSE_COMPLEX_MAGIC("Cannot parse magic items"),

  GEM_LEVEL_MISSING("Could not find gem level"),
  GEM_QUALITY_MISSING("Could not find gem quality"),
  GEM_LEVEL_OUT_OF_RANGE("Level is out of range for gem"),
  GEM_QUALITY_OUT_OF_RANGE("Quality is out of range"),
  GEM_BRAND_RECALL_LEVEL_OUT_OF_RANGE("Level is out of range for Brand Recall"),
  GEM_API_BUG("Encountered API bug for gems"),

  PARSE_UNID_UNIQUE_MAP("Cannot parse unidentified unique map"),
  PARSE_MAGIC_MAP("Cannot parse magic maps"),
  PARSE_RARE_MAP("Cannot parse rare maps"),

  MAP_TIER_MISSING("No map tier found"),
  PARSE_MAP_SERIES_FAILED("Failed to extract map series"),
  INVALID_MAP_SERIES("Invalid map series found"),

  NO_SOCKETS("No sockets found"),

  PARSE_STACK_SIZE("Couldn't parse stack size"),
  STACK_SIZE_MISSING("Couldn't locate stack size"),
  STACK_SIZE_SLASH_MISSING("Couldn't locate stack size slash"),

  DEVELOP("Item discarded for development");

  private String description;

  DiscardBasis(String description) {
    this.description = description;
  }
}
