class Lox {
    public static void main(String[] args) {
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

    private static void runFile(String path) {

    }

    private static void runPrompt() {

    }
}
