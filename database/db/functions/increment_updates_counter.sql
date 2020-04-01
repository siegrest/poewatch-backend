CREATE OR REPLACE FUNCTION increment_updates_counter()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updates = NEW.updates + 1;
    RETURN NEW;
END;
$$ language 'plpgsql';
