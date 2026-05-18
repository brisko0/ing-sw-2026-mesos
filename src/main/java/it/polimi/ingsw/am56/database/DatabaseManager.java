package it.polimi.ingsw.am56.database;

import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = System.getenv("SUPABASE_DB_URL");
    private static final String USER = System.getenv("SUPABASE_DB_USER");
    private static final String PASS = System.getenv("SUPABASE_DB_PASS");

    private Connection getConnection() throws SQLException {
        if (DB_URL == null || USER == null || PASS == null) {
            throw new SQLException("Credenziali del database mancanti! Configura le variabili d'ambiente (SUPABASE_DB_URL, SUPABASE_DB_USER, SUPABASE_DB_PASS).");
        }
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public void savePlayerResult(String nickname, int score, int gameSize, LocalDate date) {
        String sql = "INSERT INTO match_leaderboard (nickname, score, game_size, game_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nickname);
            pstmt.setInt(2, score);
            pstmt.setInt(3, gameSize);
            pstmt.setDate(4, Date.valueOf(date));

            pstmt.executeUpdate();
            System.out.println("[DB] Risultato salvato per: " + nickname);

        } catch (SQLException e) {
            System.err.println("[DB ERROR] Impossibile salvare il risultato: " + e.getMessage());
        }
    }

    public List<EndGameDTO> getGlobalLeaderboard(int gameSize) {
        List<EndGameDTO> leaderboard = new ArrayList<>();
        String sql = "SELECT nickname, score, game_date FROM match_leaderboard WHERE game_size = ? ORDER BY score DESC, game_date ASC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, gameSize);
            ResultSet rs = pstmt.executeQuery();

            int rank = 1;
            while (rs.next()) {
                String nick = rs.getString("nickname");
                int score = rs.getInt("score");
                String date = rs.getDate("game_date").toString();

                // (Nota: per la global board passiamo isWinner=false e food=0 perché non servono)
                leaderboard.add(new EndGameDTO(rank, nick, score, 0, false, date));
                rank++;
            }

        } catch (SQLException e) {
            System.err.println("[DB ERROR] Impossibile recuperare la classifica: " + e.getMessage());
        }

        return leaderboard;
    }
}