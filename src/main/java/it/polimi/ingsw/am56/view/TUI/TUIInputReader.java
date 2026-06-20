package it.polimi.ingsw.am56.view.TUI;

import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reads user input from standard input for the TUI. It prints prompts and validates the input,
 * re-prompting until a valid value is entered, and uses the {@link TUIDrawer} to print error messages.
 */
public class TUIInputReader {
    private final Scanner scanner;
    private final TUIDrawer drawer;

    /**
     * Creates the reader over standard input.
     *
     * @param drawer the drawer used to print validation errors
     */
    public TUIInputReader(TUIDrawer drawer) {
        this.scanner = new Scanner(System.in);
        this.drawer = drawer;
    }

    /**
     * Prints the prompt and reads a line of input.
     *
     * @param prompt the prompt to display
     * @return the entered text, trimmed
     */
    public String readString(String prompt) {
        System.out.print(prompt + " ");
        return scanner.nextLine().trim();
    }

    /**
     * Reads an integer, re-prompting until the input is a valid number.
     *
     * @param prompt the prompt to display
     * @return the entered integer
     */
    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt + " ");
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                drawer.printError("Please enter a valid number.");
            }
        }
    }

    /**
     * Reads an integer within an inclusive range, re-prompting until it falls within it.
     *
     * @param prompt the prompt to display
     * @param min    the minimum accepted value (inclusive)
     * @param max    the maximum accepted value (inclusive)
     * @return the entered integer, within {@code [min, max]}
     */
    public int readIntWithRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            drawer.printError("Invalid value. Please enter a number between " + min + " and " + max + ".");
        }
    }

    /**
     * Reads a single letter (case-insensitive) and loops until the uppercased input is in
     * {@code allowed}. Returns the uppercased value.
     */
    public String readLetterFrom(Set<String> allowed, String prompt) {
        Set<String> normalized = allowed.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        String hint = normalized.stream().sorted().collect(Collectors.joining(", "));
        while (true) {
            String input = readString(prompt + " [" + hint + "]:").toUpperCase();
            if (normalized.contains(input)) return input;
            drawer.printError("Invalid choice. Allowed values: " + hint);
        }
    }

    /**
     * Reads an integer and loops until the value is in {@code allowed}.
     */
    public int readIntFromSet(Set<Integer> allowed, String prompt) {
        String hint = allowed.stream().sorted().map(String::valueOf).collect(Collectors.joining(", "));
        while (true) {
            int value = readInt(prompt + " [" + hint + "]:");
            if (allowed.contains(value)) return value;
            drawer.printError("Invalid choice. Allowed values: " + hint);
        }
    }

    /**
     * Closes the underlying input scanner.
     */
    public void close() {
        scanner.close();
    }
}
