CREATE OR REPLACE FUNCTION update_found_timestamp()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.found = now();
    RETURN NEW;
END;
$$ language 'plpgsql';
