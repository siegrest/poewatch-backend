CREATE OR REPLACE FUNCTION update_seen_timestamp()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.seen = now();
    RETURN NEW;
END;
$$ language 'plpgsql';
