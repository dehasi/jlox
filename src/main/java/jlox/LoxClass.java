package jlox;

import java.util.List;
import java.util.Map;

class LoxClass implements LoxCallable {
    final String name;
    final Map<String, LoxFunction> methods;

    LoxClass(String name, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    @Override public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        else return initializer.arity();
    }

    @Override public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null)
            initializer.bind(instance).call(interpreter, arguments);
        return instance;
    }

    @Override public String toString() {
        return name;
    }

    public LoxFunction findMethod(String name) {
        if (methods.containsKey(name))
            return methods.get(name);
        return null;
    }
}
