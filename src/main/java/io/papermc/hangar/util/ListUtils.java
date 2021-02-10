package io.papermc.hangar.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class ListUtils {

    private ListUtils() { }

    public static <K, V> Map<K, V> zip(List<K> keys, List<V> values) {
        Map<K, V> map = new HashMap<>();
        if (keys != null && values != null) {
            if (keys.size() != values.size()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            for (int i = 0; i < keys.size(); i++) {
                map.put(keys.get(i), values.get(i));
            }
        }
        return map;

    }
}
