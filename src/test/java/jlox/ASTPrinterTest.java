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
}
