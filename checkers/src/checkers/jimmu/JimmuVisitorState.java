/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.jimmu;

import checkers.jimmu.JimmuVisitor.Owner;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.*;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;

/**
 *
 * @author saf
 */
public class JimmuVisitorState {

    protected JimmuAnnotatedTypeFactory atypeFactory;

    protected Stack<AnnotatedDeclaredType> enclosingClassStack;
    protected Stack<AnnotatedExecutableType> enclosingMethodStack;

    /* Current class annotated type */
    protected AnnotatedDeclaredType currentClass;

    /* Current method annotated type */
    protected AnnotatedExecutableType currentMethod;

    /* Current method. Used in Flow context only */
    protected MethodTree currentMethodFlow;
    protected Stack<MethodTree> enclosingMethodsFlow;

    /* Block -- represents the  */
    protected static class Block {

        private Map<String, AnnotatedTypeMirror> vars;

        public Block() {
            vars = new HashMap<String, AnnotatedTypeMirror>();
        }

        public AnnotatedTypeMirror get(String s) {
            return vars.get(s);
        }

        public void add(String s, AnnotatedTypeMirror m) {
            vars.put(s, m);
        }
    }

    protected LinkedList<Block> blocks;

    /* Set of possible aliases of this in the current method. */
    /*
     * #TODO
     *   * update after assignment of non-this.
     *   * split and conservatively merge on conditionals.
     */
    protected Map<Element, AssignmentTree> thisReferences;

    /* The type for the receiver of the current MethodInvocation.
     * Needed to infer type of paramaters in JimmuVisitor.
     */
    protected AnnotatedTypeMirror invocationReceiver;
    /* Owner of the current invocation receiver */
    protected JimmuVisitor.Owner invocationReceiverOwner;
    protected MethodInvocationTree currentInvocation;

    /* Map of implicit annotations that the Factory put on methods */
    protected Map<ExecutableElement, AnnotationMirror> implicitAnnotations;

    public JimmuVisitorState() {
        thisReferences = new HashMap<Element, AssignmentTree>();
        implicitAnnotations = new HashMap<ExecutableElement, AnnotationMirror>();
        enclosingClassStack = new Stack<AnnotatedDeclaredType>();
        enclosingMethodStack = new Stack<AnnotatedExecutableType>();
        enclosingMethodsFlow = new Stack<MethodTree>();
        blocks = new LinkedList<Block>();
    }

    public void setFactory(JimmuAnnotatedTypeFactory factory) {
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
        return currentClass != null && currentClass.hasAnnotation(ann);
    }
   
    public String getCurrentClassName() {
        return currentClass.getElement().getSimpleName().toString();
    }

    public void enterMethod(MethodTree tree) {
        if (currentMethod != null) {
            enclosingMethodStack.push(currentMethod);
        }
        currentMethod = atypeFactory.getAnnotatedType(tree);
//        System.err.println("ENTERING METHOD: " + currentMethod.getElement().getSimpleName().toString()
//                + " ---> " + currentMethod.toString());
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
        /*
         * currentMethod may be null when doing Flow analysis, but
         * the result of this method is meaningless for flow-inferred properties.
         */
        return currentMethod != null && currentMethod.hasAnnotation(ann);
    }

    public Boolean isCurrentMethodReceiver(AnnotationMirror ann) {
        return currentMethod.getReceiverType().hasAnnotation(ann);
    }

    public String getCurrentMethodName() {
        return currentMethod.getElement().getSimpleName().toString();
    }

    public boolean isThisAlias(Element el) {
        return thisReferences.containsKey(el);
    }

    public void addThisAlias(Element el, AssignmentTree tr) {
        thisReferences.put(el, tr);
    }

    public AssignmentTree getThisAssignment(Element el) {
        return thisReferences.get(el);
    }

    public void addImplicitAnnotation(ExecutableElement el, AnnotationMirror an) {
        implicitAnnotations.put(el, an);
    }

    public boolean isImplicitlyAnnotated(ExecutableElement el) {
        return implicitAnnotations.containsKey(el);
    }

    public boolean inImplicitlyAnnotatedMethod() {
        return isImplicitlyAnnotated(currentMethod.getElement());
    }

    public AnnotationMirror getImplicitAnnotation(ExecutableElement el) {
        return implicitAnnotations.get(el);
    }

    public Boolean inConstructor() {
        return currentMethod != null 
                ? currentMethod.getElement().getKind().equals(ElementKind.CONSTRUCTOR)
                : (currentMethodFlow.getReturnType() == null);
    }

    public void setCurrentInvocation(Tree t) {
        if (t.getKind() == Tree.Kind.METHOD_INVOCATION) {
            MethodInvocationTree mt = (MethodInvocationTree) t;
            currentInvocation = mt;
            invocationReceiver = atypeFactory.getReceiver(mt);
        } else if (t.getKind() == Tree.Kind.NEW_CLASS) {
            NewClassTree nt = (NewClassTree) t;
            currentInvocation = null;
            invocationReceiver = atypeFactory.fromTypeTree(nt.getIdentifier());
        }

        if (invocationReceiver != null) {
            try {
                invocationReceiverOwner =
                        new JimmuVisitor.Owner(invocationReceiver.getElement(), atypeFactory);
            } catch (Owner.OwnerDescriptionError e) {
                /* Ignore; should have already been reported */
                invocationReceiverOwner = null;
            }
        } else {
            invocationReceiverOwner = null;
        }
    }

    public Boolean isReceiver(AnnotationMirror m) {
        return invocationReceiver == null ? false : invocationReceiver.hasAnnotation(m);
    }

    public Owner getReceiverOwner() {
        return invocationReceiverOwner;
    }

    public MethodInvocationTree getCurrentInvocation() {
        return currentInvocation;
    }

    public void enterMethodFlow(MethodTree tree) {
        if (currentMethodFlow != null) {
            enclosingMethodsFlow.push(currentMethodFlow);
        }
        currentMethodFlow = tree;
    }

    public void leaveMethodFlow() {
        if (!enclosingMethodsFlow.isEmpty()) {
            currentMethodFlow = enclosingMethodsFlow.pop();
        } else {
            currentMethodFlow = null;
        }
    }

    public void enterBlock() {
        blocks.push(new Block());
    }

    public void leaveBlock() {
        blocks.pop();
    }

    public void addVariable(String s, AnnotatedTypeMirror m) {
        blocks.peek().add(s, m);
    }

    public void shadowVariable(String s) {
        /*
         * If a variable is shadowed by a field, we put a dummy entry
         * into the first block's vars which acts as if the variable did not exist.
         */
        if (!blocks.isEmpty()) {
            blocks.peek().add(s, null);
        }
    }

    public AnnotatedTypeMirror localVariable(String s) {
        Iterator<Block> it = blocks.iterator();
        while (it.hasNext()) {
            Block b = it.next();
            AnnotatedTypeMirror m = b.get(s);
            if (m != null) {
                return m;
            }
        }
        return null;
    }
}
