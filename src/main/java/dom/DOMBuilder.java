package dom;

import simplegrammar.SyntaxTreeNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DOMBuilder {

    private static TokenSerializer TOKEN_SERIALIZER;

    static {
        TOKEN_SERIALIZER = new TokenSerializer();
    }

    public static CharacterType buildCharacterType(SyntaxTreeNode rootNode) {

        CharacterType type = new CharacterType();
        type.name = rootNode.getValue();

        SyntaxTreeNode characterTypeSizeNode = rootNode.getChildNode("CHARACTER_TYPE_SIZE");

        if (characterTypeSizeNode != null) {

            type.size = Integer.valueOf(characterTypeSizeNode.getChildValue(0));
            String lengthSemanticsValue = characterTypeSizeNode.getChildValue(1);

            if (lengthSemanticsValue != null)
                type.sizeSemantics = CharacterTypeLengthSemantics.valueOf(lengthSemanticsValue);

        }

        return type;

    };

    public static Number buildNumber(SyntaxTreeNode rootNode) {

        Number number = new Number();

        SyntaxTreeNode precisionNode = rootNode.getChildNode(0);
        SyntaxTreeNode scaleNode = rootNode.getChildNode(1);

        if (precisionNode != null)
            number.precision = Integer.valueOf(precisionNode.getValue());

        if (scaleNode != null)
            number.scale = Integer.valueOf(scaleNode.getValue());

        return number;

    }

    public static Float buildFloat(SyntaxTreeNode rootNode) {

        Float float_ = new Float();

        SyntaxTreeNode precisionNode = rootNode.getChildNode(0);
        SyntaxTreeNode scaleNode = rootNode.getChildNode(1);

        if (precisionNode != null)
            float_.precision = Integer.valueOf(precisionNode.getValue());

        return float_;

    }

    public static TimestampType buildTimestampType(SyntaxTreeNode rootNode) {

        TimestampType timestampType = new TimestampType();

        SyntaxTreeNode tzNode = rootNode.getChildNode("TZ");

        if (tzNode != null)
            if (tzNode.getChildNode("LTZ") != null)
                timestampType.name = "TIMESTAMP_LTZ";
            else
                timestampType.name = "TIMESTAMP_TZ";
        else
            timestampType.name = "TIMESTAMP";

        SyntaxTreeNode precisionNode = rootNode.getChildNode("TIMESTAMP_PRECISION");

        if (precisionNode != null)
            timestampType.precision = Integer.valueOf(precisionNode.getChildValue(0));

        return timestampType;

    }

    public static IntervalYM buildIntervalYM(SyntaxTreeNode rootNode) {

        IntervalYM intervalYM = new IntervalYM();

        SyntaxTreeNode yearPrecisionNode = rootNode.getChildNode("YEAR_PRECISION");

        if (yearPrecisionNode != null)
            intervalYM.yearPrecision = Integer.valueOf(yearPrecisionNode.getChildValue(0));

        return intervalYM;

    }

    public static IntervalDS buildIntervalDS(SyntaxTreeNode rootNode) {

        IntervalDS intervalDS = new IntervalDS();

        SyntaxTreeNode dayPrecisionNode = rootNode.getChildNode("DAY_PRECISION");

        if (dayPrecisionNode != null)
            intervalDS.dayPrecision = Integer.valueOf(dayPrecisionNode.getChildValue(0));

        SyntaxTreeNode secondPrecisionNode = rootNode.getChildNode("SECOND_PRECISION");

        if (secondPrecisionNode != null)
            intervalDS.secondPrecision = Integer.valueOf(secondPrecisionNode.getChildValue(0));

        return intervalDS;

    }

    public static Raw buildRaw(SyntaxTreeNode rootNode) {

        Raw raw = new Raw();

        SyntaxTreeNode sizeNode = rootNode.getChildNode(0);

        if (sizeNode != null)
            raw.size = Integer.valueOf(sizeNode.getValue());

        return raw;

    }

    public static URowId buildURowId(SyntaxTreeNode rootNode) {

        URowId uRowId = new URowId();

        SyntaxTreeNode sizeNode = rootNode.getChildNode(0);

        if (sizeNode != null)
            uRowId.size = Integer.valueOf(sizeNode.getValue());

        return uRowId;

    }

    public static DataType buildDataType(SyntaxTreeNode rootNode) {

        SyntaxTreeNode typeNode = rootNode.getChildNode(0);
        String typeNodeValue = typeNode.getValue();

        if (typeNodeValue.matches("^(SIMPLE_PARAMETER_TYPE)|(SIMPLE_VARIABLE_TYPE)$")) {

            String name = typeNode.getChildValue(0);

            if (name.matches("^(VARCHAR2)|(NVARCHAR2)|(CHAR)|(NCHAR)$"))
                return buildCharacterType(typeNode.getChildNode(0));
            else if (name.equals("NUMBER"))
                return buildNumber(typeNode.getChildNode(0));
            else if (name.equals("FLOAT"))
                return buildFloat(typeNode.getChildNode(0));
            else if (name.matches("^TIMESTAMP.*$"))
                return buildTimestampType(typeNode.getChildNode(0));
            else if (name.matches("^INTERVAL_YM.*$"))
                return buildIntervalYM(typeNode.getChildNode(0));
            else if (name.matches("^INTERVAL_DS.*$"))
                return buildIntervalDS(typeNode.getChildNode(0));
            else if (name.equals("RAW"))
                return buildRaw(typeNode.getChildNode(0));
            else if (name.equals("UROWID"))
                return buildURowId(typeNode.getChildNode(0));
            else {

                SimpleDataType type = new SimpleDataType();
                type.name = name;

                return type;

            }

        } else {

            ReferenceType type = new ReferenceType();

            for (SyntaxTreeNode referencePartNode : typeNode.getChildNode("REFERENCE").getChildNodes())
                type.reference.add(referencePartNode.getValue());

            SyntaxTreeNode modifierNode = typeNode.getChildNode("TYPE_MODIFIER");

            if (modifierNode != null)
                type.modifier = DataTypeModifier.valueOf(modifierNode.getChildValue(0));

            return type;

        }

    }

    public static List<Parameter> buildParameters(SyntaxTreeNode rootNode) {

        List<Parameter> parameters = new ArrayList<>();

        for (SyntaxTreeNode parameterNode : rootNode.getChildNodes()) {

            Parameter parameter = new Parameter();
            parameter.name = parameterNode.getChildNode("PARAMETER_NAME").getChildValue(0);

            SyntaxTreeNode directionNode = parameterNode.getChildNode("PARAMETER_DIRECTION");

            if (directionNode == null)
                parameter.in = true;
            else
                for (SyntaxTreeNode directionValueNode : directionNode.getChildNodes())
                    switch (directionValueNode.getValue()) {
                        case "IN":
                            parameter.in = true;
                            break;
                        case "OUT":
                            parameter.out = true;
                            break;
                        case "NOCOPY":
                            parameter.nocopy = true;
                    }

            parameter.dataType = buildDataType(parameterNode.getChildNode("PARAMETER_TYPE"));

            SyntaxTreeNode parameterDefaultNode = parameterNode.getChildNode("PARAMETER_DEFAULT");

            if (parameterDefaultNode != null)
                parameter.defaultValueExpression = TOKEN_SERIALIZER.serializeTokens(parameterDefaultNode);

            parameter.annotations = buildAnnotations(parameterNode);

            parameters.add(parameter);

        }

        return parameters;

    }

    public static FunctionDirectives buildFunctionDirectives(SyntaxTreeNode rootNode) {

        FunctionDirectives directives = new FunctionDirectives();

        for (SyntaxTreeNode directiveNode : rootNode.getChildNodes()) {

            String value = directiveNode.getValue();

            if (value.equals("PIPELINED"))
                directives.pipelined = true;
            else if (value.equals("DETERMINISTIC"))
                directives.deterministic = true;
            else if (value.equals("PARALLEL_ENABLE"))
                directives.parallel = true;
            else if (value.equals("RESULT_CACHE"))
                directives.resultCache = true;

        }

        return directives;

    }

    public static CallSpecification buildCallSpecification(SyntaxTreeNode rootNode) {

        SyntaxTreeNode asLanguageNode = rootNode.getChildNode("AS_LANGUAGE").getChildNode(0);

        if (asLanguageNode.getValue().equals("AS_LANGUAGE_JAVA")) {

            JavaCallSpecification specification = new JavaCallSpecification();
            specification.name = asLanguageNode.getChildValue(0);

            return specification;

        } else
            return new CCallSpecification();

    }

    public static Procedure buildProcedure(SyntaxTreeNode rootNode) {

        Procedure procedure = new Procedure();
        procedure.name = rootNode.getChildNode("SUBPROGRAM_NAME").getChildValue(0);

        procedure.parameters = buildParameters(rootNode.getChildNode("PARAMETERS"));

        SyntaxTreeNode callSpecificationNode = rootNode.getChildNode("CALL_SPECIFICATION");

        if (callSpecificationNode != null)
            procedure.callSpecification = buildCallSpecification(callSpecificationNode);

        return procedure;

    }

    public static Function buildFunction(SyntaxTreeNode rootNode) {

        Function function = new Function();
        function.name = rootNode.getChildNode("SUBPROGRAM_NAME").getChildValue(0);

        function.parameters = buildParameters(rootNode.getChildNode("PARAMETERS"));

        SyntaxTreeNode functionReturnNode = rootNode.getChildNode("FUNCTION_RETURN");
        function.returnDataType = buildDataType(functionReturnNode.getChildNode("PARAMETER_TYPE"));

        function.directives = buildFunctionDirectives(rootNode.getChildNode("DIRECTIVES"));
        function.returnAnnotations = buildAnnotations(functionReturnNode);

        SyntaxTreeNode callSpecificationNode = rootNode.getChildNode("CALL_SPECIFICATION");

        if (callSpecificationNode != null)
            function.callSpecification = buildCallSpecification(callSpecificationNode);

        return function;

    }

    public static Variable buildVariable(SyntaxTreeNode rootNode) {

        Variable variable = new Variable();
        variable.name = rootNode.getChildNode("VARIABLE_NAME").getChildValue(0);
        variable.dataType = buildDataType(rootNode.getChildNode("VARIABLE_TYPE"));

        SyntaxTreeNode variableValueNode = rootNode.getChildNode("VARIABLE_VALUE");

        if (variableValueNode != null)
            variable.valueExpression = TOKEN_SERIALIZER.serializeTokens(variableValueNode.getChildNode("EXPRESSION"));

        return variable;

    }

    public static Constant buildConstant(SyntaxTreeNode rootNode) {

        Constant constant = new Constant();
        constant.name = rootNode.getChildNode("VARIABLE_NAME").getChildValue(0);
        constant.dataType = buildDataType(rootNode.getChildNode("VARIABLE_TYPE"));
        constant.valueExpression = TOKEN_SERIALIZER.serializeTokens(rootNode.getChildNode("EXPRESSION"));

        return constant;

    }

    public static RecordType buildRecordType(SyntaxTreeNode rootNode) {

        RecordType type = new RecordType();

        return type;

    }

    public static NestedTableType buildNestedTableType(SyntaxTreeNode rootNode) {

        NestedTableType type = new NestedTableType();
        type.dataType = buildDataType(rootNode.getChildNode("VARIABLE_TYPE"));

        return type;

    }

    public static AssociativeArrayType buildAssociativeArrayType(SyntaxTreeNode rootNode) {

        AssociativeArrayType type = new AssociativeArrayType();

        type.dataType = buildDataType(rootNode.getChildNode("VARIABLE_TYPE"));
        type.indexType = buildDataType(rootNode.getChildNode("INDEX_BY").getChildNode("VARIABLE_TYPE"));

        return type;

    }

    public static VArrayType buildVArrayType(SyntaxTreeNode rootNode) {

        VArrayType type = new VArrayType();

        type.size = Integer.valueOf(rootNode.getChildValue(0));
        type.dataType = buildDataType(rootNode.getChildNode("VARIABLE_TYPE"));

        return type;

    }

    public static RefCursorType buildRefCursorType(SyntaxTreeNode rootNode) {

        RefCursorType type = new RefCursorType();

        SyntaxTreeNode returnTypeNode = rootNode.getChildNode("REF_CURSOR_RETURN");

        if (returnTypeNode != null)
            type.returnType = buildDataType(returnTypeNode);

        return type;

    }

    public static Type buildType(SyntaxTreeNode rootNode) {

        Type type = null;

        SyntaxTreeNode typeDeclarationNode = rootNode.getChildNode("TYPE_DECLARATION").getChildNode(0);
        String typeType = typeDeclarationNode.getValue();

        if (typeType.equals("RECORD_TYPE"))
            type = buildRecordType(typeDeclarationNode);
        else if (typeType.equals("NESTED_TABLE_TYPE"))
            type = buildNestedTableType(typeDeclarationNode);
        else if (typeType.equals("ASSOCIATIVE_ARRAY_TYPE"))
            type = buildAssociativeArrayType(typeDeclarationNode);
        else if (typeType.equals("VARRAY_TYPE"))
            type = buildVArrayType(typeDeclarationNode);
        else if (typeType.equals("REF_CURSOR_TYPE"))
            type = buildRefCursorType(typeDeclarationNode);

        type.name = rootNode.getChildNode("TYPE_NAME").getChildValue(0);
        return type;

    }

    public static Cursor buildCursor(SyntaxTreeNode rootNode) {

        Cursor cursor = new Cursor();
        cursor.name = rootNode.getChildNode("CURSOR_NAME").getChildValue(0);
        cursor.parameters = buildParameters(rootNode.getChildNode("PARAMETERS"));
        cursor.queryStatement = TOKEN_SERIALIZER.serializeTokens(rootNode.getChildNode("CURSOR_QUERY"));

        return cursor;

    }

    public static Exception buildException(SyntaxTreeNode rootNode) {

        Exception exception = new Exception();
        exception.name = rootNode.getChildNode("EXCEPTION_NAME").getChildValue(0);

        return exception;

    }

    public static ExceptionInit buildExceptionInit(SyntaxTreeNode rootNode) {

        ExceptionInit pragma = new ExceptionInit();
        pragma.exception = rootNode.getChildNode("EXCEPTION_INIT_EXCEPTION").getChildValue(0);

        StringBuilder errorCodeBuilder = new StringBuilder();

        for (SyntaxTreeNode errorCodeNode: rootNode.getChildNode("EXCEPTION_INIT_CODE").getChildNodes())
            errorCodeBuilder.append(errorCodeNode.getValue());

        pragma.errorCode = Integer.valueOf(errorCodeBuilder.toString());

        return pragma;

    }

    public static GenericPragma buildGenericPragma(SyntaxTreeNode rootNode) {

        GenericPragma pragma = new GenericPragma();
        pragma.name = rootNode.getChildValue(0);

        return pragma;

    }

    public static Pragma buildPragma(SyntaxTreeNode rootNode) {

        SyntaxTreeNode pragmaNode = rootNode.getChildNode(0);
        String pragmaName = pragmaNode.getValue();

        if (pragmaName.equals("EXCEPTION_INIT"))
            return buildExceptionInit(pragmaNode);
        else
            return buildGenericPragma(pragmaNode);
            //throw new UnsupportedOperationException(String.format("Unsupported pragma type %s!", pragmaName));

    }

    public static Object buildAnnotationParameterSimpleValue(SyntaxTreeNode rootNode) {

        String parameterValueType = rootNode.getValue();

        if (parameterValueType.equals("ANNOTATION_STRING_LITERAL"))
            return rootNode.getChildValue(0);
        else if (parameterValueType.equals("ANNOTATION_BOOLEAN_LITERAL"))
            return Boolean.valueOf(rootNode.getChildValue(0));
        else if (parameterValueType.equals("ANNOTATION_NUMBER_LITERAL"))
            return new BigDecimal(rootNode.getChildValue(0));
        else
            throw new UnsupportedOperationException("Unsupported annotation parameter type!");

    }

    public static Object buildAnnotationParameterValue(SyntaxTreeNode rootNode) {

        String valueType = rootNode.getValue();

        if (valueType.equals("ANNOTATION_SIMPLE_VALUE"))
            return buildAnnotationParameterSimpleValue(rootNode.getChildNode(0));
        else if (valueType.equals("ANNOTATION_ARRAY_VALUE")) {

            List<Object> elements = new ArrayList<>();

            for (SyntaxTreeNode elementNode : rootNode.getChildNodes())
                elements.add(buildAnnotationParameterSimpleValue(elementNode));

            return elements;

        } else
            return null;

    }

    public static Annotation buildAnnotation(SyntaxTreeNode rootNode) {

        Annotation annotation = new Annotation();
        annotation.name = rootNode.getChildNode("ANNOTATION_NAME").getChildValue(0);

        SyntaxTreeNode annotationParametersNode = rootNode.getChildNode("ANNOTATION_PARAMETERS");

        for (SyntaxTreeNode annotationParameterNode : annotationParametersNode.getChildNodes()) {

            if (annotationParameterNode.getValue().equals("ANNOTATION_VALUE"))

                annotation.parameters.put(
                        "value",
                        buildAnnotationParameterValue(annotationParameterNode.getChildNode(0))
                );

            else {

                SyntaxTreeNode annotationNode = annotationParameterNode.getChildNode("ANNOTATION");

                if (annotationNode != null)
                    annotation.annotations.add(buildAnnotation(annotationNode));
                else {

                    String parameterName = annotationParameterNode.getChildNode("ANNOTATION_PARAMETER_NAME").getChildValue(0);
                    SyntaxTreeNode parameterValueNode = annotationParameterNode.getChildNode("ANNOTATION_PARAMETER_VALUE").getChildNode(0);

                    annotation.parameters.put(
                            parameterName,
                            buildAnnotationParameterValue(parameterValueNode)
                    );

                }

            }

        }

        return annotation;

    }

    public static List<Annotation> buildAnnotations(SyntaxTreeNode rootNode) {

        List<Annotation> annotations = new ArrayList<>();

        SyntaxTreeNode annotationsNode = rootNode.getChildNode("ANNOTATIONS");

        if (annotationsNode != null)
            for (SyntaxTreeNode annotationNode : annotationsNode.getChildNodes())
                annotations.add(buildAnnotation(annotationNode));

        return annotations;

    }

    public static Type buildSubtype(SyntaxTreeNode rootNode) {

        Subtype subtype = new Subtype();
        subtype.name = rootNode.getChildNode("TYPE_NAME").getChildValue(0);
        subtype.dataType = buildDataType(rootNode.getChildNode("VARIABLE_TYPE"));

        return subtype;

    }

    public static Package buildPackage(SyntaxTreeNode rootNode) {

        Package package_ = new Package();

        package_.name = rootNode.getChildNode("PACKAGE_NAME").getChildNode("PART2").getChildValue(0);

        SyntaxTreeNode authidNode = rootNode.getChildNode("AUTHID");

        if (authidNode != null)
            package_.authid = Authid.valueOf(authidNode.getChildValue(0));

        SyntaxTreeNode declarationsNode = rootNode.getChildNode("DECLARATIONS");

        for (SyntaxTreeNode declarationNode : declarationsNode.getChildNodes()) {

            String declarationType = declarationNode.getValue();
            Declaration declaration;

            if (declarationType.equals("PROCEDURE"))
                declaration = buildProcedure(declarationNode);
            else if (declarationType.equals("FUNCTION"))
                declaration = buildFunction(declarationNode);
            else if (declarationType.equals("VARIABLE"))
                declaration = buildVariable(declarationNode);
            else if (declarationType.equals("CONSTANT"))
                declaration = buildConstant(declarationNode);
            else if (declarationType.equals("TYPE"))
                declaration = buildType(declarationNode);
            else if (declarationType.equals("CURSOR"))
                declaration = buildCursor(declarationNode);
            else if (declarationType.equals("EXCEPTION"))
                declaration = buildException(declarationNode);
            else if (declarationType.equals("PACKAGE_PRAGMA"))
                declaration = buildPragma(declarationNode);
            else if (declarationType.equals("SUBTYPE"))
                declaration = buildSubtype(declarationNode);
            else
                throw new IllegalArgumentException(String.format("Unsupported declaration type %s!", declarationType));

            declaration.annotations = buildAnnotations(declarationNode);
            package_.declarations.add(declaration);

        }

        return package_;

    };

}
