DROP TRIGGER IF EXISTS update_character_seen ON character;

CREATE TRIGGER update_character_seen
    BEFORE UPDATE
    ON character
    FOR EACH ROW
EXECUTE PROCEDURE update_seen_timestamp();
