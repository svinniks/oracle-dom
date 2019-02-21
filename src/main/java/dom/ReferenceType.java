package dom;

import static dom.DataTypeType.REFERENCE;

public class ReferenceType extends DataType {

    String[] reference;
    DataTypeModifier modifier;

    public ReferenceType() {
        type = REFERENCE;
    }

}
