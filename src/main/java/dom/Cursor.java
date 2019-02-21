package dom;

import static dom.DeclarationType.CURSOR;

public class Cursor extends Subprogram {

    String queryStatement;

    public Cursor() {
        type = CURSOR;
    }

}
