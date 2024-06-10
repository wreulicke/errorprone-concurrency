package io.github.wreulicke.errorprone.futures;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

class ShouldRestrictThreadPoolSizeTest {

  @Test
  void testValid() {
    CompilationTestHelper compilationHelper =
        CompilationTestHelper.newInstance(ShouldRestrictThreadPoolSize.class, getClass());
    compilationHelper
        .addSourceLines(
            "Test.java",
            """
                import java.util.concurrent.Executors;
                class Test {
                    void test() {
                        Executors.newFixedThreadPool(20);
                    }
                }
                """)
        .doTest();
  }

  @Test
  void testInvalid() {
    CompilationTestHelper compilationHelper =
        CompilationTestHelper.newInstance(ShouldRestrictThreadPoolSize.class, getClass());
    compilationHelper
        .addSourceLines(
            "Test.java",
            """
                import java.util.concurrent.Executors;
                class Test {
                    void test() {
                        // BUG: Diagnostic contains: Do not use newCachedThreadPool. It is unbounded and can cause OOM. Use newFixedThreadPool instead.
                        Executors.newCachedThreadPool();
                    }
                }
                """)
        .doTest();
  }
}
