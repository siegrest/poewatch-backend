package watch.poe.persistence.utility;

public final class HashUtility {

  // prime
  private static final long h = 1125899906842597L;

  public static long hash(String string) {
    var h = HashUtility.h;
    int len = string.length();

    for (int i = 0; i < len; i++) {
      h = 31 * h + string.charAt(i);
    }

    return h;
  }

}
