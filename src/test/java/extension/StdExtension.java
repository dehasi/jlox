package extension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.function.Consumer;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotatedFields;

public class StdExtension implements TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback {

    private final static Namespace NAMESPACE = create("me.dehasi");
    private final static String TEST_INSTANCE = "testInstance";

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        context.getStore(NAMESPACE).put(TEST_INSTANCE, testInstance);
    }

    @Override
    public void beforeEach(final ExtensionContext context) {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        Object testInstance = context.getRequiredTestInstance();

        findAnnotatedFields(testInstance.getClass(), StdOut.class).forEach(setValue(testInstance, outContent));
        findAnnotatedFields(testInstance.getClass(), StdErr.class).forEach(setValue(testInstance, errContent));
    }

    private static Consumer<Field> setValue(Object obj, Object value) {
        return field -> {
            if (!field.getType().isAssignableFrom(value.getClass()))
                throw new RuntimeException("Field " + field.getName() + " should have " + value.getClass().getName() + " type.");
            field.setAccessible(true);
            try {
                field.set(obj, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(String.format("Can't set value=%sto field=%s", value, field), e);
            }
        };
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
}
