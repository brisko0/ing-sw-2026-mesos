package it.polimi.ingsw.am56.database;

import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-backed {@link LeaderboardDAO} that stores match results in a PostgreSQL (Supabase) database.
 * Connection details are read from the {@code SUPABASE_DB_URL}, {@code SUPABASE_DB_USER} and
 * {@code SUPABASE_DB_PASS} environment variables.
 * <p>
 * All persistence is best-effort: SQL failures are logged and swallowed rather than propagated, so a
 * database problem never aborts a game — at worst the leaderboard is not updated or comes back empty.
 */
public class DatabaseManager implements LeaderboardDAO {

    private static final String DB_URL = System.getenv("SUPABASE_DB_URL");
    private static final String USER = System.getenv("SUPABASE_DB_USER");
    private static final String PASS = System.getenv("SUPABASE_DB_PASS");

    /**
     * Opens a new JDBC connection using the configured credentials.
     *
     * @return an open database connection
     * @throws SQLException if any of the {@code SUPABASE_DB_*} environment variables is missing, or
     *                      the connection cannot be established
     */
    private Connection getConnection() throws SQLException {
        if (DB_URL == null || USER == null || PASS == null) {
            throw new SQLException("Credenziali del database mancanti! Configura le variabili d'ambiente (SUPABASE_DB_URL, SUPABASE_DB_USER, SUPABASE_DB_PASS).");
        }
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    /**
     * Batch-inserts one row per player into the {@code match_leaderboard} table, storing each
     * player's nickname and score together with the game size and date. Only the nickname and score
     * are persisted from each {@link EndGameDTO}; rank, food and winner flag are not stored. Any
     * {@link SQLException} is logged and swallowed.
     *
     * @param gameSize the number of players in the match
     * @param results  the per-player results to insert
     * @param date     the match date
     */
    @Override
    public void saveMatchResults(int gameSize, List<EndGameDTO> results, LocalDate date) {
        String sql = "INSERT INTO match_leaderboard (nickname, score, game_size, game_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (EndGameDTO result : results) {
                pstmt.setString(1, result.nickname());
                pstmt.setInt(2, result.score());
                pstmt.setInt(3, gameSize);
                pstmt.setDate(4, Date.valueOf(date));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("[DB] Match results saved for " + results.size() + " players.");

        } catch (SQLException e) {
            System.err.println("[DB ERROR] Unable to save results: " + e.getMessage());
        }
    }

    /**
     * Reads the all-time leaderboard for the given game size: it keeps each nickname's best score
     * (via Postgres {@code DISTINCT ON}) and orders the result from highest to lowest score,
     * assigning a 1-based rank by position. The returned {@link EndGameDTO}s carry the stored
     * nickname, score and date; food is set to 0 and the winner flag to {@code false}, since those
     * are not persisted. Any {@link SQLException} is logged and swallowed, yielding an empty list.
     *
     * @param gameSize the player count whose leaderboard to read
     * @return the ranked leaderboard entries, or an empty list if none exist or a query fails
     */
    @Override
    public List<EndGameDTO> getGlobalLeaderboard(int gameSize) {
        List<EndGameDTO> leaderboard = new ArrayList<>();
        String sql = """
        SELECT nickname, score, game_date
        FROM (
            SELECT DISTINCT ON (nickname) nickname, score, game_date
            FROM match_leaderboard
            WHERE game_size = ?
            ORDER BY nickname, score DESC
        ) best
        ORDER BY score DESC
        """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, gameSize);

            try (ResultSet rs = pstmt.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    String nick = rs.getString("nickname");
                    int score = rs.getInt("score");
                    String date = rs.getDate("game_date").toString();
                    leaderboard.add(new EndGameDTO(rank++, nick, score, 0, false, date));
                }
            }

        } catch (SQLException e) {
            System.err.println("[DB ERROR] Unable to retrieve leaderboard: " + e.getMessage());
        }

        return leaderboard;
    }
}