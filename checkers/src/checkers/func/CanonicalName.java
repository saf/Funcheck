package checkers.func;

import checkers.util.ElementUtils;
import checkers.util.InternalUtils;
import checkers.util.TypesUtils;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import javax.lang.model.element.ExecutableElement;

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

    protected static String getMethodParameterString(ExecutableElement el) {
        return ""; /* TODO */
    }

    public static String forClass(ClassTree node) {
        return "c_" + ElementUtils.getQualifiedClassName(InternalUtils.symbol(node)).toString();
    }

    public static String forMethod(MethodTree node) {
        return forMethod((ExecutableElement) InternalUtils.symbol(node));
    }

    public static String forMethod(ExecutableElement element) {
        String className = ElementUtils.getQualifiedClassName(element).toString();
        String methodName = element.getSimpleName().toString();
        return "m_" + className + "->" + methodName + getMethodParameterString(element);
    }

}
