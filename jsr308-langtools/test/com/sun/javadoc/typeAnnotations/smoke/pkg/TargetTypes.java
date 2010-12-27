/*
 * Copyright 2010 Sun Microsystems, Inc.  All Rights Reserved.
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

package pkg;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.util.*;
import java.io.*;

/*
 * @summary compiler accepts all values
 * @author Mahmood Ali
 * @author Yuri Gaevsky
 */

@Target({TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@interface A {}

@Target({TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface DA {}

/** wildcard bound */
class T0x1C {
    void m0x1C(List<? extends @A @DA String> lst) {}
}

/** wildcard bound generic/array */
class T0x1D<T> {
    void m0x1D(List<? extends @A @DA List<int[]>> lst) {}
}

/** typecast */
class T0x00 {
    void m0x00(Long l1) {
        Object l2 = (@A @DA Long) l1;
    }
}

/** typecast generic/array */
class T0x01<T> {
    void m0x01(List<T> list) {
        List<T> l = (List<@A @DA T>) list;
    }
}

/** instanceof */
class T0x02 {
    boolean m0x02(String s) {
        return (s instanceof @A @DA String);
    }
}

/** object creation (new) */
class T0x04 {
    void m0x04() {
        new @A @DA ArrayList<String>();
    }
}

/** class literal */
class T0x1E {
    void m0x1E() {
        Class<Object> c = @A @DA Object.class;
    }
}

/** local variable */
class T0x08 {
    void m0x08() {
      @A @DA String s = null;
    }
}

/** method parameter generic/array */
class T0x0D {
    void m0x0D(HashMap<@A @DA Object, List<@A @DA List<@A @DA Class>>> s1) {}
}

/** method receiver */
class T0x06 {
    void m0x06() @A @DA {}
}

/** method return type generic/array */
class T0x0B {
    Class<@A @DA Object> m0x0B() { return null; }
}

/** field generic/array */
class T0x0F {
    HashMap<@A @DA Object, @A @DA Object> c1;
}

/** method type parameter */
class T0x20<T, U> {
    <@A @DA T, @A @DA U> void m0x20() {}
}

/** class type parameter */
class T0x22<@A @DA T, @A @DA U> {
}

/** class type parameter bound */
class T0x10<T extends @A @DA Cloneable> {
}

class T0x10A<T extends @A @DA Object> {
}
/** method type parameter bound */
class T0x12<T> {
    <T extends @A @DA Cloneable> void m0x12() {}
}

/** class type parameter bound generic/array */
class T0x11<T extends List<@A @DA T>> {
}


/** method type parameter bound generic/array */
class T0x13 {
    static <T extends Comparable<@A @DA T>> T m0x13() {
        return null;
    }
}

/** class extends/implements generic/array */
class T0x15<T> extends ArrayList<@A @DA T> {
}

/** type test (instanceof) generic/array */
class T0x03<T> {
    void m0x03(T typeObj, Object obj) {
        boolean ok = obj instanceof String @A @DA [];
    }
}

/** object creation (new) generic/array */
class T0x05<T> {
    void m0x05() {
        new ArrayList<@A @DA T>();
    }
}

/** local variable generic/array */
class T0x09<T> {
    void g() {
        List<@A @DA String> l = null;
    }

    void a() {
        String @A @DA [] as = null;
    }
}

/** type argument in constructor call generic/array */
class T0x19 {
    <T> T0x19() {}

    void g() {
       new <List<@A @DA String>> T0x19();
    }
}

/** type argument in method call generic/array */
class T0x1B<T> {
    void m0x1B() {
        Collections.<T @A @DA []>emptyList();
    }
}

/** type argument in constructor call */
class T0x18<T> {
    <T> T0x18() {}

    void m() {
        new <@A @DA Integer> T0x18();
    }
}

/** type argument in method call */
class T0x1A<T,U> {
    public static <T, U> T m() { return null; }
    static void m0x1A() {
        T0x1A.<@A @DA Integer, @A @DA Short>m();
    }
}

/** class extends/implements */
class T0x14 extends @A @DA Thread implements @A @DA Serializable, @A @DA Cloneable {
}

/** exception type in throws */
class T0x16 {
    void m0x16() throws @A @DA Exception {}
}
