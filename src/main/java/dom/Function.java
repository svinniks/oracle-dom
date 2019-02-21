package dom;

import java.util.List;

import static dom.DeclarationType.FUNCTION;

public class Function extends Subprogram {

    DataType returnDataType;
    List<Annotation> returnAnnotations;
    FunctionDirectives directives;

    public Function() {
        type = FUNCTION;
    }

}
