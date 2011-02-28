package checkers.func;

import checkers.basetype.BaseTypeVisitor;
import checkers.func.quals.Anonymous;
import checkers.func.quals.Function;
import checkers.func.quals.Immutable;
import checkers.func.quals.ReadOnly;
import checkers.func.quals.WriteLocal;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.util.TreeUtils;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.tree.TreeNode;

/**
 *
 * @author saf
 */
public class FuncVisitor extends BaseTypeVisitor<Void, Void> {

    protected FactPrinter facts;
    protected FuncChecker checker;

    protected String currentMethod;
    protected String currentClass;

    public FuncVisitor(FuncChecker checker, CompilationUnitTree root) throws IOException {
        super(checker, root);
        this.checker = checker;
        facts = new FactPrinter();
    }

    @Override
    public Void visitClass(ClassTree node, Void p) {
        currentClass = CanonicalName.forClass(node);
        facts.addClass(currentClass);
        TypeElement el = TreeUtils.elementFromDeclaration(node);
        if (el.getAnnotation(Immutable.class) != null) {
            facts.addAnnotation("immutable", currentClass);
        }
        return super.visitClass(node, p);
    }

    @Override
    public Void visitMethod(MethodTree node, Void p) {
        currentMethod = CanonicalName.forMethod(node);
        ExecutableElement methodElement = TreeUtils.elementFromDeclaration(node);
        if (methodElement.getKind() == ElementKind.CONSTRUCTOR) {
            facts.addConstructor(currentMethod, currentClass);
            if (methodElement.getAnnotation(WriteLocal.class) != null) {
                facts.addAnnotation("writelocal", currentMethod);
            };
            if (methodElement.getAnnotation(Anonymous.class) != null) {
                facts.addAnnotation("anonymous", currentMethod);
            };
        } else {
            facts.addMethod(currentMethod, currentClass);
            if (methodElement.getAnnotation(Function.class) != null) {
                facts.addAnnotation("pure", currentMethod);
            };
            if (methodElement.getAnnotation(ReadOnly.class) != null) {
                facts.addAnnotation("readonly", currentMethod);
            }
        }
        return super.visitMethod(node, p);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        ExecutableElement calledMethodElement = atypeFactory.methodFromUse(node).getElement();
        String calledMethodCanonical = CanonicalName.forMethod(calledMethodElement);

        if (calledMethodElement.getAnnotation(Function.class) != null) {
            facts.addAnnotation("pure", calledMethodCanonical);
        }
        String key = checker.getNodeMapping().add((Tree) node);
        facts.addMethodCall(key, currentMethod, calledMethodCanonical);
        
        return super.visitMethodInvocation(node, p);
    }

}
