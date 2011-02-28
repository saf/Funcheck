package checkers.func;

import checkers.util.ElementUtils;
import checkers.util.InternalUtils;
import checkers.util.TypesUtils;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Generates canonical names for use in Prolog facts.
 *
 * The canonical names are generated for Java entities such as
 * classes and methods. They aim to provide a unique name regardless
 * of overriding methods in subclasses and method overloading.
 *
 * The resulting names are Prolog atoms.
 */
public class CanonicalName {

    public static String forClass(ClassTree node) {
        return "c_" + ElementUtils.getQualifiedClassName(InternalUtils.symbol(node)).toString();
    }

    public static String forMethod(MethodTree node) {
        return forMethod((ExecutableElement) InternalUtils.symbol(node));
    }

    public static String forMethod(ExecutableElement element) {
        String className = ElementUtils.getQualifiedClassName(element).toString();
        StringBuilder builder = new StringBuilder();
        builder.append(element.getSimpleName().toString());
        List<? extends VariableElement> parameters = element.getParameters();
        builder.append("_" + Integer.toString(parameters.size()));
        for (VariableElement p : parameters) {
            builder.append("_" + p.asType().toString());
        };
        return "m_" + className + "->" + builder.toString();
    }

}
