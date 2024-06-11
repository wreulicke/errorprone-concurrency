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
import com.sun.source.tree.NewClassTree;
import java.util.List;
import java.util.regex.Pattern;

@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Do not depend on the default thread factory. It is recommended to use a custom thread factory instead for tracing the thread creation.",
    severity = BugPattern.SeverityLevel.ERROR,
    link = "github.com/wreulicke/errorprone-concurrency",
    linkType = BugPattern.LinkType.CUSTOM)
public class DoNotDependDefaultThreadFactory extends BugChecker
    implements BugChecker.MethodInvocationTreeMatcher, BugChecker.NewClassTreeMatcher {

  private static final Matcher<ExpressionTree> IS_EXECUTORS_STATIC_METHODS =
      Matchers.staticMethod()
          .onClass("java.util.concurrent.Executors")
          .withNameMatching(
              Pattern.compile(
                  "newFixedThreadPool|newCachedThreadPool|newSingleThreadExecutor|newScheduledThreadPool"));

  private static final Matcher<ExpressionTree> USE_DEFAULT_THREAD_FACTORY =
      Matchers.staticMethod()
          .onClass("java.util.concurrent.Executors")
          .named("defaultThreadFactory");

  private static final Matcher<ExpressionTree> IS_THREAD_POOL_EXECUTOR =
      Matchers.anyOf(
          Matchers.constructor().forClass("java.util.concurrent.ThreadPoolExecutor"),
          Matchers.constructor().forClass("java.util.concurrent.ScheduledThreadPoolExecutor"));

  private static final Matcher<ExpressionTree> IS_THREAD_FACTORY =
      Matchers.isSubtypeOf("java.util.concurrent.ThreadFactory");

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (USE_DEFAULT_THREAD_FACTORY.matches(tree, state)) {
      return describeMatch(tree);
    }
    if (!IS_EXECUTORS_STATIC_METHODS.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    List<? extends ExpressionTree> arguments = tree.getArguments();
    if (arguments.stream().noneMatch(arg -> IS_THREAD_FACTORY.matches(arg, state))) {
      return describeMatch(tree);
    }

    return Description.NO_MATCH;
  }

  @Override
  public Description matchNewClass(NewClassTree tree, VisitorState state) {
    if (!IS_THREAD_POOL_EXECUTOR.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    List<? extends ExpressionTree> arguments = tree.getArguments();
    if (arguments.stream().noneMatch(arg -> IS_THREAD_FACTORY.matches(arg, state))) {
      return describeMatch(tree);
    }

    return Description.NO_MATCH;
  }
}
