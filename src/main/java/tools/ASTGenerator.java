package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

/* In the book it's called GenerateAst */
class ASTGenerator {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: ast-generator <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", List.of(
                "Assign:   Token name, Expr value",
                "Binary:   Expr left, Token operator, Expr right",
                "Call:     Expr callee, Token paren, List<Expr> arguments",
                "Get:      Expr object, Token name",
                "Grouping: Expr expression",
                "Literal:  Object value",
                "Logical:  Expr left, Token operator, Expr right",
                "Set:      Expr object, Token name, Expr value",
                "Super:    Token keyword, Token method",
                "This:     Token keyword",
                "Unary:    Token operator, Expr right",
                "Variable: Token name"
        ), List.of("import java.util.List;"));

        defineAst(outputDir, "Stmt", List.of(
                "Block      : List<Stmt> statements",
                "Class      : Token name, Expr.Variable superClass, List<Stmt.Function> methods",
                "Expression : Expr expression",
                "Function   : Token name, List<Token> params, List<Stmt> body",
                "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Print      : Expr expression",
                "Return     : Token keyword, Expr value",
                "Var        : Token name, Expr initializer",
                "While      : Expr condition, Stmt body"
        ), List.of("import java.util.List;"));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types, List<String> imports) throws IOException {
        var path = Path.of(outputDir, baseName + ".java").toAbsolutePath().toString();
        try (PrintWriter writer = new PrintWriter(path, UTF_8)) {
            writer.println("package jlox;");
            if (!imports.isEmpty()) {
                writer.println();
                writer.println(String.join("\n", imports));
            }
            writer.println();
            writer.println("abstract sealed class " + baseName + permits(baseName, types) + " {");
            writer.println();
            writer.println("    abstract <R> R accept(Visitor<R> visitor);");

            defineVisitor(writer, baseName, types);
            for (String type : types) {
                var className = type.split(":")[0].trim();
                var fields = type.split(":")[1].trim();
                defineType(writer, baseName, className, fields);
            }
            writer.println("}");
        }
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        String tab = " ".repeat(4);
        int nesting = 1;
        writer.println();
        writer.println(tab.repeat(nesting) + "interface Visitor<R> {");
        ++nesting;
        var methods = types.stream()
                .map(type -> type.split(":")[0].trim())
                .map(type -> String.format("%sR visit%s%s(%s %s);",
                        tab.repeat(2), type, baseName, type, baseName.toLowerCase()))
                .collect(joining("\n\n"));

        writer.println(methods);
        --nesting;
        writer.println(tab.repeat(nesting) + "}");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fields) {
        String tab = " ".repeat(4);
        int nesting = 1;
        // define class
        writer.println();
        writer.println(tab.repeat(nesting) + "static final class " + className + " extends " + baseName + " {");
        // define fields
        ++nesting;
        for (String field : fields.split(",")) {
            writer.println(tab.repeat(nesting) + "final " + field.trim() + ";");
        }
        // define constructor: start
        writer.println();
        writer.println(tab.repeat(nesting) + className + "(" + fields + ") {");
        ++nesting;
        for (String field : fields.split(",")) {
            var name = field.trim().split(" ")[1].trim();
            writer.println(tab.repeat(nesting) + "this." + name + " = " + name + ";");
        }
        --nesting;
        writer.println(tab.repeat(nesting) + "}");
        // define constructor: finish
        // implement visitor: start
        writer.println();
        writer.println(tab.repeat(nesting) + "@Override <R> R accept(Visitor<R> visitor) {");
        ++nesting;
        writer.println(tab.repeat(nesting) + "return visitor.visit" + className + baseName + "(this);");
        --nesting;
        writer.println(tab.repeat(nesting) + "}");
        // implement visitor: finish
        writer.println(tab + "}");
    }

    private static String permits(String baseName, List<String> types) {
        return " permits " + types.stream()
                .map(t -> t.split(":")[0].trim())
                .map(t -> baseName + "." + t)
                .collect(joining(", "));
    }
}
