package checkers.jimmu;

import checkers.flow.Flow;
import checkers.flow.GenKillBits;
import checkers.jimmu.quals.*;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedArrayType;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.types.BasicAnnotatedTypeFactory;
import checkers.util.AnnotationUtils;
import checkers.util.ElementUtils;
import checkers.util.TreeUtils;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

/**
 * Determines annotations based on rules governing JimmuChecker's annotations.
 */
public class JimmuAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<JimmuChecker> {

    protected final AnnotationUtils annotationFactory;
    protected JimmuChecker checker;
    protected JimmuVisitorState state;

    public JimmuAnnotatedTypeFactory(JimmuChecker checker, CompilationUnitTree root, JimmuVisitorState state) {
        super(checker, root, true);
        annotationFactory = checker.getAnnotationFactory();
        this.checker = checker;
        this.state = state;
        state.setFactory(this);
    }

    @Override
    public AnnotatedTypeMirror getAnnotatedType(Tree tree) {
        AnnotatedTypeMirror type = super.getAnnotatedType(tree);

        if (tree.getKind() == Tree.Kind.METHOD_INVOCATION) {
            MethodInvocationTree inv = (MethodInvocationTree) tree;
            ExecutableElement mElem = TreeUtils.elementFromUse(inv);
            AnnotatedExecutableType mType = getAnnotatedType(mElem);
            AnnotatedTypeMirror receiverType = getReceiver(inv);
            if (receiverType != null
                    && (receiverType.hasAnnotation(checker.THIS)
                    || receiverType.hasAnnotation(checker.MAYBE_THIS)
                    || TreeUtils.isSelfAccess(inv))) {
                if (mType.hasAnnotation(checker.ANONYMOUS)) {
                    type.addAnnotation(checker.NOT_THIS);
                } else {
                    type.addAnnotation(checker.MAYBE_THIS);
                }
            } else {
                /* Calling method on a non-this value or a static method.
                 *
                 * The result will not be THIS, provided we did not
                 * call a non-@Anonymous method before or otherwise passed a
                 * reference to 'this' to another object.
                 */
                type.addAnnotation(checker.NOT_THIS);
            }

            /* Resolve @Myaccess to the access rights of the receiver */
            if (receiverType != null && mType.getReturnType().hasAnnotation(checker.MYACCESS)) {
                type.removeAnnotation(checker.MYACCESS);
                if (receiverType.hasAnnotation(checker.IMMUTABLE)) {
                    type.addAnnotation(checker.IMMUTABLE);
                } else if (receiverType.hasAnnotation(checker.MYACCESS)) {
                    type.addAnnotation(checker.MYACCESS);
                } else if (receiverType.hasAnnotation(checker.MUTABLE)) {
                    type.addAnnotation(checker.MUTABLE);
                }
            }

            /* Refine ownership type of the returned value based on the receiver type */
            AnnotatedTypeMirror returnType = mType.getReturnType();
            if (receiverType != null) {
                if (returnType.hasAnnotation(checker.PEER)) {
                    type.removeAnnotation(checker.PEER);
                    if (receiverType.hasAnnotation(checker.REP)) {
                        type.addAnnotation(checker.REP);
                    } else if (receiverType.hasAnnotation(checker.PEER)) {
                        type.addAnnotation(checker.PEER);
                    } else if (receiverType.hasAnnotation(checker.WORLD)) {
                        type.addAnnotation(checker.WORLD);
                    } else if (receiverType.hasAnnotation(checker.OWNEDBY)) {
                        JimmuVisitor.Owner retOwner = new JimmuVisitor.Owner(mElem, this);
                        type.addAnnotation(ownerAnnotation(retOwner.asString()));
                    }
                } else if (returnType.hasAnnotation(checker.OWNEDBY) 
                        && !TreeUtils.isSelfAccess(inv)) {
                    MemberSelectTree mst = (MemberSelectTree) inv.getMethodSelect();
                    JimmuVisitor.Owner retOwner = new JimmuVisitor.Owner(mElem, this);
                    JimmuVisitor.Owner selOwner = new JimmuVisitor.Owner(mst.getExpression(), this);
                    selOwner.append(retOwner);
                    type.removeAnnotation(checker.OWNEDBY);
                    type.addAnnotation(ownerAnnotation(selOwner.asString()));
                }
            }

            /* Add @Safe annotation on values returned from calls on @Safe objects */
            /* Only @Peer objects must be protected as @Rep cannot be returned */
            if (receiverType != null
                    && receiverType.hasAnnotation(checker.SAFE)
                    && returnType.hasAnnotation(checker.PEER)) {
                type.addAnnotation(checker.SAFE);
            }
            
        } else if (tree.getKind() == Tree.Kind.IDENTIFIER) {
            IdentifierTree ident = (IdentifierTree) tree;
            if (ident.getName().contentEquals("this")) {
                type.addAnnotation(checker.THIS);
            }
        } else if (tree.getKind() == Tree.Kind.NEW_ARRAY
                || tree.getKind() == Tree.Kind.NEW_CLASS) {
            type.addAnnotation(checker.NOT_THIS);
        } else if (tree.getKind() == Tree.Kind.MEMBER_SELECT) {

            MemberSelectTree mst = (MemberSelectTree) tree;
            Element elem = TreeUtils.elementFromUse(mst);
            AnnotatedTypeMirror elemType = getAnnotatedType(elem);
            AnnotatedTypeMirror expElemType = getAnnotatedType(mst.getExpression());

            /* Resolve @Myaccess to the access rights of expElem */
            if (elemType.hasAnnotation(checker.MYACCESS)) {
                type.removeAnnotation(checker.MYACCESS);
                if (expElemType.hasAnnotation(checker.IMMUTABLE)) {
                    type.addAnnotation(checker.IMMUTABLE);
                } else if (expElemType.hasAnnotation(checker.MYACCESS)) {
                    type.addAnnotation(checker.MYACCESS);
                } else if (expElemType.hasAnnotation(checker.MUTABLE)) {
                    type.addAnnotation(checker.MUTABLE);
                }
            }

            /* Resolve @Peer to the owner of expElem */
            if (elemType.hasAnnotation(checker.PEER) && !expElemType.hasAnnotation(checker.THIS)) {
                if (expElemType.hasAnnotation(checker.REP)) {
                    type.removeAnnotation(checker.PEER);
                    type.addAnnotation(checker.REP);
                } else if (expElemType.hasAnnotation(checker.WORLD)) {
                    type.removeAnnotation(checker.PEER);
                    type.addAnnotation(checker.WORLD);
                } else if (expElemType.hasAnnotation(checker.OWNEDBY)) {
                    JimmuVisitor.Owner elOwner = new JimmuVisitor.Owner(elem, this);
                    type.removeAnnotation(checker.PEER);
                    type.addAnnotation(ownerAnnotation(elOwner.asString()));
                }
            } else if (elemType.hasAnnotation(checker.OWNEDBY)) {
                JimmuVisitor.Owner elOwner = new JimmuVisitor.Owner(elem, this);
                JimmuVisitor.Owner expOwner = new JimmuVisitor.Owner(mst.getExpression(), this);
                expOwner.append(elOwner);
                type.removeAnnotation(checker.OWNEDBY);
                type.addAnnotation(ownerAnnotation(expOwner.asString()));
            }

            /* Add @Safe annotation to members of @Safe objects to protect their transitive reach */
            /* (Only @Rep and @Peer objects must be protected) */
            if (expElemType.hasAnnotation(checker.SAFE)
                    && type.hasAnnotation(checker.PEER)) {
                type.addAnnotation(checker.SAFE);
            }
        }

        /* Implicit @World on new etc. */
        if (tree instanceof ExpressionTree
                && !type.hasAnnotation(checker.REP) && !type.hasAnnotation(checker.PEER)
                && !type.hasAnnotation(checker.OWNEDBY) && !type.hasAnnotation(checker.ANYOWNER)
                && !type.hasAnnotation(checker.SAFE)) {
            type.addAnnotation(checker.WORLD);
        }

        /* Implicit protection of encapsulated values inside read-only methods (Rule 2) */
        if (state.isCurrentMethod(checker.READONLY)
                && (type.hasAnnotation(checker.PEER) || type.hasAnnotation(checker.OWNEDBY))
                && !type.hasAnnotation(checker.IMMUTABLE)) {
            type.addAnnotation(checker.IMMUTABLE);
        }

        return type;
    }

    @Override
    public AnnotatedTypeMirror getAnnotatedType(Element elt) {
        AnnotatedTypeMirror type = super.getAnnotatedType(elt);

        /* Resolve @Myaccess on @ImmutableClasses to @Immutable */
        TypeElement enclosingClass = ElementUtils.enclosingClass(elt);
        if (enclosingClass.getAnnotation(ImmutableClass.class) != null
                && type.hasAnnotation(checker.MYACCESS)) {
            type.removeAnnotation(checker.MYACCESS);
            type.addAnnotation(checker.IMMUTABLE);
        }

        if (elt.getKind() == ElementKind.PARAMETER) {
            if (state.inConstructor()) {
                type.addAnnotation(checker.NOT_THIS);
            } else {
                type.addAnnotation(checker.MAYBE_THIS);
            }
        }

        /* Implicit ownership */
        if (!type.hasAnnotation(checker.REP) && !type.hasAnnotation(checker.PEER)
                && !type.hasAnnotation(checker.OWNEDBY) && !type.hasAnnotation(checker.ANYOWNER)
                && (elt.getKind() == ElementKind.FIELD || elt.getKind() == ElementKind.PARAMETER
                || elt.getKind() == ElementKind.LOCAL_VARIABLE)) {
            type.addAnnotation(checker.WORLD);
        }

        /* Add implicit @Immutable to objects @OwnedBy @Immutable objects. */
        if (type.hasAnnotation(checker.OWNEDBY)) {
            try {
                JimmuVisitor.Owner owner = new JimmuVisitor.Owner(elt, this);
                if (owner.isImmutable()) {
                    type.removeAnnotation(checker.MUTABLE);
                    type.removeAnnotation(checker.MYACCESS);
                    type.removeAnnotation(checker.IMMUTABLE);
                    type.addAnnotation(checker.IMMUTABLE);
                } else if (owner.isMyaccess()) {
                    if (!type.hasAnnotation(checker.IMMUTABLE)) {
                        type.removeAnnotation(checker.MUTABLE);
                        type.removeAnnotation(checker.MYACCESS);
                        type.addAnnotation(checker.MYACCESS);
                    }
                }
            } catch (JimmuVisitor.Owner.OwnerDescriptionError err) {
                /* Swallow the exception, it should have already been reported */
            }
        }

        /* Implicit protection of encapsulated values inside read-only methods (Rule 2) */
        if (state.isCurrentMethod(checker.READONLY)
                && (type.hasAnnotation(checker.PEER) || type.hasAnnotation(checker.OWNEDBY))
                && !type.hasAnnotation(checker.IMMUTABLE)) {
            type.addAnnotation(checker.IMMUTABLE);
        }

        return type;
    }

    @Override
    protected void annotateImplicit(Tree tree, AnnotatedTypeMirror type) {
        super.annotateImplicit(tree, type);
        if (tree.getKind() == Tree.Kind.METHOD) {
            refineMethodType((AnnotatedExecutableType) type);
        }
        if (tree.getKind() != Tree.Kind.CLASS
                && tree.getKind() != Tree.Kind.COMPILATION_UNIT
                && tree.getKind() != Tree.Kind.IMPORT) {
            implicitAccessRights(type);
        }
    }

    @Override
    protected void annotateImplicit(Element el, AnnotatedTypeMirror type) {
        super.annotateImplicit(el, type);
        if (el.getKind() == ElementKind.CONSTRUCTOR || el.getKind() == ElementKind.METHOD) {
            refineMethodType((AnnotatedExecutableType) type);
        }
        if (el.getKind() != ElementKind.CLASS
                && el.getKind() != ElementKind.ENUM
                && el.getKind() != ElementKind.INTERFACE
                && el.getKind() != ElementKind.PACKAGE) {
            implicitAccessRights(type);
        }
    }

    protected void refineMethodType(AnnotatedExecutableType type) {
        /* Add implicit annotations to methods of Immutable classes */
        ExecutableElement el = type.getElement();
        TypeElement enclosingClass = ElementUtils.enclosingClass(el);
        AnnotatedTypeMirror.AnnotatedDeclaredType enclosingClassType =
                getAnnotatedType(enclosingClass);
        if (enclosingClassType.hasAnnotation(checker.IMMUTABLE_CLASS)) {
            AnnotationMirror implicit = el.getKind() == ElementKind.CONSTRUCTOR
                    ? checker.ANONYMOUS
                    : checker.READONLY;
            if (!type.hasAnnotation(implicit)) {
                type.addAnnotation(implicit);
                state.addImplicitAnnotation(el, implicit);
            }
        }

        /* Add the @Immutable annotation to receivers of @ReadOnly methods */
        if (type.hasAnnotation(checker.READONLY)) {
            AnnotatedTypeMirror.AnnotatedDeclaredType receiver = type.getReceiverType();
            if (receiver != null) {
                receiver.removeAnnotation(checker.MUTABLE);
                receiver.removeAnnotation(checker.MYACCESS);
                receiver.addAnnotation(checker.IMMUTABLE);
            }
        }
    }

    /**
     * Refine the type of an instance method's receiver by adding the @This annotation.
     *
     * @return the type of 'this' in the current location. 
     */
    @Override
    public AnnotatedDeclaredType getSelfType(Tree tree) {
        AnnotatedDeclaredType type = super.getSelfType(tree);
        type.addAnnotation(checker.THIS);
        if (!type.hasAnnotation(checker.IMMUTABLE)
                && !type.hasAnnotation(checker.MYACCESS)) {
            type.addAnnotation(checker.MUTABLE);
        }
        return type;
    }

    /**
     * Add the implicit @Mutable annotation to avoid unqualified types.
     *
     * @param type the type to be refined.
     */
    protected void implicitAccessRights(AnnotatedTypeMirror type) {

        if (!(type instanceof AnnotatedExecutableType)
                && !type.hasAnnotation(checker.IMMUTABLE)
                && !type.hasAnnotation(checker.MYACCESS)
                && !type.hasAnnotation(checker.MUTABLE)) {
            type.removeAnnotation(checker.BOTTOM);
            type.addAnnotation(checker.MUTABLE);
        }

        if (type.getKind() == TypeKind.EXECUTABLE) {
            AnnotatedExecutableType ext = (AnnotatedExecutableType) type;
            if (type.getElement().getKind() != ElementKind.CONSTRUCTOR) {
                implicitAccessRights(ext.getReceiverType());
                implicitAccessRights(ext.getReturnType());
            }
            for (AnnotatedTypeMirror t : ext.getParameterTypes()) {
                implicitAccessRights(t);
            }
        } else if (type.getKind() == TypeKind.ARRAY) {
            AnnotatedArrayType art = (AnnotatedArrayType) type;
            implicitAccessRights(art.getComponentType());
        } else if (type.getKind() == TypeKind.DECLARED) {
            AnnotatedDeclaredType dt = (AnnotatedDeclaredType) type;
            for (AnnotatedTypeMirror arg : dt.getTypeArguments()) {
                implicitAccessRights(arg);
            }
        }
    }

    /**
     * Resolve the @Myaccess type using the access rights type of an enclosing object.
     */
    protected void resolveMyaccess(AnnotatedTypeMirror child, AnnotatedTypeMirror parent) {
        if (child.hasAnnotation(checker.MYACCESS)) {
            child.removeAnnotation(checker.MYACCESS);
            if (parent.hasAnnotation(checker.IMMUTABLE)) {
                child.addAnnotation(checker.IMMUTABLE);
            } else if (parent.hasAnnotation(checker.MYACCESS)) {
                child.addAnnotation(checker.MYACCESS);
            } else if (parent.hasAnnotation(checker.MUTABLE)) {
                child.addAnnotation(checker.MUTABLE);
            }
        }
    }

    public static class JimmuFlow extends Flow {

        protected JimmuChecker checker;
        MethodTree currentMethod;

        public JimmuFlow(JimmuChecker checker, CompilationUnitTree root,
                Set<AnnotationMirror> flowQuals, JimmuAnnotatedTypeFactory factory) {
            super(checker, root, flowQuals, factory);
            this.checker = checker;
        }

        @Override
        protected void merge(GenKillBits<AnnotationMirror> bits, GenKillBits<AnnotationMirror> other) {
            bits.or(other);
            for (int i = 0; i < vars.size(); i++) {
                if (bits.get(checker.MAYBE_THIS, i)
                        || (bits.get(checker.THIS, i) && bits.get(checker.NOT_THIS, i))) {
                    bits.clear(checker.THIS, i);
                    bits.clear(checker.NOT_THIS, i);
                    bits.set(checker.MAYBE_THIS, i);
                }
            }
        }

        @Override
        /* We need the state during Flow analysis */
        public Void scan(Tree tree, Void p) {
            if (tree != null && tree.getKind() == Tree.Kind.METHOD) {
                try {
                    checker.getState().enterMethodFlow((MethodTree) tree);
                    return super.scan(tree, p);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    checker.getState().leaveMethodFlow();
                }
            } else {
                return super.scan(tree, p);
            }
        }
    }

    @Override
    protected Flow createFlow(JimmuChecker checker, CompilationUnitTree root, Set<AnnotationMirror> flowQuals) {
        return new JimmuFlow(checker, root, flowQuals, this);
    }

    @Override
    protected Set<AnnotationMirror> createFlowQualifiers(JimmuChecker checker) {
        Set<AnnotationMirror> flowQuals = new HashSet<AnnotationMirror>();
        flowQuals.add(checker.THIS);
        flowQuals.add(checker.NOT_THIS);
        flowQuals.add(checker.MAYBE_THIS);
        return flowQuals;
    }

    /**
     * Return the value of the OwnedBy annotation on given type.
     */
    public String getOwner(Element el) {
        List<? extends AnnotationMirror> mirrors = el.getAnnotationMirrors();
        for (AnnotationMirror m : mirrors) {
            if ("OwnedBy".equals(m.getAnnotationType().asElement().getSimpleName().toString())) {
                return AnnotationUtils.elementValue(m, "value", String.class);
            }
        }
        return null;
    }

    public String getOwner(Tree t) {
        return getOwner(getAnnotatedType(t));
    }

    public String getOwner(AnnotatedTypeMirror t) {
        AnnotationMirror ob = t.getAnnotation(OwnedBy.class);
        if (ob != null) {
            return AnnotationUtils.elementValue(ob, "value", String.class);
        } else {
            return null;
        }
    }

    public AnnotationMirror ownerAnnotation(String owner) {
        AnnotationUtils.AnnotationBuilder builder =
                new AnnotationUtils.AnnotationBuilder(env, OwnedBy.class.getCanonicalName());
        builder.setValue("value", owner);
        return builder.build();
    }
}
