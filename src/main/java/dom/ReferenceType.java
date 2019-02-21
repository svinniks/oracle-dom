package dom;

import java.util.ArrayList;
import java.util.List;

import static dom.DataTypeType.REFERENCE;

public class ReferenceType extends DataType {

    List<String> reference;
    DataTypeModifier modifier;

    public ReferenceType() {
        type = REFERENCE;
        reference = new ArrayList<>();
    }

}
