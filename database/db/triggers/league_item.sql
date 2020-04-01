DROP TRIGGER IF EXISTS update_league_item_seen ON league_item;

CREATE TRIGGER update_league_item_seen
    BEFORE UPDATE
    ON league_item
    FOR EACH ROW
EXECUTE PROCEDURE update_seen_timestamp();
