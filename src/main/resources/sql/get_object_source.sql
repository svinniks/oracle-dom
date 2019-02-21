DECLARE
    v_source_lines DBMS_PREPROCESSOR.SOURCE_LINES_T;
    v_source CLOB;
BEGIN

    v_source_lines := DBMS_PREPROCESSOR.GET_POST_PROCESSED_SOURCE(?, ?, ?);
    DBMS_LOB.CREATETEMPORARY(v_source, TRUE);

    FOR v_i IN 1..v_source_lines.COUNT LOOP
        DBMS_LOB.APPEND(v_source, v_source_lines(v_i));
    END LOOP;

    ? := v_source;

END;