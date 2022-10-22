package jlox;

import org.junit.jupiter.api.Test;

import java.util.List;

import static jlox.TokenType.EOF;
import static jlox.TokenType.IDENTIFIER;
import static jlox.TokenType.NUMBER;
import static jlox.TokenType.OR;
import static jlox.TokenType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

class ScannerTest {

    @Test void scanTokens_noSource_returnsOnlyEOF() {
        String source = "";
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();

        assertThat(tokens).containsExactly(token(EOF));
    }

    @Test void scanTokens_parsesString() {
        String source = "\"aaa\"";
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();

        assertThat(tokens).containsExactly(string("aaa"), token(EOF));
    }

    @Test void scanTokens_parsesNumber() {
        String source = "123.123";
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();

        assertThat(tokens).containsExactly(number(123.123), token(EOF));
    }

    @Test void scanTokens_parsesKeyword() {
        String source = "or";
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();

        assertThat(tokens).containsExactly(keyword(OR), token(EOF));
    }

    @Test void scanTokens_parsesIdentifier() {
        String source = "orc";
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();

        assertThat(tokens).containsExactly(identifier("orc"), token(EOF));
    }

    private static Token keyword(TokenType type) {
        return new Token(type, type.toString().toLowerCase(), null, 1);
    }

    private static Token string(String text) {
        return new Token(STRING, '"' + text + '"', text, 1);
    }

    private static Token identifier(String name) {
        return new Token(IDENTIFIER, name, null, 1);
    }

    private static Token number(double val) {
        return new Token(NUMBER, Double.toString(val), val, 1);
    }

    private static Token token(TokenType type) {
        return token(type, null);
    }

    private static Token token(TokenType type, Object value) {
        return new Token(type, "", value, 1);
    }
}