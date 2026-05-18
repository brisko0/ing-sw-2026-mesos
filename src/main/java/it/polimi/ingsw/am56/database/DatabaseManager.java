package it.polimi.ingsw.am56.database;

import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    // Sostituisci con la TUA stringa presa da Supabase
    private static final String DB_URL = "jdbc:postgresql://[TUO-HOST].supabase.com:6543/postgres";
    private static final String USER = "postgres.[TUO-PROGETTO]";
    private static final String PASS = "la_tua_password";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    /**
     * Salva il risultato di un singolo giocatore a fine partita
     */
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

    /**
     * Recupera la classifica globale per un determinato numero di giocatori
     */
    public List<EndGameDTO> getGlobalLeaderboard(int gameSize) {
        List<EndGameDTO> leaderboard = new ArrayList<>();
        // Ordina per punteggio (decrescente) e in caso di parità per data (dal più vecchio al più recente)
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