package io.github.wreulicke.errorprone.futures;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;

@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Do not use newCachedThreadPool. It is unbounded and can cause OOM. Use newFixedThreadPool instead.",
    severity = BugPattern.SeverityLevel.ERROR,
    link = "github.com/wreulicke/errorprone-concurrency",
    linkType = BugPattern.LinkType.CUSTOM)
public class ShouldRestrictThreadPoolSize extends BugChecker
    implements BugChecker.MethodInvocationTreeMatcher {

  private static final Matcher<ExpressionTree> MATCHER =
      Matchers.anyOf(
          Matchers.staticMethod()
              .onClass("java.util.concurrent.Executors")
              .named("newCachedThreadPool"));

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (MATCHER.matches(tree, state)) {
      return describeMatch(tree);
    }
    return Description.NO_MATCH;
  }
}
