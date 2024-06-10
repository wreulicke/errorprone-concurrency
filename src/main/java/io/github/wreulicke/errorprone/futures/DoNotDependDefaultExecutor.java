package io.github.wreulicke.errorprone.futures;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.matchers.method.MethodMatchers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.concurrent.CompletableFuture;

@AutoService(BugChecker.class)
@BugPattern(
    summary = "Do not depend default executor for IO-bound tasks. Use dedicated executor instead.",
    severity = BugPattern.SeverityLevel.ERROR,
    link = "github.com/wreulicke/errorprone-futures",
    linkType = BugPattern.LinkType.CUSTOM)
public class DoNotDependDefaultExecutor extends BugChecker
    implements BugChecker.MethodInvocationTreeMatcher {

  private static final Matcher<ExpressionTree> COMPLETABLE_FUTURE_STATIC_METHODS =
      MethodMatchers.staticMethod()
          .onClass("java.util.concurrent.CompletableFuture")
          .namedAnyOf("runAsync", "supplyAsync");

  private static final Matcher<ExpressionTree> COMPLETABLE_FUTURE_INSTANCE_METHODS =
      MethodMatchers.instanceMethod()
          .onDescendantOf(CompletableFuture.class.getName())
          .namedAnyOf(
              "thenApplyAsync",
              "thenAcceptAsync",
              "thenRunAsync",
              "thenCombineAsync",
              "thenAcceptBothAsync",
              "runAfterBothAsync",
              "applyToEitherAsync",
              "acceptEitherAsync",
              "runAfterEitherAsync",
              "thenComposeAsync",
              "whenCompleteAsync",
              "handleAsync",
              "exceptionallyAsync",
              "exceptionallyComposeAsync",
              "completeAsync");

  private static final Matcher<ExpressionTree> IS_EXECUTOR =
      Matchers.allOf(Matchers.not(Matchers.nullLiteral()), Matchers.isSubtypeOf("java.util.concurrent.Executor"));

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (COMPLETABLE_FUTURE_STATIC_METHODS.matches(tree, state)) {
      if (tree.getArguments().stream().noneMatch(t -> IS_EXECUTOR.matches(t, state))) {
        return describeMatch(tree);
      }
    }

    if (COMPLETABLE_FUTURE_INSTANCE_METHODS.matches(tree, state)) {
      if (tree.getArguments().stream().noneMatch(t -> IS_EXECUTOR.matches(t, state))) {
        return describeMatch(tree);
      }
    }

    return Description.NO_MATCH;
  }
}
