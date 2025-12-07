package db.migration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * Migrate user_tokens to store only SHA-256 hashes and add token_version to users.
 * Implemented in Java to keep compatibility with both Postgres and H2 during tests.
 */
public class V6__user_tokens_hash_and_version extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS token_version INTEGER NOT NULL DEFAULT 0");
            stmt.execute("ALTER TABLE user_tokens ADD COLUMN IF NOT EXISTS token_hash VARCHAR(128)");
        }

        boolean hasTokenColumn = columnExists(conn, "USER_TOKENS", "TOKEN");
        if (hasTokenColumn) {
            try (Statement select = conn.createStatement();
                 ResultSet rs = select.executeQuery("SELECT id, token FROM user_tokens WHERE token_hash IS NULL")) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String raw = rs.getString("token");
                    String hash = sha256Hex(raw);
                    try (var update = conn.prepareStatement("UPDATE user_tokens SET token_hash = ? WHERE id = ?")) {
                        update.setString(1, hash);
                        update.setLong(2, id);
                        update.executeUpdate();
                    }
                }
            }
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE user_tokens SET token_hash = '" + sha256Hex("") + "' WHERE token_hash IS NULL");
            stmt.execute("ALTER TABLE user_tokens ALTER COLUMN token_hash SET NOT NULL");
        } catch (SQLException ignored) {
            // some dialects use different syntax; try H2/Postgres variant
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE user_tokens ALTER COLUMN token_hash SET NOT NULL");
            }
        }

        dropIfExists(conn, "ALTER TABLE user_tokens DROP CONSTRAINT user_tokens_token_key");
        dropIfExists(conn, "ALTER TABLE user_tokens DROP CONSTRAINT uq_user_tokens_token");

        if (hasTokenColumn) {
            dropIfExists(conn, "ALTER TABLE user_tokens DROP COLUMN IF EXISTS token");
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE user_tokens ADD CONSTRAINT uq_user_tokens_token_hash UNIQUE (token_hash)");
        } catch (SQLException ignored) {
            // already exists
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private void dropIfExists(Connection conn, String sql) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException ignored) {
            // ignore
        }
    }

    private String sha256Hex(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashed = digest.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hashed) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
