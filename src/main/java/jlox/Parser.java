package jlox;

import java.util.ArrayList;
import java.util.List;

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

class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd())
            statements.add(declaration());
        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(CLASS)) return classDeclaration();
            if (match(FUN)) return function("function");
            if (match(VAR)) return varDeclaration();
            else return statement();
        } catch (ParseError e) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");
        Expr.Variable superclass = null;
        if (match(LESS)) {
            consume(IDENTIFIER, "Expect superclass name.");
            superclass = new Expr.Variable(previous());
        }
        consume(LEFT_BRACE, "Expect '{' before class body.");
        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd())
            methods.add(function("method"));

        consume(RIGHT_BRACE, "Expect '}' after class body.");
        return new Stmt.Class(name, superclass, methods);
    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");

        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() > 255)
                    error(peek(), "Cant' have more than 255 parameters.");
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");

        consume(LEFT_BRACE, "Expect '{' before " + kind + "body.");
        List<Stmt> body = block();

        return new Stmt.Function(name, parameters, body);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expect '}' after a block.");
        return statements;
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (match(EQUAL))
            initializer = expression();

        consume(SEMICOLON, "Expect ';' after variable declaration");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifDeclaration();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");
        Stmt initializer = null;
        if (match(SEMICOLON))
            initializer = null;
        else if (match(VAR))
            initializer = varDeclaration();
        else
            initializer = expressionStatement();

        Expr condition;
        if (match(SEMICOLON)) condition = new Expr.Literal(true);
        else condition = expression();
        consume(SEMICOLON, "Expect ';' after loop condition");

        Expr increment;
        if (match(RIGHT_PAREN)) increment = null;
        else increment = expression();
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        Stmt body = statement();

        if (increment != null)
            body = new Stmt.Block(List.of(body, new Stmt.Expression(increment)));

        body = new Stmt.While(condition, body);

        if (initializer != null)
            body = new Stmt.Block(List.of(initializer, body));
        return body;
    }

    private Stmt ifDeclaration() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        var condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after 'if' condition.");

        var thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE))
            elseBranch = statement();

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON))
            value = expression();
        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after 'while' condition.");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt expressionStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(value);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable variable) {
                Token name = variable.name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get get) {
                return new Expr.Set(get.object, get.name, value);
            }
            error(equals, "Invalid assignment target."); // no throw
        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(GREATER, GREATER_EQUAL, LESS_EQUAL, LESS)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return call();
    }

    private Expr call() {
        Expr expr = primary();
        while (true) {
            if (match(LEFT_PAREN))
                expr = finishCall(expr);
            else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else break;
        }
        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() > 255)
                    error(peek(), "Cant' have more than 255 arguments.");
                arguments.add(expression());
            } while (match(COMMA));
        }
        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING))
            return new Expr.Literal(previous().literal());

        if (match(SUPER)) {
            Token keyword = previous();
            consume(DOT, "Expect '.' after 'super;");
            Token method = consume(IDENTIFIER, "Expect superclass method name.");
            return new Expr.Super(keyword, method);
        }

        if (match(THIS))
            return new Expr.This(previous());

        if (match(IDENTIFIER))
            return new Expr.Variable(previous());

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type() == SEMICOLON) return;
            switch (peek().type()) {
                case CLASS:
                case FUN:
                case VAR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return; // todo try rewrite with match(...)
            }
            advance();
        }
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    private Token consume(TokenType type, String errorMessage) {
        if (check(type)) return advance();
        else throw error(peek(), errorMessage);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private Token advance() {
        if (!isAtEnd()) ++current;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private static class ParseError extends RuntimeException {}
}
