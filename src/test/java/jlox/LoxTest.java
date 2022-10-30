package jlox;

import extension.StdErr;
import extension.StdExtension;
import extension.StdOut;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(StdExtension.class)
class LoxTest {

    @StdOut ByteArrayOutputStream stdOut;
    @StdErr ByteArrayOutputStream stdErr;

    @Test void run_printsExpressions() {
        var source = """
                print 2+2*2;
                print 1==1;
                print 1==2;
                print "abc"+"cba";
                        """;

        Lox.run(source);

        assertThat("""
                6
                true
                false
                abccba
                """).isEqualTo(stdOut.toString());
    }

    @Test void run_printsVariables() {
        var source = """
                var a = 1;
                var b = 2;
                print a + b;

                var beverage = "espresso";
                print beverage;
                """;

        Lox.run(source);

        assertThat(stdOut.toString()).as(stdErr.toString()).isEqualTo("""
                3
                espresso
                """);
    }

    @Test void run_printsReassignedVariables() {
        var source = """
                var a = 1;
                print a = 2;
                """;

        Lox.run(source);

        assertThat(stdOut.toString()).as(stdErr.toString()).isEqualTo("""
                2
                """);
    }

    @Test void run_printsDifferentScopes() {
        var source = """
                var a = "global a";
                var b = "global b";
                var c = "global c";
                {
                    var a = "outer a";
                    var b = "outer b";
                    {
                        var a = "inner a";
                        print a;
                        print b;
                        print c;
                    }
                    print a;
                    print b;
                    print c;
                }
                print a;
                print b;
                print c;
                """;

        Lox.run(source);

        assertThat(stdOut.toString()).as(stdErr.toString()).isEqualTo("""
                inner a
                outer b
                global c
                outer a
                outer b
                global c
                global a
                global b
                global c
                """);
    }

    @Test void run_interpretsIf() {
        var source = """
                var a = 1;
                var b = 2;
                if(a > b) print a;
                else print b;
                 """;

        Lox.run(source);

        assertThat(stdOut.toString()).as(stdErr.toString()).isEqualToIgnoringNewLines("2");
    }

    @Test void run_doesShortCircuit() {
        var source = """
                print "hi" or 2;
                print nil or "yes";
                print nil and "yes";
                print "yes" and nil;
                 """;

        Lox.run(source);

        assertThat(stdOut.toString()).as(stdErr.toString()).isEqualToIgnoringNewLines("""
                hi
                yes
                nil
                nil
                """);
    }

    @Test void run_executesWhileLoop() {
        var source = """
                var a = 3;
                while (a > 0)
                    print a = a-1;
                 """;

        Lox.run(source);

        assertThat(stdOut.toString()).as(stdErr.toString()).isEqualToIgnoringNewLines("""
                2
                1
                0
                """);
    }

    @Test void run_executesForLoop() {
        var source = """
                var n = 3;
                for (var i = 0; i < n; i = i + 1) {
                   print i;
                }
                  """;

        Lox.run(source);

        assertThat(stdOut.toString()).as(stdErr.toString()).isEqualToIgnoringNewLines("""
                0
                1
                2
                """);
    }
}
