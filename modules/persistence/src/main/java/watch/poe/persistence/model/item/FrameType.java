package watch.poe.persistence.model.item;

public enum FrameType {
  NORMAL,
  MAGIC,
  RARE,
  UNIQUE,
  GEM,
  CURRENCY,
  DIV,
  QUEST,
  PROPHECY,
  RELIC;

  public static FrameType from(Integer ordinal) {
    return FrameType.values()[ordinal];
  }

  public boolean is(Integer ordinal) {
    return this.ordinal() == ordinal;
  }
}
