SELECT *
FROM all_objects
WHERE object_type = ?
      AND owner = ?
      AND object_name = ?