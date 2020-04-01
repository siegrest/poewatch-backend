DROP TRIGGER IF EXISTS update_stash_updates ON stash;

CREATE TRIGGER update_stash_updates
    BEFORE UPDATE
    ON stash
    FOR EACH ROW
EXECUTE PROCEDURE increment_updates_counter();
