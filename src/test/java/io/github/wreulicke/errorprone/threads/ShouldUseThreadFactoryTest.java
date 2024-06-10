package io.github.wreulicke.errorprone.threads;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

class ShouldUseThreadFactoryTest {

  @Test
  void testValid() {
    CompilationTestHelper compilationHelper =
        CompilationTestHelper.newInstance(ShouldUseThreadFactory.class, getClass());
    compilationHelper
        .addSourceLines(
            "Test.java",
            """
            import java.util.concurrent.ThreadFactory;
            import java.util.concurrent.atomic.AtomicInteger;
            class Test {
                void test() {
                    new ThreadFactory() {

                        private final AtomicInteger counter = new AtomicInteger();

                        public Thread newThread(Runnable r) {
                            Thread t = new Thread(r);
                            t.setName(String.format("test-%d", counter.incrementAndGet()));
                            t.setUncaughtExceptionHandler((t1, e) -> {});
                            return t;
                        }
                    };
                }
            }
            """)
        .doTest();
  }

  @Test
  void testInvalid() {
    CompilationTestHelper compilationHelper =
        CompilationTestHelper.newInstance(ShouldUseThreadFactory.class, getClass());
    compilationHelper
        .addSourceLines(
            "Test.java",
            """
            class Test {
                void test() {
                    // BUG: Diagnostic contains: Do not use new Thread() directly. Use ThreadFactory instead, setting the thread name and uncaught exception handler.
                    new Thread();
                }
            }
            """)
        .doTest();
  }

  @Test
  void testWithThreadFactory_SetNameOnly() {
    CompilationTestHelper compilationHelper =
        CompilationTestHelper.newInstance(ShouldUseThreadFactory.class, getClass());
    compilationHelper
        .addSourceLines(
            "Test.java",
            """
            import java.util.concurrent.ThreadFactory;
            import java.util.concurrent.atomic.AtomicInteger;
            class Test {
                void test() {
                    new ThreadFactory() {

                        private final AtomicInteger counter = new AtomicInteger();

                        @Override // BUG: Diagnostic contains: Should set thread name and uncaught exception handler.
                        public Thread newThread(Runnable r) {
                            Thread t = new Thread(r);
                            t.setName(String.format("test-%d", counter.incrementAndGet()));
                            return t;
                        }
                    };
                }
            }
            """)
        .doTest();
  }

  @Test
  void testWithThreadFactory_SetNameWithConstant() {
    CompilationTestHelper compilationHelper =
        CompilationTestHelper.newInstance(ShouldUseThreadFactory.class, getClass());
    compilationHelper
        .addSourceLines(
            "Test.java",
            """
            import java.util.concurrent.ThreadFactory;
            class Test {
                void test() {
                    new ThreadFactory() {

                        @Override
                        public Thread newThread(Runnable r) {
                            Thread t = new Thread(r);
                            // BUG: Diagnostic contains: Should generate thread name dynamically.
                            t.setName("invalid-constant");
                            return t;
                        }
                    };
                }
            }
            """)
        .doTest();
  }

  @Test
  void testWithThreadFactory_SetUncaughtExceptionHandler() {
    CompilationTestHelper compilationHelper =
        CompilationTestHelper.newInstance(ShouldUseThreadFactory.class, getClass());
    compilationHelper
        .addSourceLines(
            "Test.java",
            """
            import java.util.concurrent.ThreadFactory;
            class Test {
                void test() {
                    new ThreadFactory() {

                        @Override // BUG: Diagnostic contains: Should set thread name and uncaught exception handler.
                        public Thread newThread(Runnable r) {
                            Thread t = new Thread(r);
                            t.setUncaughtExceptionHandler((t1, e) -> {});
                            return t;
                        }
                    };
                }
            }
            """)
        .doTest();
  }

  @Test
  void testField() {
    CompilationTestHelper compilationHelper =
        CompilationTestHelper.newInstance(ShouldUseThreadFactory.class, getClass());
    compilationHelper
        .addSourceLines(
            "Test.java",
            """
            import java.util.concurrent.ThreadFactory;
            class Test {
                // BUG: Diagnostic contains: Do not use new Thread() directly. Use ThreadFactory instead, setting the thread name and uncaught exception handler.
                private final Thread thread = new Thread();
            }
            """)
        .doTest();
  }
}
