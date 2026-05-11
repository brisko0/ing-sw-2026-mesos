package it.polimi.ingsw.am56.view.TUI;

import java.util.Scanner;

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
                drawer.printError("Inserisci un numero valido.");
            }
        }
    }

    public int readIntWithRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            drawer.printError("Valore non valido. Inserisci un numero tra " + min + " e " + max + ".");
        }
    }

    public void close() {
        scanner.close();
    }
}