DROP TRIGGER IF EXISTS update_league_item_entry_updates ON league_item_entry;

CREATE TRIGGER update_league_item_entry_updates
    BEFORE UPDATE
    ON league_item_entry
    FOR EACH ROW
EXECUTE PROCEDURE increment_updates_counter();
