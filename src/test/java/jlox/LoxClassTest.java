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
class LoxClassTest {

    @StdOut ByteArrayOutputStream stdOut;
    @StdErr ByteArrayOutputStream stdErr;

    @Test void classDeclaration() {
        var source = """
                class DevonshireCream {
                  serveOn() {
                    return "Scones";
                  }
                }
                  
                print DevonshireCream;
                  """;

        Lox.run(source);

        assertThat(stdOut.toString()).as(stdErr.toString()).isEqualTo("""
                DevonshireCream
                """);
    }

    @Test void createsClassInstance() {
        var source = """
                class Bagel {}
                var bagel = Bagel();
                print bagel;
                  """;

        Lox.run(source);

        assertThat(stdOut.toString()).as(stdErr.toString()).isEqualTo("""
                Bagel instance
                """);
    }

    @Test void fields() {
        var source = """
                class Bagel {}
                var bagel = Bagel();
                bagel.field = "value";
                print bagel.field;
                  """;

        Lox.run(source);

        assertThat(stdOut.toString()).as(stdErr.toString()).isEqualTo("""
                value
                """);
    }

    @Test void methodCall() {
        var source = """
                class Bacon {
                  eat() {
                    print "Crunch crunch crunch!";
                  }
                }
                Bacon().eat();
                  """;

        Lox.run(source);

        assertThat(stdOut.toString()).as(stdErr.toString()).isEqualTo("""
                Crunch crunch crunch!
                """);
    }

    @Test void classFields() {
        var source = """
                class Cake {
                  taste() {
                    var adjective = "delicious";
                    print "The " + this.flavor + " cake is " + adjective + "!";
                  }
                }
                              
                var cake = Cake();
                cake.flavor = "German chocolate";
                cake.taste();
                  """;

        Lox.run(source);

        assertThat(stdOut.toString()).as(stdErr.toString()).isEqualTo("""
                The German chocolate cake is delicious!
                """);
    }

    @Test void constructor() {
        var source = """
                class Foo {
                  init() {
                    print this;
                  }
                }
                             
                var foo = Foo();
                print foo.init();
                  """;

        Lox.run(source);

        assertThat(stdOut.toString()).as(stdErr.toString()).isEqualTo("""
                Foo instance
                Foo instance
                Foo instance
                """);
    }
}
