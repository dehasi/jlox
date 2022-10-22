package jlox;

import org.junit.jupiter.api.Test;

import static jlox.TokenType.MINUS;
import static jlox.TokenType.STAR;
import static org.assertj.core.api.Assertions.assertThat;

class ASTPrinterTest {

    private final ASTPrinter printer = new ASTPrinter();

    @Test void print() {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(MINUS, "-", null, 1),
                        new Expr.Literal(123)),
                new Token(STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67)));


        String result = printer.print(expression);

        assertThat(result).isEqualTo("(* (- 123) (group 45.67))");
    }

    private static class ASTPrinter implements Expr.Visitor<String> {

        String print(Expr expr) {
            return expr.accept(this);
        }

        @Override public String visitBinaryExpr(Expr.Binary expr) {
            return parenthesize(expr.operator.lexeme(), expr.left, expr.right);
        }

        @Override public String visitGroupingExpr(Expr.Grouping expr) {
            return parenthesize("group", expr.expression);
        }

        @Override public String visitLiteralExpr(Expr.Literal expr) {
            if (expr.value == null) return "nil";
            return expr.value.toString();
        }

        @Override public String visitUnaryExpr(Expr.Unary expr) {
            return parenthesize(expr.operator.lexeme(), expr.right);
        }

        private String parenthesize(String name, Expr... exprs) {
            var builder = new StringBuilder();
            builder.append("(").append(name);

            for (Expr expr : exprs) {
                builder.append(" ")
                        .append(expr.accept(this));
            }
            builder.append(")");
            return builder.toString();
        }
    }
}
