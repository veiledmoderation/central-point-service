
package org.lukecreator.aw;

import org.lukecreator.aw.data.AWPlayer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Manages the SQLite backend.
 */
public class AWDatabase {
    private static final String DB_URL = System.getenv("AW_DB_URL");

    /**
     * The active database connection.
     */
    public static Connection connection = null;

    public static void init() throws Exception {
        if (DB_URL == null || DB_URL.isBlank()) {
            throw new Exception("AW_DB_URL is not configured.");
        }

        connection = DriverManager.getConnection(DB_URL);
        connection.setAutoCommit(true);

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS players (
                    user_id INTEGER PRIMARY KEY,
                    username TEXT,
                    is_appeal_blacklisted BOOLEAN NOT NULL DEFAULT FALSE,
                    appeal_blacklist_reason TEXT,
                    appeal_blacklist_date INTEGER NOT NULL DEFAULT 0,
                    appeal_blacklist_issuer INTEGER NOT NULL DEFAULT 0
                )
            """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS stats (
                    user_id INTEGER PRIMARY KEY,
                    punches INTEGER NOT NULL DEFAULT 0,
                    gamepasses TEXT
                )
            """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS bans (
                    user_id INTEGER NOT NULL,
                    responsible_moderator INTEGER,
                    reason TEXT,
                    starts INTEGER NOT NULL,
                    ends INTEGER,
                    linked_ticket INTEGER,
                    is_legacy BOOLEAN NOT NULL DEFAULT FALSE,
                    PRIMARY KEY (user_id, starts)
                )
            """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS unbans (
                    user_id INTEGER NOT NULL,
                    responsible_moderator INTEGER,
                    date INTEGER NOT NULL
                )
            """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS punch_update_records (
                    user_id INTEGER NOT NULL,
                    responsible_moderator INTEGER,
                    date INTEGER NOT NULL,
                    old_punches INTEGER NOT NULL,
                    new_punches INTEGER NOT NULL
                )
            """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tickets (
                    ticket_id INTEGER PRIMARY KEY,
                    discord_channel_id INTEGER NOT NULL,
                    type INTEGER NOT NULL,
                    opened_timestamp INTEGER NOT NULL,
                    is_open BOOLEAN NOT NULL,
                    close_reason TEXT,
                    closed_by INTEGER,
                    input_questions TEXT,
                    owner_discord_id INTEGER NOT NULL
                )
            """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS evidence (
                    evidence_id INTEGER PRIMARY KEY,
                    timestamp INTEGER NOT NULL,
                    accused_user INTEGER,
                    details TEXT,
                    url TEXT
                )
            """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ticket_evidence_link (
                    ticket_id INTEGER NOT NULL,
                    evidence_id INTEGER NOT NULL
                )
            """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ban_evidence_link (
                    user_id INTEGER NOT NULL,
                    starts_timestamp INTEGER NOT NULL,
                    evidence_id INTEGER NOT NULL
                )
            """);
        }

        System.out.println("Connected to database and verified database tables.");
    }

    /**
     * Loads an Ability Wars player from the database.
     */
    public static AWPlayer loadPlayer(long userId) {
        return AWPlayer.loadFromDatabase(userId, true, true, true, true);
    }

    /**
     * Loads an Ability Wars player by username.
     */
    public static AWPlayer loadPlayerByName(String username) {
        RobloxAPI.User user = RobloxAPI.getUserByCurrentUsername(username);
        if (user == null)
            return null;

        return loadPlayer(user.userId());
    }

    /**
     * Loads a player with optional database information.
     */
    public static AWPlayer loadPlayer(
            long userId,
            boolean loadStats,
            boolean loadBans,
            boolean loadUnbans,
            boolean loadPunchUpdates
    ) {
        return AWPlayer.loadFromDatabase(
                userId,
                loadStats,
                loadBans,
                loadUnbans,
                loadPunchUpdates
        );
    }

    /**
     * Loads a player by username with optional database information.
     */
    public static AWPlayer loadPlayerByName(
            String username,
            boolean loadStats,
            boolean loadBans,
            boolean loadUnbans,
            boolean loadPunchUpdates
    ) {
        RobloxAPI.User user = RobloxAPI.getUserByCurrentUsername(username);
        if (user == null)
            return null;

        return loadPlayer(
                user.userId(),
                loadStats,
                loadBans,
                loadUnbans,
                loadPunchUpdates
        );
    }
}
