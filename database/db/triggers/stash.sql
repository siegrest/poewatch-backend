DROP TRIGGER IF EXISTS update_stash_seen ON stash;
DROP TRIGGER IF EXISTS update_stash_updates ON stash;

CREATE TRIGGER update_stash_seen
    BEFORE UPDATE
    ON stash
    FOR EACH ROW
EXECUTE PROCEDURE update_seen_timestamp();

CREATE TRIGGER update_stash_updates
    BEFORE UPDATE
    ON stash
    FOR EACH ROW
EXECUTE PROCEDURE increment_updates_counter();
