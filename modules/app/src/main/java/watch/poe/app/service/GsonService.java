package watch.poe.app.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.ListOfJson;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GsonService {

  private final Gson gson;

  public <T> T toObject(String json, Class<T> typeClass) {
    return gson.fromJson(json, typeClass);
  }

  public <T> List<T> toList(String json, Class<T> typeClass) {
    return gson.fromJson(json, new ListOfJson<T>(typeClass));
  }

  public <T> T fromType(String json) {
    return gson.fromJson(json, new TypeToken<T>(){}.getType());
  }
}
