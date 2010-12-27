/*
 * Copyright 2009 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sun.tools.classfile.Attribute;
import com.sun.tools.classfile.ClassFile;
import com.sun.tools.classfile.ExtendedAnnotation;
import com.sun.tools.classfile.Field;
import com.sun.tools.classfile.Method;
import com.sun.tools.classfile.RuntimeTypeAnnotations_attribute;
import com.sun.tools.classfile.ConstantPool.InvalidIndex;
import com.sun.tools.classfile.ConstantPool.UnexpectedEntry;

public class ReferenceInfoUtil {

    public static final int IGNORE_VALUE = -321;

    public static List<ExtendedAnnotation> extendedAnnotationsOf(ClassFile cf) {
        List<ExtendedAnnotation> annos = new ArrayList<ExtendedAnnotation>();
        findAnnotations(cf, annos);
        return annos;
    }

    /////////////////// Extract type annotations //////////////////
    private static void findAnnotations(ClassFile cf, List<ExtendedAnnotation> annos) {
        findAnnotations(cf, Attribute.RuntimeVisibleTypeAnnotations, annos);
        findAnnotations(cf, Attribute.RuntimeInvisibleTypeAnnotations, annos);

        for (Field f : cf.fields) {
            findAnnotations(cf, f, annos);
        }
        for (Method m: cf.methods) {
            findAnnotations(cf, m, annos);
        }
    }

    private static void findAnnotations(ClassFile cf, Method m, List<ExtendedAnnotation> annos) {
        findAnnotations(cf, m, Attribute.RuntimeVisibleTypeAnnotations, annos);
        findAnnotations(cf, m, Attribute.RuntimeInvisibleTypeAnnotations, annos);
    }

    private static void findAnnotations(ClassFile cf, Field m, List<ExtendedAnnotation> annos) {
        findAnnotations(cf, m, Attribute.RuntimeVisibleTypeAnnotations, annos);
        findAnnotations(cf, m, Attribute.RuntimeInvisibleTypeAnnotations, annos);
    }

    // test the result of Attributes.getIndex according to expectations
    // encoded in the method's name
    private static void findAnnotations(ClassFile cf, String name, List<ExtendedAnnotation> annos) {
        int index = cf.attributes.getIndex(cf.constant_pool, name);
        if (index != -1) {
            Attribute attr = cf.attributes.get(index);
            assert attr instanceof RuntimeTypeAnnotations_attribute;
            RuntimeTypeAnnotations_attribute tAttr = (RuntimeTypeAnnotations_attribute)attr;
            annos.addAll(Arrays.asList(tAttr.annotations));
        }
    }

    // test the result of Attributes.getIndex according to expectations
    // encoded in the method's name
    private static void findAnnotations(ClassFile cf, Method m, String name, List<ExtendedAnnotation> annos) {
        int index = m.attributes.getIndex(cf.constant_pool, name);
        if (index != -1) {
            Attribute attr = m.attributes.get(index);
            assert attr instanceof RuntimeTypeAnnotations_attribute;
            RuntimeTypeAnnotations_attribute tAttr = (RuntimeTypeAnnotations_attribute)attr;
            annos.addAll(Arrays.asList(tAttr.annotations));
        }
    }

    // test the result of Attributes.getIndex according to expectations
    // encoded in the method's name
    private static void findAnnotations(ClassFile cf, Field m, String name, List<ExtendedAnnotation> annos) {
        int index = m.attributes.getIndex(cf.constant_pool, name);
        if (index != -1) {
            Attribute attr = m.attributes.get(index);
            assert attr instanceof RuntimeTypeAnnotations_attribute;
            RuntimeTypeAnnotations_attribute tAttr = (RuntimeTypeAnnotations_attribute)attr;
            annos.addAll(Arrays.asList(tAttr.annotations));
        }
    }

    /////////////////// TA Position Builder ///////////////////////
    public static class TAPositionBuilder {
        private ExtendedAnnotation.Position pos = new ExtendedAnnotation.Position();

        private TAPositionBuilder() { }

        public ExtendedAnnotation.Position build() { return pos; }

        public static TAPositionBuilder ofType(ExtendedAnnotation.TargetType type) {
            TAPositionBuilder builder = new TAPositionBuilder();
            builder.pos.type = type;
            return builder;
        }

        public TAPositionBuilder atOffset(int offset) {
            switch (pos.type) {
            // type case
            case TYPECAST:
            case TYPECAST_GENERIC_OR_ARRAY:
                // object creation
            case INSTANCEOF:
            case INSTANCEOF_GENERIC_OR_ARRAY:
                // new expression
            case NEW:
            case NEW_GENERIC_OR_ARRAY:
                // class literals
            case CLASS_LITERAL:
            case CLASS_LITERAL_GENERIC_OR_ARRAY:
                pos.offset = offset;
                break;
            default:
                throw new IllegalArgumentException("invalid field for given type: " + pos.type);
            }
            return this;
        }

        public TAPositionBuilder atLocalPosition(int offset, int length, int index) {
            switch (pos.type) {
            // local variable
            case LOCAL_VARIABLE:
            case LOCAL_VARIABLE_GENERIC_OR_ARRAY:
                pos.lvarOffset = new int[] { offset };
                pos.lvarLength = new int[] { length };
                pos.lvarIndex  = new int[] { index  };
                break;
            default:
                throw new IllegalArgumentException("invalid field for given type: " + pos.type);
            }
            return this;
        }

        public TAPositionBuilder atParameterIndex(int index) {
            switch (pos.type) {
            // type parameters
            case CLASS_TYPE_PARAMETER:
            case METHOD_TYPE_PARAMETER:
                // method parameter
            case METHOD_PARAMETER:
            case METHOD_PARAMETER_GENERIC_OR_ARRAY:
                pos.parameter_index = index;
                break;
            default:
                throw new IllegalArgumentException("invalid field for given type: " + pos.type);
            }
            return this;
        }

        public TAPositionBuilder atParamBound(int param, int bound) {
            switch (pos.type) {
            // type parameters bounds
            case CLASS_TYPE_PARAMETER_BOUND:
            case CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY:
            case METHOD_TYPE_PARAMETER_BOUND:
            case METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY:
                pos.parameter_index = param;
                pos.bound_index = bound;
                break;
            default:
                throw new IllegalArgumentException("invalid field for given type: " + pos.type);
            }
            return this;
        }

        public TAPositionBuilder atWildcardPosition(ExtendedAnnotation.Position pos) {
            switch (pos.type) {
            // wildcards
            case WILDCARD_BOUND:
            case WILDCARD_BOUND_GENERIC_OR_ARRAY:
                pos.wildcard_position = pos;
                break;
            default:
                throw new IllegalArgumentException("invalid field for given type: " + pos.type);
            }
            return this;
        }

        public TAPositionBuilder atTypeIndex(int index) {
            switch (pos.type) {
            // Class extends and implements clauses
            case CLASS_EXTENDS:
            case CLASS_EXTENDS_GENERIC_OR_ARRAY:
                // throws
            case THROWS:
                pos.type_index = index;
                break;
            default:
                throw new IllegalArgumentException("invalid field for given type: " + pos.type);
            }
            return this;
        }

        public TAPositionBuilder atOffsetWithIndex(int offset, int index) {
            switch (pos.type) {
            // method type argument: wasn't specified
            case NEW_TYPE_ARGUMENT:
            case NEW_TYPE_ARGUMENT_GENERIC_OR_ARRAY:
            case METHOD_TYPE_ARGUMENT:
            case METHOD_TYPE_ARGUMENT_GENERIC_OR_ARRAY:
                pos.offset = offset;
                pos.type_index = index;
                break;
            default:
                throw new IllegalArgumentException("invalid field for given type: " + pos.type);
            }
            return this;
        }

        public TAPositionBuilder atGenericLocation(Integer ...loc) {
            pos.location = Arrays.asList(loc);
            pos.type = pos.type.getGenericComplement();
            return this;
        }
    }

    /////////////////////// Equality testing /////////////////////
    private static boolean areEquals(int a, int b) {
        return a == b || a == IGNORE_VALUE || b == IGNORE_VALUE;
    }

    private static boolean areEquals(int[] a, int[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i] && a[i] != IGNORE_VALUE && a2[i] != IGNORE_VALUE)
                return false;

        return true;
    }

    public static boolean areEquals(ExtendedAnnotation.Position p1, ExtendedAnnotation.Position p2) {
        if (p1 == p2)
            return true;
        if (p1 == null || p2 == null)
            return false;

        return ((p1.type == p2.type)
                && (p1.location.equals(p2.location))
                && areEquals(p1.offset, p2.offset)
                && areEquals(p1.lvarOffset, p2.lvarOffset)
                && areEquals(p1.lvarLength, p2.lvarLength)
                && areEquals(p1.lvarIndex, p2.lvarIndex)
                && areEquals(p1.bound_index, p2.bound_index)
                && areEquals(p1.parameter_index, p2.parameter_index)
                && areEquals(p1.type_index, p2.type_index)
                && areEquals(p1.wildcard_position, p2.wildcard_position));
    }

    private static ExtendedAnnotation findAnnotation(String name, List<ExtendedAnnotation> annotations, ClassFile cf) throws InvalidIndex, UnexpectedEntry {
        String properName = "L" + name + ";";
        for (ExtendedAnnotation anno : annotations) {
            String actualName = cf.constant_pool.getUTF8Value(anno.annotation.type_index);
            if (properName.equals(actualName))
                return anno;
        }
        return null;
    }

    public static boolean compare(Map<String, ExtendedAnnotation.Position> expectedAnnos,
            List<ExtendedAnnotation> actualAnnos, ClassFile cf) throws InvalidIndex, UnexpectedEntry {
        if (actualAnnos.size() != expectedAnnos.size()) {
            throw new ComparisionException("Wrong number of annotations",
                    expectedAnnos.size() + " annotations",
                    actualAnnos.size() + " annotations");
        }

        for (Map.Entry<String, ExtendedAnnotation.Position> e : expectedAnnos.entrySet()) {
            String aName = e.getKey();
            ExtendedAnnotation.Position expected = e.getValue();
            ExtendedAnnotation actual = findAnnotation(aName, actualAnnos, cf);
            if (actual == null)
                throw new ComparisionException("Expected annotation not found: " + aName);

            if (!areEquals(expected, actual.position)) {
                throw new ComparisionException("Unexpected position for annotation : " + aName,
                        expected.toString(), actual.position.toString());
            }
        }
        return true;
    }
}

class ComparisionException extends RuntimeException {
    private static final long serialVersionUID = -3930499712333815821L;

    public final String expected;
    public final String found;

    public ComparisionException(String message) {
        this(message, null, null);
    }

    public ComparisionException(String message, String expected, String found) {
        super(message);
        this.expected = expected;
        this.found = found;
    }

    public String toString() {
        String str = super.toString();
        if (expected != null && found != null) {
            str += "\n\tExpected: " + expected + "; but found: " + found;
        }
        return str;
    }
}
