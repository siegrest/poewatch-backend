package watch.poe.app.service;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.ListOfJson;

import java.util.List;

@Service
public class GsonService {

    @Autowired
    private Gson gson;

    public <T> T toObject(String json, Class<T> typeClass) {
        return gson.fromJson(json, typeClass);
    }

    public <T> List<T> toList(String json, Class<T> typeClass) {
        return gson.fromJson(json, new ListOfJson<T>(typeClass));
    }

}
