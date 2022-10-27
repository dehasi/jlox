package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class Lox {

    private static final Interpreter interpreter = new Interpreter();
    private static boolean hasError = false;
    private static boolean hasRuntimeError = false;

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
        if (hasError) System.exit(65);
        if (hasRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        while (true) {
            System.out.println("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hasError = false;
        }
    }

    /* visible for test */
    static String run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        if (hasError) return null;
        else return new Interpreter().interpret(expression);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.printf("[line %d] Error%s:%s\n", line, where, message);
        hasError = true;
    }

    public static void error(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), "'" + token.lexeme() + "'", message);
        }
    }

    public static String runtimeError(RuntimeError error) {
        String message = error.getMessage() + "\n[line " + error.token.line() + "]";
        hasRuntimeError = true;
        System.err.println(message);
        return message;
    }
}
