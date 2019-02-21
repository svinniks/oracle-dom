package dom;

import simplegrammar.SyntaxTreeNode;

public class TokenSerializer {

    private boolean spaceRequired(String name1, String value1, String name2, String value2) {

        return
                "IDENTIFIER".equals(name1) && "IDENTIFIER".equals(name2) ||
                "IDENTIFIER".equals(name1) && "QUOTED_IDENTIFIER".equals(name2) ||
                "QUOTED_IDENTIFIER".equals(name1) && "IDENTIFIER".equals(name2) ||

                "IDENTIFIER".equals(name1) && "ARITHMETIC_OPERATOR".equals(name2) ||
                "QUOTED_IDENTIFIER".equals(name1) && "ARITHMETIC_OPERATOR".equals(name2) ||
                "STRING_LITERAL".equals(name1) && "ARITHMETIC_OPERATOR".equals(name2) ||
                "NUMBER_LITERAL".equals(name1) && "ARITHMETIC_OPERATOR".equals(name2) ||

                "IDENTIFIER".equals(name1) && "CONCATENATION_OPERATOR".equals(name2) ||
                "QUOTED_IDENTIFIER".equals(name1) && "CONCATENATION_OPERATOR".equals(name2) ||
                "STRING_LITERAL".equals(name1) && "CONCATENATION_OPERATOR".equals(name2) ||
                "NUMBER_LITERAL".equals(name1) && "CONCATENATION_OPERATOR".equals(name2) ||

                "IDENTIFIER".equals(name1) && "LOGICAL_OPERATOR".equals(name2) ||
                "QUOTED_IDENTIFIER".equals(name1) && "LOGICAL_OPERATOR".equals(name2) ||
                "STRING_LITERAL".equals(name1) && "LOGICAL_OPERATOR".equals(name2) ||
                "NUMBER_LITERAL".equals(name1) && "LOGICAL_OPERATOR".equals(name2) ||

                "ARITHMETIC_OPERATOR".equals(name1) && "IDENTIFIER".equals(name2) ||
                "ARITHMETIC_OPERATOR".equals(name1) && "QUOTED_IDENTIFIER".equals(name2) ||
                "ARITHMETIC_OPERATOR".equals(name1) && "STRING_LITERAL".equals(name2) ||
                "ARITHMETIC_OPERATOR".equals(name1) && "NUMBER_LITERAL".equals(name2) ||

                "CONCATENATION_OPERATOR".equals(name1) && "IDENTIFIER".equals(name2) ||
                "CONCATENATION_OPERATOR".equals(name1) && "QUOTED_IDENTIFIER".equals(name2) ||
                "CONCATENATION_OPERATOR".equals(name1) && "STRING_LITERAL".equals(name2) ||
                "CONCATENATION_OPERATOR".equals(name1) && "NUMBER_LITERAL".equals(name2) ||

                "LOGICAL_OPERATOR".equals(name1) && "IDENTIFIER".equals(name2) ||
                "LOGICAL_OPERATOR".equals(name1) && "QUOTED_IDENTIFIER".equals(name2) ||
                "LOGICAL_OPERATOR".equals(name1) && "STRING_LITERAL".equals(name2) ||
                "LOGICAL_OPERATOR".equals(name1) && "NUMBER_LITERAL".equals(name2) ||

                "ARITHMETIC_OPERATOR".equals(name1) && "LEFT_BRACKET".equals(name2) ||
                "RIGHT_BRACKET".equals(name1) && "ARITHMETIC_OPERATOR".equals(name2) ||

                "CONCATENATION_OPERATOR".equals(name1) && "LEFT_BRACKET".equals(name2) ||
                "RIGHT_BRACKET".equals(name1) && "CONCATENATION_OPERATOR".equals(name2) ||

                "LOGICAL_OPERATOR".equals(name1) && "LEFT_BRACKET".equals(name2) ||
                "RIGHT_BRACKET".equals(name1) && "LOGICAL_OPERATOR".equals(name2);

    }

    public String serializeTokens(SyntaxTreeNode rootNode) {

        StringBuilder resultBuilder = new StringBuilder();

        String lastName = null;
        String lastValue = null;

        for (SyntaxTreeNode childNode : rootNode.getChildNodes()) {

            String name = childNode.getValue();
            String value = childNode.getChildValue(0);

            if (spaceRequired(lastName, lastValue, name, value))
                resultBuilder.append(' ');

            if (name == "QUOTED_IDENTIFIER")
                resultBuilder.append('"').append(value).append('"');
            else if (name == "STRING_LITERAL")
                resultBuilder.append('\'').append(value.replace("'", "''")).append('\'');
            else
                resultBuilder.append(value);

            lastName = name;
            lastValue = value;

        }

        return resultBuilder.toString();

    }

}
