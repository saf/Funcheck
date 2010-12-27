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

import static com.sun.tools.classfile.ExtendedAnnotation.TargetType.*;

/*
 * @test
 * @summary Test population of reference info for method return
 * @compile -g Driver.java ReferenceInfoUtil.java MethodReturns.java
 * @run main Driver MethodReturns
 */
public class MethodReturns {

    // Method returns
    @TADescription(annotation = "TA", type = METHOD_RETURN)
    public String methodReturnAsPrimitive() {
        return "@TA int test() { return 0; }";
    }

    @TADescription(annotation = "TA", type = METHOD_RETURN)
    public String methodReturnAsObject() {
        return "@TA Object test() { return null; }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = METHOD_RETURN),
        @TADescription(annotation = "TB", type = METHOD_RETURN_GENERIC_OR_ARRAY,
                genericLocation = { 0 }),
        @TADescription(annotation = "TC", type = METHOD_RETURN_GENERIC_OR_ARRAY,
                genericLocation = { 1 }),
        @TADescription(annotation = "TD", type = METHOD_RETURN_GENERIC_OR_ARRAY,
                genericLocation = { 1, 0 })
    })
    public String methodReturnAsParametrized() {
        return "@TA Map<@TB String, @TC List<@TD String>> test() { return null; }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = METHOD_RETURN),
        @TADescription(annotation = "TB", type = METHOD_RETURN_GENERIC_OR_ARRAY,
                genericLocation = { 0 }),
        @TADescription(annotation = "TC", type = METHOD_RETURN_GENERIC_OR_ARRAY,
                genericLocation = { 1 })
    })
    public String methodReturnAsArray() {
        return "@TC String @TA [] @TB [] test() { return null; }";
    }

    @TADescriptions({})
    public String methodWithDeclarationAnnotatin() {
        return "@Decl String test() { return null; }";
    }

    @TADescription(annotation = "A", type = METHOD_RETURN)
    public String methodWithNoTargetAnno() {
        return "@A String test() { return null; }";
    }

    // Smoke tests
    @TADescription(annotation = "TA", type = METHOD_RETURN)
    public String interfaceMethodReturnAsObject() {
        return "interface Test { @TA Object test(); }";
    }

    @TADescription(annotation = "TA", type = METHOD_RETURN)
    public String abstractMethodReturnAsObject() {
        return "abstract class Test { abstract @TA Object test(); }";
    }


    @TADescriptions({
        @TADescription(annotation = "TA", type = METHOD_RETURN),
        @TADescription(annotation = "TB", type = METHOD_RETURN_GENERIC_OR_ARRAY,
                genericLocation = { 0 }),
        @TADescription(annotation = "TC", type = METHOD_RETURN_GENERIC_OR_ARRAY,
                genericLocation = { 1 }),
        @TADescription(annotation = "TD", type = METHOD_RETURN_GENERIC_OR_ARRAY,
                genericLocation = { 1, 0 })
    })
    public String interfaceMethodReturnAsParametrized() {
        return "interface Test { @TA Map<@TB String, @TC List<@TD String>> test(); }";
    }

}
