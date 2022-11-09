package jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static jlox.TokenType.OR;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override public int arity() {return 0;}

            @Override public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override public String toString() {return "<native fn>";}
        });
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override public Void visitClassStmt(Stmt.Class stmt) {
        environment.define(stmt.name.lexeme(), null);
        Map<String, LoxFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            LoxFunction function = new LoxFunction(method, environment, method.name.lexeme().equals("init"));
            methods.put(method.name.lexeme(), function);
        }

        LoxClass klass = new LoxClass(stmt.name.lexeme(), methods);
        environment.assign(stmt.name, klass);
        return null;
    }

    @Override public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override public Void visitIfStmt(Stmt.If stmt) {
        Object value = evaluate(stmt.condition);
        if (isTruthy(value))
            execute(stmt.thenBranch);
        else if (stmt.elseBranch != null)
            execute(stmt.elseBranch);

        return null;
    }

    @Override public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition)))
            execute(stmt.body);
        return null;
    }

    @Override public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null)
            value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null)
            value = evaluate(stmt.initializer);
        environment.define(stmt.name.lexeme(), value);
        return null;
    }

    @Override public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction loxFunction = new LoxFunction(stmt, environment, false);
        environment.define(stmt.name.lexeme(), loxFunction);
        return null;
    }

    @Override public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null)
            environment.assignAt(distance, expr.name, value);
        else
            globals.assign(expr.name, value);

        return value;
    }

    @Override public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.type() == OR) {
            if (isTruthy(left)) return left; // true OR right = true
        } else {
            if (!isTruthy(left)) return left;// false OR right = false
        }
        return evaluate(expr.right);
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

    @Override public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof LoxInstance loxInstance))
            throw new RuntimeError(expr.name, "Only instances have fields.");

        Object value = evaluate(expr.value);
        loxInstance.set(expr.name, value);
        return null;
    }

    @Override public Object visitThisExpr(Expr.This expr) {
        return lookupVariable(expr.keyword, expr);
    }

    @Override public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        return switch (expr.operator.type()) {
            case BANG -> !isTruthy(right);
            case MINUS -> -requireNumber(expr.operator, right);
            default -> throw new InterpreterException("Unexpected token type" + expr.operator.type().name());
        };
    }

    @Override public Object visitVariableExpr(Expr.Variable expr) {
        return lookupVariable(expr.name, expr);
    }

    private Object lookupVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null)
            return environment.getAt(distance, name.lexeme());
        else
            return globals.get(name);
    }

    @Override public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        List<Object> arguments = expr.arguments.stream().map(this::evaluate).toList();

        if (!(callee instanceof LoxCallable function))
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");

        if (arguments.size() != function.arity())
            throw new RuntimeError(expr.paren,
                    "Expected " + function.arity() + " arguments, but got " + arguments.size() + ".");

        return function.call(this, arguments);
    }

    @Override public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof LoxInstance loxInstance)
            return loxInstance.get(expr.name);
        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean bool) return bool;
        return true;
    }

    private void execute(Stmt statement) {
        statement.accept(this);
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
