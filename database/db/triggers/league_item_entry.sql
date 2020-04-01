DROP TRIGGER IF EXISTS update_league_item_entry_seen ON league_item_entry;
DROP TRIGGER IF EXISTS update_league_item_entry_updates ON league_item_entry;

CREATE TRIGGER update_league_item_entry_seen
    BEFORE UPDATE
    ON league_item_entry
    FOR EACH ROW
EXECUTE PROCEDURE update_seen_timestamp();

CREATE TRIGGER update_league_item_entry_updates
    BEFORE UPDATE
    ON league_item_entry
    FOR EACH ROW
EXECUTE PROCEDURE increment_updates_counter();
