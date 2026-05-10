import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

/**
 * PA #2 -- Battleship
 * * Implement a single-player Battleship game on a 10x10 board.
 */
public class Battleship {

    private static final int  BOARD_SIZE  = 10;
    private static final long RANDOM_SEED = Long.parseLong(System.getProperty("seed", "2026"));

    // === Board State ===
    private char[][] baseBoard;
    private boolean[][] shot;
    private Ship[][] shipRef;
    private int score;

    // === Entry Point ===
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            
            StartupConfig config;
            try {
                config = parseStartupLine(reader);
            } catch (BombInputException e) {
                System.out.println("BombInputException");
                return;
            } catch (ModeInputException e) {
                System.out.println("ModeInputException");
                return;
            }

            Battleship game = new Battleship();
            game.clearBoard();
            game.initializeBoard(config.fileName);
            game.play(config.bombs, config.mode, reader);

        } catch (IOException e) {
            System.out.println("IOException");
        }
    }

    // === Startup Parsing ===
    private static StartupConfig parseStartupLine(BufferedReader reader) 
            throws IOException, BombInputException, ModeInputException {
        
        String line = reader.readLine();
        if (line == null || line.trim().isEmpty()) {
            throw new BombInputException();
        }

        // Split into at most 3 parts to handle spaces in the file name
        String[] parts = line.split(" ", 3);
        if (parts.length < 3) {
            throw new ModeInputException();
        }

        int bombs;
        try {
            bombs = Integer.parseInt(parts[0]);
            if (bombs <= 0) {
                throw new BombInputException();
            }
        } catch (NumberFormatException e) {
            throw new BombInputException();
        }

        String modeStr = parts[1];
        Mode mode;
        if (modeStr.equals("d") || modeStr.equals("D")) {
            mode = Mode.DEBUG;
        } else if (modeStr.equals("r") || modeStr.equals("R")) {
            mode = Mode.RELEASE;
        } else {
            throw new ModeInputException();
        }

        String fileName = parts[2];

        return new StartupConfig(bombs, mode, fileName);
    }

    // === Board Initialisation ===
    private void clearBoard() {
        baseBoard = new char[BOARD_SIZE][BOARD_SIZE];
        shot = new boolean[BOARD_SIZE][BOARD_SIZE];
        shipRef = new Ship[BOARD_SIZE][BOARD_SIZE];
        score = 0;

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                baseBoard[r][c] = ' ';
                shot[r][c] = false;
                shipRef[r][c] = null;
            }
        }
    }

    private void initializeBoard(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.exists() && !file.isDirectory()) {
            loadBoardFromFile(file);
        } else {
            generateRandomBoard(new Random(RANDOM_SEED));
        }
    }

    private void loadBoardFromFile(File file) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(file));
        for (int r = 0; r < BOARD_SIZE; r++) {
            String line = fileReader.readLine();
            if (line == null) line = "";
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (c < line.length()) {
                    char ch = line.charAt(c);
                    if (ch == 'A' || ch == 'B' || ch == 'S' || ch == 'D' || ch == 'P') {
                        baseBoard[r][c] = ch;
                    }
                }
            }
        }
        fileReader.close();

        // Assign ships to references for score calculation
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (baseBoard[r][c] != ' ' && shipRef[r][c] == null) {
                    char type = baseBoard[r][c];
                    Ship s = createShip(type);
                    shipRef[r][c] = s;
                }
            }
        }
    }

    private Ship createShip(char type) {
        if (type == 'A') return new AircraftCarrier();
        if (type == 'B') return new BattleshipShip();
        if (type == 'S') return new Submarine();
        if (type == 'D') return new Destroyer();
        if (type == 'P') return new PatrolBoat();
        return null;
    }

    private void generateRandomBoard(Random rng) {
        // Order must be exact for deterministic output (TC7)
        Ship[] shipsToPlace = {
            new AircraftCarrier(),
            new BattleshipShip(), new BattleshipShip(),
            new Submarine(), new Submarine(),
            new Destroyer(),
            new PatrolBoat(), new PatrolBoat(), new PatrolBoat(), new PatrolBoat()
        };

        for (int i = 0; i < shipsToPlace.length; i++) {
            Ship ship = shipsToPlace[i];
            boolean placed = false;
            
            while (!placed) {
                boolean horizontal = rng.nextBoolean();
                int row = rng.nextInt(10);
                int col = rng.nextInt(10);

                if (canPlace(row, col, ship.size, horizontal)) {
                    placeShip(ship, row, col, horizontal);
                    placed = true;
                }
            }
        }
    }

    private boolean canPlace(int row, int col, int size, boolean horizontal) {
        if (horizontal) {
            if (col + size > BOARD_SIZE) return false;
            for (int r = row - 1; r <= row + 1; r++) {
                for (int c = col - 1; c <= col + size; c++) {
                    if (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE) {
                        if (baseBoard[r][c] != ' ') return false;
                    }
                }
            }
        } else {
            if (row + size > BOARD_SIZE) return false;
            for (int r = row - 1; r <= row + size; r++) {
                for (int c = col - 1; c <= col + 1; c++) {
                    if (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE) {
                        if (baseBoard[r][c] != ' ') return false;
                    }
                }
            }
        }
        return true;
    }

    private void placeShip(Ship ship, int row, int col, boolean horizontal) {
        if (horizontal) {
            for (int i = 0; i < ship.size; i++) {
                baseBoard[row][col + i] = ship.type;
                shipRef[row][col + i] = ship;
            }
        } else {
            for (int i = 0; i < ship.size; i++) {
                baseBoard[row + i][col] = ship.type;
                shipRef[row + i][col] = ship;
            }
        }
    }

    // === Game Loop ===
    private void play(int bombs, Mode mode, BufferedReader reader) throws IOException {
        int usedBombs = 0;

        while (usedBombs < bombs) {
            if (mode == Mode.DEBUG) {
                printBoard();
            }

            String input = reader.readLine();
            if (input == null) break;
            
            String[] tokens = input.trim().split("\\s+");
            if (tokens.length == 0 || tokens[0].isEmpty()) continue;
            String target = tokens[0];

            try {
                int[] coords = parseCoordinate(target);
                int r = coords[0];
                int c = coords[1];
                
                shoot(r, c);
                usedBombs++;

            } catch (HitException e) {
                // Do not increment bomb counter, just print and continue
                System.out.println("Try again");
            }
        }

        // Always print final board at the end
        printBoard();
        System.out.println("Score " + score);
    }

    private int[] parseCoordinate(String token) throws HitException {
        if (token == null || token.length() < 2) {
            throw new HitException();
        }

        char colChar = Character.toUpperCase(token.charAt(0));
        if (colChar < 'A' || colChar > 'J') {
            throw new HitException();
        }
        int col = colChar - 'A';

        int row;
        try {
            row = Integer.parseInt(token.substring(1)) - 1;
        } catch (NumberFormatException e) {
            throw new HitException();
        }

        if (row < 0 || row > 9) {
            throw new HitException();
        }

        if (shot[row][col]) {
            throw new HitException();
        }

        return new int[]{row, col};
    }

    private void shoot(int row, int col) {
        shot[row][col] = true;
        if (baseBoard[row][col] == ' ') {
            System.out.println("Miss");
        } else {
            System.out.println("Hit " + baseBoard[row][col]);
            if (shipRef[row][col] != null) {
                score += shipRef[row][col].size;
            }
        }
    }

    // === Display ===
    private void printBoard() {
        System.out.println("  A B C D E F G H I J");
        System.out.println("  - - - - - - - - - -");
        
        for (int r = 0; r < BOARD_SIZE; r++) {
            StringBuilder sb = new StringBuilder();
            
            // Format row number with pipe, matching .out exact spacing
            sb.append((r + 1)).append(" | ");

            for (int c = 0; c < BOARD_SIZE; c++) {
                if (!shot[r][c]) {
                    if (baseBoard[r][c] == ' ') {
                        sb.append("  ");
                    } else {
                        sb.append(baseBoard[r][c]).append(" ");
                    }
                } else {
                    if (baseBoard[r][c] == ' ') {
                        sb.append("X ");
                    } else {
                        
                        sb.append("X").append(Character.toLowerCase(baseBoard[r][c])).append(" ");
                    }
                }
            }
            
            // Strip trailing spaces manually to avoid regex overhead
            String line = sb.toString();
            int i = line.length() - 1;
            while (i >= 0 && line.charAt(i) == ' ') {
                i--;
            }
            System.out.println(line.substring(0, i + 1));
        }
    }

    // === Inner Types ===
    private enum Mode { DEBUG, RELEASE }

    private static class StartupConfig {
        final int    bombs;
        final Mode   mode;
        final String fileName;

        StartupConfig(int bombs, Mode mode, String fileName) {
            this.bombs    = bombs;
            this.mode     = mode;
            this.fileName = fileName;
        }
    }

    // === Ship Hierarchy ===
    private abstract static class Ship {
        final char type;
        final int  size;
        int        hits;

        Ship(char type, int size) {
            this.type = type;
            this.size = size;
            this.hits = 0;
        }
    }

    private static final class AircraftCarrier extends Ship {
        AircraftCarrier() { super('A', 6); }
    }

    private static final class BattleshipShip extends Ship {
        BattleshipShip() { super('B', 4); }
    }

    private static final class Submarine extends Ship {
        Submarine() { super('S', 3); }
    }

    private static final class Destroyer extends Ship {
        Destroyer() { super('D', 3); }
    }

    private static final class PatrolBoat extends Ship {
        PatrolBoat() { super('P', 2); }
    }

    // === Exceptions ===
    private static class BombInputException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    private static class ModeInputException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    private static class HitException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}