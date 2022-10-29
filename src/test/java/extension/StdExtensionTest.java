package extension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(StdExtension.class)
class StdExtensionTest {

    @StdOut ByteArrayOutputStream stdOut;
    @StdErr ByteArrayOutputStream stdErr;

    private final TestRun run = new TestRun();

    @Test void capturesStds() {
        run.stdout("Hello stdout");
        run.stderr("Hello stderr");

        assertThat(stdOut.toString()).isEqualToIgnoringNewLines("Hello stdout");
        assertThat(stdErr.toString()).isEqualToIgnoringNewLines("Hello stderr");
    }

    @Test void refreshesAfterEachTest() {
        run.stdout("Goodbye stdout");
        run.stderr("Goodbye stderr");

        assertThat(stdOut.toString()).isEqualToIgnoringNewLines("Goodbye stdout");
        assertThat(stdErr.toString()).isEqualToIgnoringNewLines("Goodbye stderr");
    }

    static class TestRun {

        void stdout(String stdout) {
            System.out.println(stdout);
        }

        void stderr(String stderr) {
            System.err.println(stderr);
        }
    }
}
