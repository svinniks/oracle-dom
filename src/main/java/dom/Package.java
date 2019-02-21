package dom;

import java.util.ArrayList;
import java.util.List;

import static dom.Authid.DEFINER;

public class Package extends CodeItem {

    String name;
    Authid authid;
    List<Declaration> declarations;

    public Package() {
        declarations = new ArrayList<>();
    }

}
