# errorprone-futures

This is a collection of custom errorprone checks for Concurrency and Threads.

## Install

TODO: release

### Maven

TBD

### Gradle

```ruby
dependencies {
  annotationProcessor 'io.github.wreulicke:errorprone-concurrency:0.0.1'
  # or you can write below when you use net.ltgt.errorprone plugin
  errorprone 'io.github.wreulicke:errorprone-concurrency:0.0.1'
}
```

## Rules

- [DoNotDependDefaultExecutor](#donotdependdefaultexecutor)
- [ShouldRestrictThreadPoolSize](#shouldrestrictthreadpoolsize)
- [ShouldUseThreadFactory](#shouldusethreadfactory)

### DoNotDependDefaultExecutor

DoNotDependDefaultExecutor rule prevents using default executor.
Default executor is shared executor and it is not recommended to use IO-bound tasks.
Tasks can get stuck.

```java
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Executor;

class Test {
  void test() {
    Executor e = Executors.newFixedThreadPool(20);
    // valid
    CompletableFuture.runAsync(() -> {}, e);
    // valid
    CompletableFuture.supplyAsync(() -> 1, e);

    // invalid: next line use default executor implicitly
    CompletableFuture.runAsync(() -> {});
    // invalid: next line use default executor implicitly
    CompletableFuture.supplyAsync(() -> 1);
  }
}
```

### ShouldRestrictThreadPoolSize

ShouldRestrictThreadPoolSize rule prevents using unbounded thread pool.
Unbounded thread pool can cause OOM. It is recommended to use bounded thread pool.

```java
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

class Test {
  void test() {
    // valid
    Executor e = Executors.newFixedThreadPool(20);
    
    // invalid: next line use unbounded thread pool
    Executor e = Executors.newCachedThreadPool();
  }
}
```

### ShouldUseThreadFactory

ShouldUseThreadFactory rule prevents using Thread without thread name and uncaught exception handler.

```java
import java.util.concurrent.ThreadFactory;

class Test {
  void test() {
    // valid
    new ThreadFactory() {

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

    // invalid: should use ThreadFactory instead of new Thread() directly
    new Thread();

    // invalid
    new ThreadFactory() {

      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        // invalid: next line use constant name for thread name
        t.setName("constant");
        t.setUncaughtExceptionHandler((t1, e) -> {
          // should write log
        });
        return t;
      }
    };

    // invalid
    new ThreadFactory() {

      private final AtomicInteger counter = new AtomicInteger();

      // invalid: should set uncaught exception handler
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(String.format("test-%d", counter.incrementAndGet()));
        return t;
      }
    };

    // invalid
    new ThreadFactory() {

      private final AtomicInteger counter = new AtomicInteger();

      // invalid: should set thread name
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setUncaughtExceptionHandler((t1, e) -> {
          // should write log
        });
        return t;
      }
    };
  }
}
```

## License

MIT License