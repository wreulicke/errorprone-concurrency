package io.github.wreulicke.errorprone.threads;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;

@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Do not use new Thread() directly. Use ThreadFactory instead, setting the thread name and uncaught exception handler.",
    severity = BugPattern.SeverityLevel.ERROR,
    link = "github.com/wreulicke/errorprone-concurrency",
    linkType = BugPattern.LinkType.CUSTOM)
public class ShouldUseThreadFactory extends BugChecker implements BugChecker.NewClassTreeMatcher {

  private static final String THREAD_CLASS = "java.lang.Thread";
  private static final Matcher<ExpressionTree> MATCHER =
      Matchers.constructor().forClass(THREAD_CLASS);

  private static final Matcher<ClassTree> IS_NOT_SUBTYPE_OF_THREAD_FACTORY =
      Matchers.not(Matchers.isSubtypeOf("java.util.concurrent.ThreadFactory"));

  private static final Matcher<ExpressionTree> SET_NAME =
      Matchers.instanceMethod().onDescendantOf(THREAD_CLASS).named("setName");

  private static final Matcher<ExpressionTree> SET_UNCAUGHT_EXCEPTION_HANDLER =
      Matchers.instanceMethod().onDescendantOf(THREAD_CLASS).named("setUncaughtExceptionHandler");

  @Override
  public Description matchNewClass(NewClassTree tree, VisitorState state) {
    if (!MATCHER.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    if (IS_NOT_SUBTYPE_OF_THREAD_FACTORY.matches(getClassDeclaration(state), state)) {
      return describeMatch(tree);
    }
    MethodTree method = getMethodDeclaration(state);
    BlockTree body = method.getBody();
    if (body == null) {
      return Description.NO_MATCH;
    }

    boolean hasSetName = false;
    boolean hasSetUncaughtExceptionHandler = false;
    for (StatementTree statement : body.getStatements()) {
      if (statement.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
        ExpressionTree expression = ((ExpressionStatementTree) statement).getExpression();
        if (SET_NAME.matches(expression, state)) {
          if (ASTHelpers.constValue(((MethodInvocationTree) expression).getArguments().get(0))
              != null) {
            return buildDescription(expression)
                .setMessage("Should generate thread name dynamically.")
                .build();
          }
          hasSetName = true;
        }
        if (SET_UNCAUGHT_EXCEPTION_HANDLER.matches(expression, state)) {
          hasSetUncaughtExceptionHandler = true;
        }
      }
    }
    if (!hasSetName || !hasSetUncaughtExceptionHandler) {
      return buildDescription(method)
          .setMessage("Should set thread name and uncaught exception handler.")
          .build();
    }

    return Description.NO_MATCH;
  }

  private ClassTree getClassDeclaration(VisitorState state) {
    TreePath path =
        ASTHelpers.findPathFromEnclosingNodeToTopLevel(state.getPath(), ClassTree.class);
    return (ClassTree) path.getLeaf();
  }

  private MethodTree getMethodDeclaration(VisitorState state) {
    TreePath path =
        ASTHelpers.findPathFromEnclosingNodeToTopLevel(state.getPath(), MethodTree.class);
    return (MethodTree) path.getLeaf();
  }
}
