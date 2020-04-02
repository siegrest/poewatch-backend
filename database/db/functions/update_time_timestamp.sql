CREATE OR REPLACE FUNCTION update_time_timestamp()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.time = now();
    RETURN NEW;
END;
$$ language 'plpgsql';
