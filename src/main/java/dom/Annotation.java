package dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Annotation {

    String name;
    List<Annotation> annotations;
    Map<String, Object> parameters;

    public Annotation() {
        annotations = new ArrayList<>();
        parameters = new HashMap<>();
    }

}
