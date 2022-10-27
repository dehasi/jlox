package jlox;

import java.util.Objects;

class Interpreter implements Expr.Visitor<Object> {

    String interpret(Expr expression) {
        try {
            String string = stringify(expression.accept(this));
            System.out.println(string);
            return string;
        } catch (RuntimeError error) {
            return Lox.runtimeError(error);
        }
    }

    @Override public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        return switch (expr.operator.type()) {
            case MINUS -> requireNumber(expr.operator, left) - requireNumber(expr.operator, right);
            case SLASH -> requireNumber(expr.operator, left) / requireNumber(expr.operator, right);
            case STAR -> requireNumber(expr.operator, left) * requireNumber(expr.operator, right);
            case PLUS -> {
                if (left instanceof Double l && right instanceof Double r)
                    yield l + r;
                if (left instanceof String l && right instanceof String r)
                    yield l + r;
                throw new RuntimeError(expr.operator, "Operands must be two strings or two numbers.");
            }

            case GREATER -> requireNumber(expr.operator, left) > requireNumber(expr.operator, right);
            case GREATER_EQUAL -> requireNumber(expr.operator, left) >= requireNumber(expr.operator, right);
            case LESS -> requireNumber(expr.operator, left) < requireNumber(expr.operator, right);
            case LESS_EQUAL -> requireNumber(expr.operator, left) <= requireNumber(expr.operator, right);

            case EQUAL_EQUAL -> Objects.equals(left, right);
            case BANG_EQUAL -> !Objects.equals(left, right);
            default -> throw new InterpreterException("Unexpected token type" + expr.operator.type().name());
        };
    }

    @Override public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        return switch (expr.operator.type()) {
            case BANG -> !isTruthy(right);
            case MINUS -> -requireNumber(expr.operator, right);
            default -> throw new InterpreterException("Unexpected token type" + expr.operator.type().name());
        };
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean bool) return bool;
        return true;
    }

    private Object evaluate(Expr expression) {
        return expression.accept(this);
    }

    private static String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double d) {
            var text = object.toString();
            if (text.endsWith(".0"))
                text = text.substring(0, text.length() - 2);
            return text;
        }
        return object.toString();
    }

    private static Double requireNumber(Token operator, Object operand) {
        if (operand instanceof Double d) return d;
        throw new RuntimeError(operator, "Operand must me a number.");
    }

    private static class InterpreterException extends RuntimeException {
        public InterpreterException(String message) {
            super(message);
        }
    }
}
