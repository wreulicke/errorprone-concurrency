package io.github.wreulicke.errorprone.futures;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

class DoNotDependDefaultThreadFactoryTest {

  @Test
  void testValid() {
    CompilationTestHelper compilationHelper =
        CompilationTestHelper.newInstance(DoNotDependDefaultThreadFactory.class, getClass());
    compilationHelper
        .addSourceLines(
            "Test.java",
            """
        import java.util.concurrent.ThreadFactory;
        import java.util.concurrent.atomic.AtomicInteger;
        import java.util.concurrent.Executors;
        import java.util.concurrent.ThreadPoolExecutor;
        import java.util.concurrent.TimeUnit;
        import java.util.concurrent.LinkedBlockingQueue;
        import java.util.concurrent.ScheduledThreadPoolExecutor;

        class Test {
            void test() {
                ThreadFactory f = new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger();
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName(String.format("test-%d", counter.incrementAndGet()));
                        t.setUncaughtExceptionHandler((t1, e) -> {
                          // should write log
                        });
                        return t;
                    }
                };

                Executors.newFixedThreadPool(10, f);

                Executors.newCachedThreadPool(f);

                Executors.newSingleThreadExecutor(f);

                Executors.newScheduledThreadPool(10, f);

                Executors.newWorkStealingPool();

                new ThreadPoolExecutor(10, 10, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), f);

                new ScheduledThreadPoolExecutor(10, f);
            }
        }
        """)
        .doTest();
  }

  @Test
  void testInvalid() {
    CompilationTestHelper compilationHelper =
        CompilationTestHelper.newInstance(DoNotDependDefaultThreadFactory.class, getClass());
    compilationHelper
        .addSourceLines(
            "Test.java",
            """
        import java.util.concurrent.Executors;
        import java.util.concurrent.ThreadFactory;
        import java.util.concurrent.ThreadPoolExecutor;
        import java.util.concurrent.TimeUnit;
        import java.util.concurrent.LinkedBlockingQueue;
        import java.util.concurrent.ScheduledThreadPoolExecutor;

        class Test {
            void test() {
                // BUG: Diagnostic contains: Do not depend on the default thread factory. It is recommended to use a custom thread factory instead for tracing the thread creation.
                ThreadFactory factory = Executors.defaultThreadFactory();

                // BUG: Diagnostic contains: Do not depend on the default thread factory. It is recommended to use a custom thread factory instead for tracing the thread creation.
                Executors.newFixedThreadPool(10);

                // BUG: Diagnostic contains: Do not depend on the default thread factory. It is recommended to use a custom thread factory instead for tracing the thread creation.
                Executors.newCachedThreadPool();

                // BUG: Diagnostic contains: Do not depend on the default thread factory. It is recommended to use a custom thread factory instead for tracing the thread creation.
                Executors.newSingleThreadExecutor();

                // BUG: Diagnostic contains: Do not depend on the default thread factory. It is recommended to use a custom thread factory instead for tracing the thread creation.
                Executors.newScheduledThreadPool(10);

                // BUG: Diagnostic contains: Do not depend on the default thread factory. It is recommended to use a custom thread factory instead for tracing the thread creation.
                new ThreadPoolExecutor(10, 10, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

                // BUG: Diagnostic contains: Do not depend on the default thread factory. It is recommended to use a custom thread factory instead for tracing the thread creation.
                new ScheduledThreadPoolExecutor(10);
            }
        }
        """)
        .doTest();
  }
}
