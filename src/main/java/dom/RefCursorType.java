package dom;

import static dom.DeclarationType.REF_CURSOR_TYPE;

public class RefCursorType extends Type {

    DataType returnType;

    public RefCursorType() {
        type = REF_CURSOR_TYPE;
    }

}
