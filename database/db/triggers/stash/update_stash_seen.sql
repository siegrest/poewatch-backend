DROP TRIGGER IF EXISTS update_stash_seen ON stash;

CREATE TRIGGER update_stash_seen
    BEFORE UPDATE
    ON stash
    FOR EACH ROW
EXECUTE PROCEDURE update_seen_timestamp();
