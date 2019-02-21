package database;

import java.io.InputStream;
import java.util.Scanner;

public class SQL {

    public static String load(String fileName) {

        InputStream inputStream = SQL.class.getResourceAsStream(String.format("/sql/%s", fileName));
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");

        return scanner.hasNext() ? scanner.next() : "";

    }

}
