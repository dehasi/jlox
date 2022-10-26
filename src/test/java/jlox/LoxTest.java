package jlox;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LoxTest {

    @Test void run_printsExpressions() {
        Map.of(
                expr("1+1"), "(+ 1.0 1.0)",
                expr("2+2*2"), "(+ 2.0 (* 2.0 2.0))",
                expr("-1"), "(- 1.0)",
                expr("1+12"), "(+ 1.0 12.0)").forEach(
                (actual, expected) -> assertThat(actual).isEqualTo(expected));
    }

    private static String expr(String source) {
        return new ASTPrinter().print(Lox.run(source));
    }
}