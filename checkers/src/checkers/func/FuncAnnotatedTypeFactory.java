package checkers.func;

import checkers.func.quals.Anonymous;
import checkers.func.quals.Immutable;
import checkers.func.quals.ReadOnly;
import checkers.func.quals.WriteLocal;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.BasicAnnotatedTypeFactory;
import checkers.util.TreeUtils;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Determines annotations based on rules governing Funcheck's annotations.
 */
public class FuncAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<FuncChecker> {
    public FuncAnnotatedTypeFactory(FuncChecker checker, CompilationUnitTree root) {
        super(checker, root, false);
    }

    @Override
    protected void annotateImplicit(Element el, AnnotatedTypeMirror type) {
        super.annotateImplicit(el, type);

        if (el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.CONSTRUCTOR) {
            System.err.println("For " + el.getSimpleName() + " " + CanonicalName.forMethod((ExecutableElement) el));
            System.err.println(el.getEnclosingElement().getKind().toString() + el.getEnclosingElement().getSimpleName());
            for (AnnotationMirror m : el.getEnclosingElement().getAnnotationMirrors()) {
                System.err.println(m.toString());
            };
        }


        if (el.getKind() == ElementKind.METHOD) {
            if (el.getEnclosingElement().getAnnotation(Immutable.class) != null) {
                type.addAnnotation(ReadOnly.class);
            }
        } else if (el.getKind() == ElementKind.CONSTRUCTOR) {
            if (el.getEnclosingElement().getAnnotation(Immutable.class) != null) {
                type.addAnnotation(WriteLocal.class);
                type.addAnnotation(Anonymous.class);
            }
        }
    }

    @Override
    protected void annotateImplicit(Tree tree, AnnotatedTypeMirror type) {
        super.annotateImplicit(tree, type);

        if (tree.getKind() == Tree.Kind.VARIABLE) {
            VariableTree vt = (VariableTree) tree;
            vt.getType();

            System.err.println("Annotating " + vt.getType().getKind().toString() + " " + vt.getType().toString());
        }
    }



    
}
