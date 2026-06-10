package it.polimi.ingsw.am56.view.TUI;

import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class TUIInputReader {
    private final Scanner scanner;
    private final TUIDrawer drawer;

    public TUIInputReader(TUIDrawer drawer) {
        this.scanner = new Scanner(System.in);
        this.drawer = drawer;
    }

    public String readString(String prompt) {
        System.out.print(prompt + " ");
        return scanner.nextLine().trim();
    }

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

    public int readIntWithRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            drawer.printError("Invalid value. Please enter a number between " + min + " and " + max + ".");
        }
    }

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

    public int readIntFromSet(Set<Integer> allowed, String prompt) {
        String hint = allowed.stream().sorted().map(String::valueOf).collect(Collectors.joining(", "));
        while (true) {
            int value = readInt(prompt + " [" + hint + "]:");
            if (allowed.contains(value)) return value;
            drawer.printError("Invalid choice. Allowed values: " + hint);
        }
    }

    public void close() {
        scanner.close();
    }
}
