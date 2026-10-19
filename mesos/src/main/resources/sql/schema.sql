-- =====================================================================
-- Mesos — Leaderboard schema (PostgreSQL)
-- =====================================================================
-- Supports the "Classifica partite su DB" advanced feature.
-- Run this once on a PostgreSQL 13+ instance before starting the server.
--
-- The server reads connection parameters from the environment variables:
--   SUPABASE_DB_URL   (e.g. jdbc:postgresql://host:5432/postgres?sslmode=require)
--   SUPABASE_DB_USER
--   SUPABASE_DB_PASS
--
-- Used by it.polimi.ingsw.am56.database.DatabaseManager.
-- =====================================================================

CREATE TABLE IF NOT EXISTS match_leaderboard (
                                                 id         SERIAL       PRIMARY KEY,
                                                 nickname   VARCHAR(64)  NOT NULL,
    score      INTEGER      NOT NULL,
    game_size  INTEGER      NOT NULL CHECK (game_size BETWEEN 2 AND 5),
    game_date  DATE         NOT NULL
    );

-- Composite index aligned with the DISTINCT ON / ORDER BY pattern used
-- by getGlobalLeaderboard(gameSize): the inner query orders by
-- (nickname, score DESC) within a filter on game_size, picking the
-- best score per player; the outer sort by score DESC is then cheap
-- because it operates on at most one row per nickname.
CREATE INDEX IF NOT EXISTS idx_leaderboard_size_nick_score
    ON match_leaderboard (game_size, nickname, score DESC);