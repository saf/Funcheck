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
 * @summary Test population of reference info for method parameters
 * @compile -g Driver.java ReferenceInfoUtil.java MethodParameters.java
 * @run main Driver MethodParameters
 */
public class MethodParameters {

    // Method returns
    @TADescription(annotation = "TA", type = METHOD_PARAMETER, paramIndex = 0)
    public String methodParamAsPrimitive() {
        return "void test(@TA int a) { }";
    }

    @TADescription(annotation = "TA", type = METHOD_PARAMETER, paramIndex = 1)
    public String methodParamAsObject() {
        return "void test(Object b, @TA Object a) { }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = METHOD_PARAMETER, paramIndex = 0),
        @TADescription(annotation = "TB", type = METHOD_PARAMETER_GENERIC_OR_ARRAY,
                genericLocation = { 0 }, paramIndex = 0),
        @TADescription(annotation = "TC", type = METHOD_PARAMETER_GENERIC_OR_ARRAY,
                genericLocation = { 1 }, paramIndex = 0),
        @TADescription(annotation = "TD", type = METHOD_PARAMETER_GENERIC_OR_ARRAY,
                genericLocation = { 1, 0 }, paramIndex = 0)
    })
    public String methodParamAsParametrized() {
        return "void test(@TA Map<@TB String, @TC List<@TD String>> a) { }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = METHOD_PARAMETER, paramIndex = 1),
        @TADescription(annotation = "TB", type = METHOD_PARAMETER_GENERIC_OR_ARRAY,
                genericLocation = { 0 }, paramIndex = 1),
        @TADescription(annotation = "TC", type = METHOD_PARAMETER_GENERIC_OR_ARRAY,
                genericLocation = { 1 }, paramIndex = 1)
    })
    public String methodParamAsArray() {
        return "void test(Object b, @TC String @TA [] @TB [] a) { }";
    }

    @TADescriptions({})
    public String methodWithDeclarationAnnotatin() {
        return "void test(@Decl String a) { }";
    }

    @TADescription(annotation = "A", type = METHOD_PARAMETER, paramIndex = 0)
    public String methodWithNoTargetAnno() {
        return "void test(@A String a) { }";
    }

    // Smoke tests
    @TADescription(annotation = "TA", type = METHOD_PARAMETER, paramIndex = 0)
    public String interfacemethodParamAsObject() {
        return "interface Test { void test(@TA Object a); }";
    }

    @TADescription(annotation = "TA", type = METHOD_PARAMETER, paramIndex = 2)
    public String abstractmethodParamAsObject() {
        return "abstract class Test { abstract void test(Object b, Object c, @TA Object a); }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = METHOD_PARAMETER, paramIndex = 0),
        @TADescription(annotation = "TB", type = METHOD_PARAMETER_GENERIC_OR_ARRAY,
                genericLocation = { 0 }, paramIndex = 0),
        @TADescription(annotation = "TC", type = METHOD_PARAMETER_GENERIC_OR_ARRAY,
                genericLocation = { 1 }, paramIndex = 0),
        @TADescription(annotation = "TD", type = METHOD_PARAMETER_GENERIC_OR_ARRAY,
                genericLocation = { 1, 0 }, paramIndex = 0)
    })
    public String interfacemethodParamAsParametrized() {
        return "interface Test { void test(@TA Map<@TB String, @TC List<@TD String>> a); }";
    }

}
