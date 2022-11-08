package jlox;

import extension.StdErr;
import extension.StdExtension;
import extension.StdOut;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

// Works if run one by one, I guess it's some multithreading problem with my extension.
@ExtendWith(StdExtension.class)
class LoxResolutionTest {

    @StdOut ByteArrayOutputStream stdOut;
    @StdErr ByteArrayOutputStream stdErr;

    @Test void variableAlreadyDeclared() {
        var source = """
                fun bad() {
                   var myVar = "first";
                   var myVar = "second";
                 }
                  """;

        Lox.run(source);

        assertThat(stdErr.toString()).as(stdOut.toString()).isEqualTo("""
                [line 3] Error'myVar':Already a variable with this name in this scope.
                """);
    }

    @Test void returnInWrongScope() {
        var source = """
                return "at top level";
                  """;

        Lox.run(source);

        assertThat(stdErr.toString()).as(stdOut.toString()).isEqualTo("""
                [line 1] Error'return':Can't return from top level code.
                """);
    }
}
