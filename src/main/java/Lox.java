import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

class Lox {
    public static void main(String[] args) throws IOException {
        switch (args.length) {
            case 0:
                runPrompt();
            case 1:
                runFile(args[0]);
            default: {
                System.out.println("Usage jlox [script]");
                System.exit(64);
            }
        }
    }

    private static void runFile(String path) throws IOException {
        run(Files.readString(Path.of(path)));
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        while (true) {
            System.out.println("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
        }
    }

    private static void run(String source) {

    }
}
