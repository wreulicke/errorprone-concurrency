package io.github.wreulicke.errorprone.sdkhttp;

import static org.junit.jupiter.api.Assertions.*;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

class ShouldSetExecutorJavaNetHttpClientTest {

  @Test
  void testValid() {
    CompilationTestHelper.newInstance(ShouldSetExecutorJavaNetHttpClient.class, getClass())
        .addSourceLines(
            "Test.java",
            """
            import java.net.http.HttpClient;
            import java.util.concurrent.Executors;
            class Test {
                void testValid() {
                    var e = Executors.newFixedThreadPool(20);
                    HttpClient.newBuilder().executor(e).build();
                }
            }
            """)
        .doTest();
  }

  @Test
  void testInvalid() {
    CompilationTestHelper.newInstance(ShouldSetExecutorJavaNetHttpClient.class, getClass())
        .addSourceLines(
            "Test.java",
            """
            import java.net.http.HttpClient;
            class Test {
                void test() {
                    // BUG: Diagnostic contains: Should set executor to java.net.http.HttpClient. It is recommended to use a dedicated executor instead of the default unbounded executor.
                    HttpClient.newHttpClient();

                    // BUG: Diagnostic contains: Should set executor to java.net.http.HttpClient. It is recommended to use a dedicated executor instead of the default unbounded executor.
                    HttpClient.newBuilder().build();
                }
            }
            """)
        .doTest();
  }
}
