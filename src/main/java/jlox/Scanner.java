package jlox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Character.isDigit;
import static java.lang.Double.parseDouble;
import static java.util.Map.entry;
import static jlox.TokenType.AND;
import static jlox.TokenType.BANG;
import static jlox.TokenType.BANG_EQUAL;
import static jlox.TokenType.CLASS;
import static jlox.TokenType.COMMA;
import static jlox.TokenType.DOT;
import static jlox.TokenType.ELSE;
import static jlox.TokenType.EOF;
import static jlox.TokenType.EQUAL;
import static jlox.TokenType.EQUAL_EQUAL;
import static jlox.TokenType.FALSE;
import static jlox.TokenType.FOR;
import static jlox.TokenType.FUN;
import static jlox.TokenType.GREATER;
import static jlox.TokenType.GREATER_EQUAL;
import static jlox.TokenType.IDENTIFIER;
import static jlox.TokenType.IF;
import static jlox.TokenType.LEFT_BRACE;
import static jlox.TokenType.LEFT_PAREN;
import static jlox.TokenType.LESS;
import static jlox.TokenType.LESS_EQUAL;
import static jlox.TokenType.MINUS;
import static jlox.TokenType.NIL;
import static jlox.TokenType.NUMBER;
import static jlox.TokenType.OR;
import static jlox.TokenType.PLUS;
import static jlox.TokenType.PRINT;
import static jlox.TokenType.RETURN;
import static jlox.TokenType.RIGHT_BRACE;
import static jlox.TokenType.RIGHT_PAREN;
import static jlox.TokenType.SEMICOLON;
import static jlox.TokenType.SLASH;
import static jlox.TokenType.STAR;
import static jlox.TokenType.STRING;
import static jlox.TokenType.SUPER;
import static jlox.TokenType.THIS;
import static jlox.TokenType.TRUE;
import static jlox.TokenType.VAR;
import static jlox.TokenType.WHILE;

class Scanner {

    private static final Map<String, TokenType> keywords = Map.ofEntries(
            entry("and", AND),
            entry("class", CLASS),
            entry("else", ELSE),
            entry("false", FALSE),
            entry("fun", FUN),
            entry("for", FOR),
            entry("if", IF),
            entry("nil", NIL),
            entry("or", OR),
            entry("print", PRINT),
            entry("return", RETURN),
            entry("super", SUPER),
            entry("this", THIS),
            entry("true", TRUE),
            entry("var", VAR),
            entry("while", WHILE));

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0, current = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/' -> { // A comment goes until EOL
                if (match('/')) while (peek() != '\n' && !isAtEnd()) advance();
                else addToken(SLASH);
            }
            case ' ', '\t', '\r' -> {} // ignore whitespace
            case '\n' -> ++line;
            case '"' -> string();
            default -> {
                if (isDigit(c)) digit();
                else if (isAlpha(c)) identifier();
                else Lox.error(line, "Unexpected character '" + c + "'");
            }
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') ++line;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void digit() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }
        addToken(NUMBER, parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        addToken(keywords.getOrDefault(text, IDENTIFIER));
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        ++current;
        return true;
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private static boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
