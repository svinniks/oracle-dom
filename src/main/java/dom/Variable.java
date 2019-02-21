package dom;

import static dom.DeclarationType.VARIABLE;

public class Variable extends Declaration {

    DataType dataType;
    String valueExpression;

    public Variable() {
        type = VARIABLE;
    }

}
