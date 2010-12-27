/*
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.tools.javac.code;

import static com.sun.tools.javac.code.Flags.ANNOTATION;
import static com.sun.tools.javac.code.Flags.PARAMETER;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTags.VOID;

import javax.lang.model.element.ElementKind;

import com.sun.tools.javac.code.Attribute.Compound;
import com.sun.tools.javac.code.Attribute.TypeCompound;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.comp.Annotate.Annotator;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

/**
 * Contains operations specific to processing type annotations
 */
public class TypeAnnotations {
    private static final Context.Key<TypeAnnotations> key
        = new Context.Key<TypeAnnotations>();

    public static TypeAnnotations instance(Context context) {
        TypeAnnotations instance = context.get(key);
        if (instance == null)
            instance = new TypeAnnotations(context);
        return instance;
    }

    private final Symtab syms;
    private final Names names;

    protected TypeAnnotations(Context context) {
        context.put(key, this);
        syms = Symtab.instance(context);
        names = Names.instance(context);
    }

    public Annotator annotator(final JCClassDecl tree) {
        return new Annotator() {

            @Override
            public void enterAnnotation() {
                taFillAndLift(tree, false);
            }

        };
    }

    public void taFillAndLift(List<JCCompilationUnit> trees, boolean visitBodies) {
        for (JCCompilationUnit tree : trees) {
            for (JCTree def : tree.defs) {
                if (def.getTag() == JCTree.CLASSDEF)
                    taFillAndLift((JCClassDecl)def, visitBodies);
            }
        }
    }

    public void taFillAndLift(JCClassDecl tree, boolean visitBodies) {
        new AnnotationsKindSeparator(visitBodies).scan(tree);
        new TypeAnnotationPositions(visitBodies).scan(tree);
        new TypeAnnotationLift(visitBodies).scan(tree);
    }

    enum AnnotationType { DECLARATION, TYPE, BOTH };

    /**
     * Separates type annotations from declaration annotations
     */
    private class AnnotationsKindSeparator extends TreeScanner {

        private final boolean visitBodies;
        public AnnotationsKindSeparator(boolean visitBodies) {
            this.visitBodies = visitBodies;
        }

        // each class (including enclosed inner classes) should be visited
        // separately through MemberEnter.complete(Symbol)
        // this flag is used to prevent from visiting inner classes.
        private boolean isInner = false;
        @Override
        public void visitClassDef(final JCClassDecl tree) {
            if (isInner)
                return;
            isInner = true;
            super.visitClassDef(tree);
        }

        @Override
        public void visitMethodDef(final JCMethodDecl tree) {
            // clear all annotations
            if (!visitBodies) {
                if (!areAllDecl(tree.sym))
                    separateAnnotationsKinds(tree.sym, tree.sym.type.getReturnType(),
                            new TypeAnnotationPosition(TargetType.METHOD_RETURN));
                int i = 0;
                for (JCVariableDecl param : tree.params) {
                    if (!areAllDecl(param.sym)) {
                        TypeAnnotationPosition pos =
                            new TypeAnnotationPosition(TargetType.METHOD_PARAMETER);
                        pos.parameter_index = i;
                        separateAnnotationsKinds(param.sym, param.sym.type, pos);
                    }
                    ++i;
                }
            }
            super.visitMethodDef(tree);
        }

        @Override
        public void visitVarDef(final JCVariableDecl tree) {
            if (!visitBodies && !areAllDecl(tree.sym)) {
                if (tree.sym.getKind() == ElementKind.FIELD) {
                    separateAnnotationsKinds(tree.sym, tree.sym.type,
                            new TypeAnnotationPosition(TargetType.FIELD));
                } else if (tree.sym.getKind() == ElementKind.LOCAL_VARIABLE) {
                    TypeAnnotationPosition pos = new TypeAnnotationPosition(TargetType.LOCAL_VARIABLE);
                    pos.pos = tree.pos;
                    separateAnnotationsKinds(tree.sym, tree.sym.type, pos);
                }

            }
            super.visitVarDef(tree);
        }

        @Override
        public void visitBlock(final JCBlock tree) {
            if (visitBodies)
                super.visitBlock(tree);
        }
    }

    private static class TypeAnnotationPositions extends TreeScanner {

        private final boolean visitBodies;
        TypeAnnotationPositions(boolean visitBodies) {
            this.visitBodies = visitBodies;
        }

        private ListBuffer<JCTree> frames = ListBuffer.lb();
        private void push(JCTree t) { frames = frames.prepend(t); }
        private JCTree pop() { return frames.next(); }
        private JCTree peek2() { return frames.toList().tail.head; }

        @Override
        public void scan(JCTree tree) {
            push(tree);
            super.scan(tree);
            pop();
        }

        // each class (including enclosed inner classes) should be visited
        // separately through MemberEnter.complete(Symbol)
        // this flag is used to prevent from visiting inner classes.
        private boolean isInner = false;
        @Override
        public void visitClassDef(final JCClassDecl tree) {
            if (isInner)
                return;
            isInner = true;
            super.visitClassDef(tree);
        }

        private TypeAnnotationPosition resolveFrame(JCTree tree, JCTree frame,
                List<JCTree> path, TypeAnnotationPosition p) {
            switch (frame.getKind()) {
                case TYPE_CAST:
                    p.type = TargetType.TYPECAST;
                    p.pos = frame.pos;
                    return p;

                case INSTANCE_OF:
                    p.type = TargetType.INSTANCEOF;
                    p.pos = frame.pos;
                    return p;

                case NEW_CLASS:
                    JCNewClass frameNewClass = (JCNewClass)frame;
                    if (frameNewClass.typeargs.contains(tree)) {
                        p.type = TargetType.NEW_TYPE_ARGUMENT;
                        p.type_index = frameNewClass.typeargs.indexOf(tree);
                    } else {
                        p.type = TargetType.NEW;
                    }
                    p.pos = frame.pos;
                    return p;

                case NEW_ARRAY:
                    p.type = TargetType.NEW;
                    p.pos = frame.pos;
                    return p;

                case CLASS:
                    p.pos = frame.pos;
                    if (((JCClassDecl)frame).extending == tree) {
                        p.type = TargetType.CLASS_EXTENDS;
                        p.type_index = -1;
                    } else if (((JCClassDecl)frame).implementing.contains(tree)) {
                        p.type = TargetType.CLASS_EXTENDS;
                        p.type_index = ((JCClassDecl)frame).implementing.indexOf(tree);
                    } else if (((JCClassDecl)frame).typarams.contains(tree)) {
                        p.type = TargetType.CLASS_TYPE_PARAMETER;
                        p.parameter_index = ((JCClassDecl)frame).typarams.indexOf(tree);
                    } else
                        throw new AssertionError();
                    return p;

                case METHOD: {
                    JCMethodDecl frameMethod = (JCMethodDecl)frame;
                    p.pos = frame.pos;
                    if (frameMethod.receiverAnnotations.contains(tree))
                        p.type = TargetType.METHOD_RECEIVER;
                    else if (frameMethod.thrown.contains(tree)) {
                        p.type = TargetType.THROWS;
                        p.type_index = frameMethod.thrown.indexOf(tree);
                    } else if (((JCMethodDecl)frame).restype == tree) {
                        p.type = TargetType.METHOD_RETURN;
                    } else if (frameMethod.typarams.contains(tree)) {
                        p.type = TargetType.METHOD_TYPE_PARAMETER;
                        p.parameter_index = frameMethod.typarams.indexOf(tree);
                    } else
                        throw new AssertionError();
                    return p;
                }
                case MEMBER_SELECT: {
                    JCFieldAccess fieldFrame = (JCFieldAccess)frame;
                    if ("class".contentEquals(fieldFrame.name)) {
                        p.type = TargetType.CLASS_LITERAL;
                        p.pos = TreeInfo.innermostType(fieldFrame.selected).pos;
                    } else
                        throw new AssertionError();
                    return p;
                }
                case PARAMETERIZED_TYPE: {
                    if (((JCTypeApply)frame).clazz == tree)
                    { } // generic: RAW; noop
                    else if (((JCTypeApply)frame).arguments.contains(tree))
                        p.location = p.location.prepend(
                                ((JCTypeApply)frame).arguments.indexOf(tree));
                    else
                        throw new AssertionError();

                    List<JCTree> newPath = path.tail;
                    return resolveFrame(newPath.head, newPath.tail.head, newPath, p);
                }

                case ARRAY_TYPE: {
                    int index = 0;
                    List<JCTree> newPath = path.tail;
                    while (true) {
                        JCTree npHead = newPath.tail.head;
                        if (npHead.getTag() == JCTree.TYPEARRAY) {
                            newPath = newPath.tail;
                            index++;
                        } else if (npHead.getTag() == JCTree.ANNOTATED_TYPE) {
                            newPath = newPath.tail;
                        } else {
                            break;
                        }
                    }
                    p.location = p.location.prepend(index);
                    return resolveFrame(newPath.head, newPath.tail.head, newPath, p);
                }

                case TYPE_PARAMETER:
                    if (path.tail.tail.head.getTag() == JCTree.CLASSDEF) {
                        JCClassDecl clazz = (JCClassDecl)path.tail.tail.head;
                        p.type = TargetType.CLASS_TYPE_PARAMETER_BOUND;
                        p.parameter_index = clazz.typarams.indexOf(path.tail.head);
                        p.bound_index = ((JCTypeParameter)frame).bounds.indexOf(tree);
                    } else if (path.tail.tail.head.getTag() == JCTree.METHODDEF) {
                        JCMethodDecl method = (JCMethodDecl)path.tail.tail.head;
                        p.type = TargetType.METHOD_TYPE_PARAMETER_BOUND;
                        p.parameter_index = method.typarams.indexOf(path.tail.head);
                        p.bound_index = ((JCTypeParameter)frame).bounds.indexOf(tree);
                    } else
                        throw new AssertionError();
                    p.pos = frame.pos;
                    return p;

                case VARIABLE:
                    VarSymbol v = ((JCVariableDecl)frame).sym;
                    p.pos = frame.pos;
                    switch (v.getKind()) {
                        case LOCAL_VARIABLE:
                            p.type = TargetType.LOCAL_VARIABLE; break;
                        case FIELD:
                            p.type = TargetType.FIELD; break;
                        case PARAMETER:
                            p.type = TargetType.METHOD_PARAMETER;
                            p.parameter_index = methodParamIndex(path, frame);
                            break;
                        default: throw new AssertionError();
                    }
                    return p;

                case ANNOTATED_TYPE: {
                    List<JCTree> newPath = path.tail;
                    return resolveFrame(newPath.head, newPath.tail.head,
                            newPath, p);
                }

                case METHOD_INVOCATION: {
                    JCMethodInvocation invocation = (JCMethodInvocation)frame;
                    if (!invocation.typeargs.contains(tree))
                        throw new AssertionError("{" + tree + "} is not an argument in the invocation: " + invocation);
                    p.type = TargetType.METHOD_TYPE_ARGUMENT;
                    p.pos = invocation.pos;
                    p.type_index = invocation.typeargs.indexOf(tree);
                    return p;
                }

                case EXTENDS_WILDCARD:
                case SUPER_WILDCARD: {
                    p.type = TargetType.WILDCARD_BOUND;
                    List<JCTree> newPath = path.tail;

                    TypeAnnotationPosition wildcard =
                        resolveFrame(newPath.head, newPath.tail.head, newPath,
                                new TypeAnnotationPosition());
                    if (!wildcard.location.isEmpty())
                        wildcard.type = wildcard.type.getGenericComplement();
                    p.wildcard_position = wildcard;
                    p.pos = frame.pos;
                    return p;
                }
            }
            return p;
        }

        private void setTypeAnnotationPos(List<JCTypeAnnotation> annotations, TypeAnnotationPosition position) {
            for (JCTypeAnnotation anno : annotations) {
                anno.annotation_position = position;
                anno.attribute_field.position = position;
            }
        }

        @Override
        public void visitNewArray(JCNewArray tree) {
            findPosition(tree, tree, tree.annotations);
            int dimAnnosCount = tree.dimAnnotations.size();

            // handle annotations associated with dimentions
            for (int i = 0; i < dimAnnosCount; ++i) {
                TypeAnnotationPosition p = new TypeAnnotationPosition();
                p.pos = tree.pos;
                if (i == 0) {
                    p.type = TargetType.NEW;
                } else {
                    p.type = TargetType.NEW_GENERIC_OR_ARRAY;
                    p.location = p.location.append(i - 1);
                }

                setTypeAnnotationPos(tree.dimAnnotations.get(i), p);
            }

            // handle "free" annotations
            int i = dimAnnosCount == 0 ? 0 : dimAnnosCount - 1;
            JCExpression elemType = tree.elemtype;
            while (elemType != null) {
                if (elemType.getTag() == JCTree.ANNOTATED_TYPE) {
                    JCAnnotatedType at = (JCAnnotatedType)elemType;
                    TypeAnnotationPosition p = new TypeAnnotationPosition();
                    p.type = TargetType.NEW_GENERIC_OR_ARRAY;
                    p.pos = tree.pos;
                    p.location = p.location.append(i);
                    setTypeAnnotationPos(at.annotations, p);
                    elemType = at.underlyingType;
                } else if (elemType.getTag() == JCTree.TYPEARRAY) {
                    ++i;
                    elemType = ((JCArrayTypeTree)elemType).elemtype;
                } else
                    break;
            }

            // TODO: Is this needed?
            scan(tree.elems);
        }

        @Override
        public void visitAnnotatedType(JCAnnotatedType tree) {
            findPosition(tree, peek2(), tree.annotations);
            super.visitAnnotatedType(tree);
        }

        @Override
        public void visitMethodDef(JCMethodDecl tree) {
            super.visitMethodDef(tree);
            TypeAnnotationPosition p = new TypeAnnotationPosition();
            p.type = TargetType.METHOD_RECEIVER;
            setTypeAnnotationPos(tree.receiverAnnotations, p);
        }

        @Override
        public void visitBlock(JCBlock tree) {
            if (visitBodies)
                super.visitBlock(tree);
        }

        @Override
        public void visitTypeParameter(JCTypeParameter tree) {
            findPosition(tree, peek2(), tree.annotations);
            super.visitTypeParameter(tree);
        }

        void findPosition(JCTree tree, JCTree frame, List<JCTypeAnnotation> annotations) {
            if (!annotations.isEmpty()) {
                TypeAnnotationPosition p =
                        resolveFrame(tree, frame, frames.toList(),
                                new TypeAnnotationPosition());
                if (!p.location.isEmpty())
                    p.type = p.type.getGenericComplement();
                setTypeAnnotationPos(annotations, p);
            }
        }

        private int methodParamIndex(List<JCTree> path, JCTree param) {
            List<JCTree> curr = path;
            if (curr.head != param)
                curr = path.tail;
            JCMethodDecl method = (JCMethodDecl)curr.tail.head;
            return method.params.indexOf(param);
        }
    }

    private static class TypeAnnotationLift extends TreeScanner {
        List<Attribute.TypeCompound> recordedTypeAnnotations = List.nil();

        // TODO: Find a better of handling this
        // Handle cases where the symbol typeAnnotation is filled multiple times
        private static <T> List<T> appendUnique(List<T> l1, List<T> l2) {
            if (l1.isEmpty() || l2.isEmpty())
                return l1.appendList(l2);

            ListBuffer<T> buf = ListBuffer.lb();
            buf.appendList(l1);
            for (T i : l2) {
                if (!l1.contains(i))
                    buf.append(i);
            }
            return buf.toList();
        }

        private final boolean visitBodies;
        TypeAnnotationLift(boolean visitBodies) {
            this.visitBodies = visitBodies;
        }

        boolean isInner = false;
        @Override
        public void visitClassDef(JCClassDecl tree) {
            if (isInner) {
                // tree is an inner class tree.  stop now.
                // TransTypes.visitClassDef makes an invocation for each class
                // separately.
                return;
            }
            isInner = true;
            List<Attribute.TypeCompound> prevTAs = recordedTypeAnnotations;
            recordedTypeAnnotations = List.nil();
            try {
                super.visitClassDef(tree);
            } finally {
                tree.sym.typeAnnotations = appendUnique(tree.sym.typeAnnotations, recordedTypeAnnotations);
                recordedTypeAnnotations = prevTAs;
            }
        }

        @Override
        public void visitMethodDef(JCMethodDecl tree) {
            List<Attribute.TypeCompound> prevTAs = recordedTypeAnnotations;
            recordedTypeAnnotations = List.nil();
            try {
                super.visitMethodDef(tree);
            } finally {
                tree.sym.typeAnnotations = appendUnique(tree.sym.typeAnnotations, recordedTypeAnnotations);
                recordedTypeAnnotations = prevTAs;
            }
        }

        @Override
        public void visitBlock(JCBlock tree) {
            if (visitBodies)
                super.visitBlock(tree);
        }

        private boolean isCatchParameter = false;

        @Override
        public void visitCatch(JCCatch tree) {
            isCatchParameter = true;
            scan(tree.param);
            isCatchParameter = false;
            scan(tree.body);
        }

        @Override
        public void visitVarDef(JCVariableDecl tree) {
            List<Attribute.TypeCompound> prevTAs = recordedTypeAnnotations;
            recordedTypeAnnotations = List.nil();
            ElementKind kind = tree.sym.getKind();
            if (tree.mods.annotations.nonEmpty()
                && (kind == ElementKind.LOCAL_VARIABLE || isCatchParameter)) {
                // need to lift the annotations
                TypeAnnotationPosition position = new TypeAnnotationPosition();
                position.pos = tree.pos;
                position.type = TargetType.LOCAL_VARIABLE;
                for (Attribute.Compound attribute : tree.sym.attributes_field) {
                    Attribute.TypeCompound tc =
                        new Attribute.TypeCompound(attribute.type, attribute.values, position);
                    recordedTypeAnnotations = recordedTypeAnnotations.append(tc);
                }
            }
            try {
                // copied from super.visitVarDef. need to skip tree.init
                scan(tree.mods);
                scan(tree.vartype);
                if (visitBodies)
                    scan(tree.init);

            } finally {
                if (kind.isField() || kind == ElementKind.LOCAL_VARIABLE || isCatchParameter)
                    tree.sym.typeAnnotations = appendUnique(tree.sym.typeAnnotations, recordedTypeAnnotations);
                recordedTypeAnnotations = kind.isField() ? prevTAs : prevTAs.appendList(recordedTypeAnnotations);
            }
        }

        @Override
        public void visitApply(JCMethodInvocation tree) {
            scan(tree.meth);
            scan(tree.typeargs);
            scan(tree.args);
        }

        public void visitAnnotation(JCAnnotation tree) {
            if (tree instanceof JCTypeAnnotation)
                recordedTypeAnnotations = recordedTypeAnnotations.append(((JCTypeAnnotation)tree).attribute_field);
            super.visitAnnotation(tree);
        }
    }

    private void separateAnnotationsKinds(Symbol sym, Type type, TypeAnnotationPosition pos) {
        List<Compound> annotations = sym.attributes_field;

        ListBuffer<Compound> declAnnos = new ListBuffer<Compound>();
        ListBuffer<TypeCompound> typeAnnos = new ListBuffer<TypeCompound>();

        for (Compound a : annotations) {
            switch (annotationType(a, sym)) {
            case DECLARATION:
                declAnnos.append(a);
                break;
            case BOTH: {
                declAnnos.append(a);
                TypeCompound ta = toTypeCompound(a, pos);
                typeAnnos.append(ta);
                break;
            }
            case TYPE: {
                TypeCompound ta = toTypeCompound(a, pos);
                typeAnnos.append(ta);
                break;
            }
            }
        }

        sym.attributes_field = declAnnos.toList();
        List<TypeCompound> typeAnnotations = typeAnnos.toList();
        Type atype = typeWithAnnotations(type, typeAnnotations);
        if (sym.getKind() == ElementKind.METHOD) {
            sym.type.asMethodType().restype = atype;
        } else
            sym.type = atype;

        sym.typeAnnotations = sym.typeAnnotations.appendList(typeAnnotations);
        if (sym.getKind() == ElementKind.PARAMETER
            || sym.getKind() == ElementKind.LOCAL_VARIABLE)
            sym.owner.typeAnnotations = sym.owner.typeAnnotations.appendList(typeAnnotations);
    }

    private Type typeWithAnnotations(Type type, List<TypeCompound> annotations) {
        if (type.tag != TypeTags.ARRAY) {
            Type atype = (Type)type.clone();
            atype.typeAnnotations = List.convert(Compound.class, annotations);
            return atype;
        } else {
            ArrayType arType = (ArrayType)type;
            int depth = 0;
            while (arType.elemtype.tag == TypeTags.ARRAY) {
                arType = (ArrayType)arType.elemtype;
                depth++;
            }
            arType.elemtype = typeWithAnnotations(arType.elemtype, annotations);
            for (TypeCompound a : annotations) {
                TypeAnnotationPosition p = a.position;
                p.location = p.location.append(depth);
                p.type = p.type.getGenericComplement();
            }
        }

        return type;
    }

    private TypeCompound toTypeCompound(Compound a, TypeAnnotationPosition p) {
        return new TypeCompound(a, p.clone());
    }

    private boolean areAllDecl(Symbol s) {
        for (Compound a : s.attributes_field) {
            if (annotationType(a, s) != AnnotationType.DECLARATION)
                return false;
        }

        return true;
    }

    AnnotationType annotationType(Compound a, Symbol s) {
        Attribute.Compound atTarget =
            a.type.tsym.attribute(syms.annotationTargetType.tsym);
        if (atTarget == null) return inferTargetMetaInfo(a, s);
        Attribute atValue = atTarget.member(names.value);
        if (!(atValue instanceof Attribute.Array)) return AnnotationType.DECLARATION; // error recovery
        Attribute.Array arr = (Attribute.Array) atValue;
        boolean isDecl = false, isType = false;
        for (Attribute app : arr.values) {
            if (!(app instanceof Attribute.Enum)) return AnnotationType.DECLARATION; // recovery
            Attribute.Enum e = (Attribute.Enum) app;
            if (e.value.name == names.TYPE)
                { if (s.kind == TYP) isDecl = true; }
            else if (e.value.name == names.FIELD)
                { if (s.kind == VAR && s.owner.kind != MTH) isDecl = true; }
            else if (e.value.name == names.METHOD)
                { if (s.kind == MTH && !s.isConstructor()) isDecl = true; }
            else if (e.value.name == names.PARAMETER)
                { if (s.kind == VAR &&
                      s.owner.kind == MTH &&
                      (s.flags() & PARAMETER) != 0)
                    isDecl = true;
                }
            else if (e.value.name == names.CONSTRUCTOR)
                { if (s.kind == MTH && s.isConstructor()) isDecl = true; }
            else if (e.value.name == names.LOCAL_VARIABLE)
                { if (s.kind == VAR && s.owner.kind == MTH &&
                      (s.flags() & PARAMETER) == 0)
                    isDecl = true;
                }
            else if (e.value.name == names.ANNOTATION_TYPE)
                { if (s.kind == TYP && (s.flags() & ANNOTATION) != 0)
                    isDecl = true;
                }
            else if (e.value.name == names.PACKAGE)
                { if (s.kind == PCK) isDecl = true; }
            else if (e.value.name == names.TYPE_USE)
                { if (s.kind == TYP ||
                      s.kind == VAR ||
                      (s.kind == MTH && !s.isConstructor() &&
                       s.type.getReturnType().tag != VOID))
                    isType = true;
                }
            else
                return AnnotationType.DECLARATION; // recovery
        }
        if (isDecl && isType)
            return AnnotationType.BOTH;
        else
            return isType ? AnnotationType.TYPE : AnnotationType.DECLARATION;
    }

    private AnnotationType inferTargetMetaInfo(Compound a, Symbol s) {
        if (s.kind == TYP || s.kind == VAR
            || (s.kind == MTH && !s.isConstructor() &&
                    s.type.getReturnType().tag != VOID))
            return AnnotationType.BOTH;
        else
            return AnnotationType.DECLARATION;
    }

}
