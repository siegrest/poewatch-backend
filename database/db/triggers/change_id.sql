DROP TRIGGER IF EXISTS update_change_id_updated ON change_id;

CREATE TRIGGER update_change_id_updated
    BEFORE INSERT
    ON change_id
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_timestamp();
