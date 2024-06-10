package io.github.wreulicke.errorprone.futures;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

class DoNotDependDefaultExecutorTest {

  @Test
  void testValid() {
    CompilationTestHelper.newInstance(DoNotDependDefaultExecutor.class, getClass())
        .addSourceLines(
            "Test.java",
            """
            import java.util.concurrent.CompletableFuture;
            import java.util.concurrent.Executors;
            import java.util.concurrent.Executor;
            class Test {
                void testValid() {
                    Executor e = Executors.newFixedThreadPool(20);
                    CompletableFuture.runAsync(() -> {}, e);

                    CompletableFuture.supplyAsync(() -> 1, e);

                    var f = CompletableFuture.completedFuture(1);
                    f.thenApplyAsync(i -> i, e);
                    f.thenAcceptAsync(i -> {}, e);
                    f.thenRunAsync(() -> {}, e);
                    f.thenCombineAsync(f, (a, b) -> a + b, e);
                    f.thenAcceptBothAsync(f, (a, b) -> {}, e);
                    f.runAfterBothAsync(f, () -> {}, e);
                    f.applyToEitherAsync(f, i -> i, e);
                    f.acceptEitherAsync(f, i -> {}, e);
                    f.runAfterEitherAsync(f, () -> {}, e);
                    f.thenComposeAsync(i -> CompletableFuture.completedFuture(i), e);
                    f.whenCompleteAsync((i, ex) -> {}, e);
                    f.handleAsync((i, ex) -> i, e);
                    f.exceptionallyAsync(ex -> 1, e);
                    f.exceptionallyComposeAsync(ex -> f, e);
                    f.completeAsync(() -> 1, e);
                }
            }
            """)
        .doTest();
  }

  @Test
  void testInvalid() {
    CompilationTestHelper.newInstance(DoNotDependDefaultExecutor.class, getClass())
        .addSourceLines(
            "Test.java",
            """
            import java.util.concurrent.ForkJoinPool;
            import java.util.concurrent.CompletableFuture;
            import java.util.concurrent.Executors;
            class Test {
                void test() {
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    CompletableFuture.runAsync(() -> {});

                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    CompletableFuture.supplyAsync(() -> 1);

                    var f = CompletableFuture.completedFuture(1);
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    f.thenApplyAsync(i -> i);
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    f.thenAcceptAsync(i -> {});
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    f.thenRunAsync(() -> {});
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    f.thenCombineAsync(f, (a, b) -> a + b);
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    f.thenAcceptBothAsync(f, (a, b) -> {});
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    f.runAfterBothAsync(f, () -> {});
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    f.applyToEitherAsync(f, i -> i);
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    f.acceptEitherAsync(f, i -> {});
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    f.runAfterEitherAsync(f, () -> {});
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    f.thenComposeAsync(i -> CompletableFuture.completedFuture(i));
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    f.whenCompleteAsync((i, e) -> {});
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    f.handleAsync((i, e) -> i);
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    f.exceptionallyAsync(e -> 1);
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    f.exceptionallyComposeAsync(e -> f);
                    // BUG: Diagnostic contains: Do not depend default executor for IO-bound tasks. Use dedicated executor instead.
                    f.completeAsync(() -> 1);
                }
            }
            """)
        .doTest();
  }
}
