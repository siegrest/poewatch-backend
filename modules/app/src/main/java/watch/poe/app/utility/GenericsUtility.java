package watch.poe.app.utility;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class GenericsUtility {

  public static <T> Stream<List<T>> toBatches(List<T> list, int batchSize) {
    var size = list.size();
    if (size <= 0) {
      return Stream.empty();
    }

    var fullChunks = (size - 1) / batchSize;
    return IntStream.range(0, fullChunks + 1)
      .mapToObj(i -> list.subList(i * batchSize, i == fullChunks ? size : (i + 1) * batchSize));
  }

}
