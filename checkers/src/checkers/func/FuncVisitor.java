package checkers.func;

import checkers.basetype.BaseTypeVisitor;
import checkers.func.quals.Function;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
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
        return super.visitClass(node, p);
    }

    @Override
    public Void visitMethod(MethodTree node, Void p) {
        currentMethod = CanonicalName.forMethod(node);
        facts.addMethod(currentMethod, currentClass);
        AnnotatedExecutableType annotMethod = atypeFactory.getAnnotatedType(node);
        if (annotMethod.hasAnnotation(Function.class)) {
            facts.addPureDeclaration(currentMethod);
        }
        for (AnnotationMirror m : annotMethod.getAnnotations()) {
            System.err.println("Annot: " + m.toString() + " for " + currentMethod);
        }
        return super.visitMethod(node, p);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        ExecutableElement calledMethodElement = atypeFactory.methodFromUse(node).getElement();
        String calledMethodCanonical = CanonicalName.forMethod(calledMethodElement);

        if (calledMethodElement.getAnnotation(Function.class) != null) {
            facts.addPureDeclaration(calledMethodCanonical);
        }
        String key = checker.getNodeMapping().add((Tree) node);
        facts.addMethodCall(key, currentMethod, calledMethodCanonical);
        
        return super.visitMethodInvocation(node, p);
    }

}
