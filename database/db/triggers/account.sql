DROP TRIGGER IF EXISTS update_account_seen ON account;

CREATE TRIGGER update_account_seen
    BEFORE UPDATE
    ON account
    FOR EACH ROW
EXECUTE PROCEDURE update_seen_timestamp();
