# Mesos — Software Engineering 2026 · Final grade: 30/30

Digital, distributed implementation of the board game **Mesos**, developed as the final project of the
*Software Engineering* course (Prova Finale di Ingegneria del Software) at **Politecnico di Milano**, A.Y. 2025/2026.

One server hosts the match; players join from a **JavaFX GUI** or a **terminal TUI**, over **TCP Socket** or
**Java RMI**. Clients using different technologies can play in the same game.

> The full source code is in the [`mesos/`](mesos) folder.

| Requirement | Status |
|---|---|
| Complete rules (2–5 players) | ✅ |
| TUI + GUI (JavaFX) | ✅ |
| Socket + RMI | ✅ |
| Advanced feature: **Persistence** | ✅ |
| Advanced feature: **DB leaderboard** | ✅ |

---

## 1. Implemented Features

### Complete Game Rules
The **complete rules** of Mesos are implemented for **2–5 players**. A game lasts **10 rounds**, until the Tribe
deck is exhausted. Each round goes through the following phases, modelled as a state machine:

1. **Totem Placement**: in turn order, each player moves their totem onto a free Offer tile.
2. **Card Picking**: offers are resolved left to right. Players take Character/Building cards from the upper
   and/or lower row (paying the Food cost of buildings, discounted by Builders), then return their totem to the
   Turn Order tile, collecting its Food bonus or paying the last-place penalty.
3. **Extra Card**: players owning the matching building may take one extra card from the upper row.
4. **Round End**: Event cards in the lower row are resolved (Hunt, Shamanic Ritual, Cave Paintings, with
   Sustenance always last); the lower row is discarded, the upper row shifts down and is refilled from the deck.
   When a card from the next Era is revealed, a new **Era** begins and its Building cards enter the game.

At the end of the 10th round all visible events are resolved and the final score is computed from Builders,
Inventors (count × distinct invention icons), Artists and Building cards (printed points + end-game effects).
The player with the most Prestige Points wins; ties are broken by Food, otherwise the victory is shared.

All card types are implemented:
- **Character cards**: 6 types (Builder, Inventor, Artist, Gatherer, Hunter, Shaman), each with its own
  in-game effect and end-game scoring.
- **Building cards**: 21 cards across 3 Eras, implemented as 14 effect classes (event bonuses, Food bonuses,
  end-game points, extra card pick, ...).
- **Event cards**: Hunt, Sustenance, Shamanic Ritual and Cave Paintings, with values growing each Era, plus the
  2 **Final Event** cards at the bottom of the deck.

Every player can see the full state of all the other players (cards, food, points) at any time, as in the
physical game. Nicknames are unique and validated by the server.

### User Interfaces
- **TUI**: fully playable, colour-coded text interface for any terminal.
- **GUI**: JavaFX application (FXML + CSS) with dedicated screens for connection choice, pre-lobby, lobby,
  game board and end-game ranking.

### Network Protocols
The client chooses the protocol at startup:
- **Socket**: TCP with Java object serialization, port `12345`
- **RMI**: Remote Method Invocation, port `1099`

Both transports sit behind the same abstractions (`ServerAdapter`, `ClientConnection`, `ConnectionFactory`), so
the game logic is fully protocol-agnostic. The server listens on both at once, and RMI and Socket clients share
the same match. A heartbeat detects dropped connections: if a player leaves or loses connection, the game is
terminated and all players are notified, as required by the specification.

### Game Persistence (advanced feature)
The server saves the game state to `mesos_save.dat` after **every game action**. Writes are **atomic**: the
snapshot is first written to a `.tmp` file and then moved over the previous save, so a crash mid-write can never
corrupt it.

If the server crashes, on restart it detects the save, restores the match and opens a **reconnection window**:
clients automatically retry the connection and, once all players have rejoined with the same nicknames, the game
resumes exactly where it stopped. When a game ends normally, the save is deleted.

### Global Leaderboard (advanced feature)
At the end of each game, every player's result (nickname, score, number of players, date) is stored in a
**PostgreSQL** database. The end-game screen shows the match ranking and the global leaderboard for games with
the same number of players (best score per player). The feature is **optional**: if the database is not
configured or unreachable, the game runs normally without it.

## 2. Architecture

The system follows the **MVC** pattern in a distributed client-server setting.

| Layer | Main components | Design patterns |
|---|---|---|
| **Model** | `GameModel`, `Player`, `Board`, card hierarchy, factories | **State** (game phases), **Observer** (game events), **Factory** (cards, decks, board), **Template Method** (card effects) |
| **Controller** | `GameController`, `PlayerVirtualView`, `EndGameService` | Thread-safe request handling, per-client model → DTO translation |
| **Network** | `ServerAdapter`, `ClientConnection`, `ConnectionManager`, messages | **Command** (client requests), **Visitor** (server messages), **DTO** |
| **View** | JavaFX GUI, TUI | MVC, shared network layer for both clients |

Game phases handled by the state machine:
`PlayersFilling → TotemPlacement → CardPicking → ExtraUpperCardPicking → RoundEnding → GameEnding`

<p align="center">
  <img src="mesos/deliveries/UML/UML%20class%20diagram%20(model)%20-%20autogenerated.png" alt="Model UML class diagram" width="800">
</p>

[`mesos/deliveries`](mesos/deliveries) contains the full UML class diagrams, the **sequence diagrams** of every
game phase (create/join game, totem placement, card picking, round/game ending, disconnection) and the
**Javadoc**.

> **Note on the diagrams:** the hand-drawn UML diagrams (`UML_Model_Lab_1`, `UML_PreLab4`) are early,
> pre-implementation designs. During development the architecture evolved considerably, as new design patterns
> learned along the course (e.g. State, Visitor, Command) were adopted to improve the design. The
> **autogenerated** class diagrams, like the one above, are generated from the final source code and reflect the
> actual implementation.

The project includes **200 JUnit 5 unit tests** covering the model and the network layer.

## 3. Running the Project

### Prerequisites
- **JDK 25** (Maven is included through the wrapper)

### Build

```bash
cd mesos
./mvnw clean package
```

This creates three self-contained jars in `mesos/target/`.

### Start the server

```bash
java -jar target/server.jar
```

The server starts both the socket server (port `12345`) and the RMI registry (port `1099`). The save file
`mesos_save.dat` is created in the directory the server is launched from.

### Start a client

```bash
java -jar target/client-gui.jar    # graphical client (JavaFX)
java -jar target/client-tui.jar    # terminal client
```

At startup the client asks for the protocol (Socket/RMI) and the server IP: press Enter to use `localhost`.
To play on different machines, enter the server's IP and make sure ports `12345` and `1099` are reachable.

### Leaderboard database (optional)

1. Create the table on a PostgreSQL instance (local or cloud, e.g. Supabase) using
   [`schema.sql`](mesos/src/main/resources/sql/schema.sql).
2. Set the environment variables before starting the server:

| Variable | Description | Example |
|---|---|---|
| `SUPABASE_DB_URL` | JDBC URL of the database | `jdbc:postgresql://localhost:5432/postgres` |
| `SUPABASE_DB_USER` | Database user | `postgres` |
| `SUPABASE_DB_PASS` | Database password | `password` |

```bash
export SUPABASE_DB_URL="jdbc:postgresql://localhost:5432/postgres"
export SUPABASE_DB_USER="postgres"
export SUPABASE_DB_PASS="password"
java -jar target/server.jar
```

## Tech Stack

Java 25 · JavaFX 21 · Java RMI · TCP Sockets · Gson · PostgreSQL (JDBC) · JUnit 5 · Maven

## Team

Developed by a team of 4 students (group AM56):

- **Carlo Briscolini** ([@brisko0](https://github.com/brisko0))
- Adriano Condulmari
- Andrea Bonzi
- Jatinder Dhanda

The complete commit history of the original repository is preserved.
