package jlox;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LoxTest {

    @Test void run_printsExpressions() {
        Map.of(
                expr("1+1"), "2",
                expr("2+2*2"), "6",
                expr("-1"), "-1",
                expr("1+12"), "13",
                expr("1==1"), "true",
                expr("1==2"), "false",
                expr("\"abc\"+\"cba\""), "abccba"
        ).forEach(
                (actual, expected) -> assertThat(actual).isEqualTo(expected));
    }

    private static String expr(String source) {
        return Lox.run(source);
    }
}