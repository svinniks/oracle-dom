package dom;

import java.util.List;

public class Parameter {

    String name;

    boolean in;
    boolean out;
    boolean nocopy;

    DataType dataType;
    String defaultValueExpression;

    List<Annotation> annotations;

}
