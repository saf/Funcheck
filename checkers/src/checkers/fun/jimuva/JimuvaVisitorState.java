/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.fun.jimuva;

import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.*;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;

/**
 *
 * @author saf
 */
public class JimuvaVisitorState {

    protected JimuvaAnnotatedTypeFactory atypeFactory;

    protected Stack<AnnotatedDeclaredType> enclosingClassStack;
    protected Stack<AnnotatedExecutableType> enclosingMethodStack;

    /* Current class annotated type */
    protected AnnotatedDeclaredType currentClass;

    /* Current method annotated type */
    protected AnnotatedExecutableType currentMethod;

    /* Set of possible aliases of this in the current method. */
    /*
     * #TODO
     *   * update after assignment of non-this.
     *   * split and conservatively merge on conditionals.
     */
    protected Map<Element, AssignmentTree> thisReferences;

    /* Map of implicit annotations that the Factory put on methods */
    protected Map<ExecutableElement, AnnotationMirror> implicitAnnotations;

    public JimuvaVisitorState() {
        thisReferences = new HashMap<Element, AssignmentTree>();
        implicitAnnotations = new HashMap<ExecutableElement, AnnotationMirror>();
        enclosingClassStack = new Stack<AnnotatedDeclaredType>();
        enclosingMethodStack = new Stack<AnnotatedExecutableType>();
    }

    public void setFactory(JimuvaAnnotatedTypeFactory factory) {
        this.atypeFactory = factory;
    }

    public void enterClass(ClassTree tree) {
        if (currentClass != null) {
            enclosingClassStack.push(currentClass);
        }
        currentClass = atypeFactory.getAnnotatedType(tree);
    }

    public void leaveClass() {
        if (!enclosingClassStack.isEmpty()) {
            currentClass = enclosingClassStack.pop();
        } else {
            currentClass = null;
        }
    }

    public AnnotatedTypeMirror getCurrentClass() {
        return currentClass;
    }

    public Boolean isCurrentClass(AnnotationMirror ann) {
        return currentClass.hasAnnotation(ann);
    }

    public void enterMethod(MethodTree tree) {
        if (currentMethod != null) {
            enclosingMethodStack.push(currentMethod);
        }
        currentMethod = atypeFactory.getAnnotatedType(tree);
    }

    public void leaveMethod() {
        if (!enclosingMethodStack.isEmpty()) {
            currentMethod = enclosingMethodStack.pop();
        } else {
            currentMethod = null;
        }
    }

    public AnnotatedExecutableType getCurrentMethod() {
        return currentMethod;
    }

    public Boolean isCurrentMethod(AnnotationMirror ann) {
        return currentMethod.hasAnnotation(ann);
    }

    public boolean isThisAlias(Element el) {
        return thisReferences.containsKey(el);
    }

    public void addThisAlias(Element el, AssignmentTree tr) {
        thisReferences.put(el, tr);
    }

    public void addImplicitAnnotation(ExecutableElement el, AnnotationMirror an) {
        implicitAnnotations.put(el, an);
    }

    public boolean isImplicitlyAnnotated(ExecutableElement el) {
        return implicitAnnotations.containsKey(el);
    }

    public AnnotationMirror getImplicitAnnotation(ExecutableElement el) {
        return implicitAnnotations.get(el);
    }

}
