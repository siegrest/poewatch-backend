package watch.poe.app.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.ListOfJson;

import java.lang.reflect.Type;
import java.util.List;

@Service
public class GsonService {

    @Autowired
    private Gson gson;

    public <T> T toObject(String json) {
        Type type = new TypeToken<T>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public <T> List<T> toList(String json, Class<T> typeClass) {
        return gson.fromJson(json, new ListOfJson<T>(typeClass));
    }

}
