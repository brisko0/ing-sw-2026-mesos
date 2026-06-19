package it.polimi.ingsw.am56.model.exceptions;

/**
 * Checked exception thrown when a player attempts an action that is not allowed in the current
 * game state — e.g. acting out of turn, picking an unavailable card, or making a move that
 * violates the rules. The {@code message} is meant to be human-readable and is propagated back
 * to the client as feedback.
 */
public class InvalidActionException extends Exception {
  /**
   * Creates the exception with a message describing why the action was rejected.
   *
   * @param message human-readable explanation of the invalid action
   */
  public InvalidActionException(String message) {
    super(message);
  }
}
