package io.github.wreulicke.errorprone.sdkhttp;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;

@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Should set executor to java.net.http.HttpClient. It is recommended to use a dedicated executor instead of the default unbounded executor.",
    severity = BugPattern.SeverityLevel.ERROR,
    link = "github.com/wreulicke/errorprone-concurrency",
    linkType = BugPattern.LinkType.CUSTOM)
public class ShouldSetExecutorJavaNetHttpClient extends BugChecker
    implements BugChecker.MethodInvocationTreeMatcher {

  private static final String HTTP_CLIENT_CLASS = "java.net.http.HttpClient";

  private static final Matcher<ExpressionTree> IS_SIMPLE_FACTORY =
      Matchers.staticMethod().onClass(HTTP_CLIENT_CLASS).named("newHttpClient");

  private static final Matcher<ExpressionTree> IS_BUILDING_CLINET =
      Matchers.instanceMethod().onDescendantOf("java.net.http.HttpClient.Builder").named("build");

  private static final Matcher<ExpressionTree> SET_EXECUTOR =
      Matchers.instanceMethod()
          .onDescendantOf("java.net.http.HttpClient.Builder")
          .named("executor");

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (IS_SIMPLE_FACTORY.matches(tree, state)) {
      return describeMatch(tree);
    }
    if (!IS_BUILDING_CLINET.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    ExpressionTree t = tree;

    // find executor method invocation in method chaining
    while (t instanceof MethodInvocationTree) {
      t = ASTHelpers.getReceiver(t);
      if (t instanceof MethodInvocationTree m) {
        if (SET_EXECUTOR.matches(m, state)) {
          return Description.NO_MATCH;
        }
      }
    }

    return describeMatch(tree);
  }
}
