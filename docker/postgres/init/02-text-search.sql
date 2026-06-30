DO $$
    BEGIN
        CREATE TEXT SEARCH CONFIGURATION korean (COPY = simple);
    EXCEPTION
        WHEN duplicate_object THEN NULL;
    END
$$;