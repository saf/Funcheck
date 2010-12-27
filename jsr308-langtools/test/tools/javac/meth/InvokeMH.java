/*
 * Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 6754038
 * @summary Generate call sites for method handle
 * @author jrose
 *
 * @compile -source 7 -target 7 InvokeMH.java
 */

/*
 * Standalone testing:
 * <code>
 * $ cd $MY_REPO_DIR/langtools
 * $ (cd make; make)
 * $ ./dist/bootstrap/bin/javac -d dist test/tools/javac/meth/InvokeMH.java
 * $ javap -c -classpath dist meth.InvokeMH
 * </code>
 */

package meth;

import java.dyn.MethodHandle;

public class InvokeMH {
    void test(MethodHandle mh_SiO,
              MethodHandle mh_vS,
              MethodHandle mh_vi,
              MethodHandle mh_vv) throws Throwable {
        Object o; String s; int i;  // for return type testing

        // next five must have sig = (String,int)Object
        mh_SiO.invokeExact("world", 123);
        mh_SiO.invokeExact("mundus", 456);
        Object k = "kosmos";
        mh_SiO.invokeExact((String)k, 789);
        o = mh_SiO.invokeExact((String)null, 000);
        o = mh_SiO.<Object>invokeExact("arda", -123);

        // sig = ()String
        s = mh_vS.<String>invokeExact();

        // sig = ()int
        i = mh_vi.<int>invokeExact();
        o = mh_vi.<int>invokeExact();
        //s = mh_vi.<int>invokeExact(); //BAD
        mh_vi.<int>invokeExact();

        // sig = ()void
        //o = mh_vv.<void>invokeExact(); //BAD
        mh_vv.<void>invokeExact();
    }

    void testGen(MethodHandle mh_SiO,
                 MethodHandle mh_vS,
                 MethodHandle mh_vi,
                 MethodHandle mh_vv) throws Throwable {
        Object o; String s; int i;  // for return type testing

        // next five must have sig = (*,*)*
        mh_SiO.invokeGeneric((Object)"world", (Object)123);
        mh_SiO.<void>invokeGeneric((Object)"mundus", (Object)456);
        Object k = "kosmos";
        mh_SiO.invokeGeneric(k, 789);
        o = mh_SiO.invokeGeneric(null, 000);
        o = mh_SiO.<Object>invokeGeneric("arda", -123);

        // sig = ()String
        o = mh_vS.invokeGeneric();

        // sig = ()int
        i = mh_vi.<int>invokeGeneric();
        o = mh_vi.invokeGeneric();
        //s = mh_vi.<int>invokeGeneric(); //BAD
        mh_vi.<void>invokeGeneric();

        // sig = ()void
        //o = mh_vv.<void>invokeGeneric(); //BAD
        o = mh_vv.invokeGeneric();
    }
}
