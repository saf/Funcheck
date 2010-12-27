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
 * @summary Test population of reference info for class type parameters
 * @compile -g Driver.java ReferenceInfoUtil.java ClassTypeParam.java
 * @run main Driver ClassTypeParam
 */
public class ClassTypeParam {

    @TADescriptions({
        @TADescription(annotation = "A", type = CLASS_TYPE_PARAMETER, paramIndex = 0),
        @TADescription(annotation = "TB", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 0, boundIndex = 0),
        @TADescription(annotation = "C", type = CLASS_TYPE_PARAMETER, paramIndex = 1),
        @TADescription(annotation = "TD", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 1, boundIndex = 0),
        @TADescription(annotation = "TE", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 1, boundIndex = 1)
    })
    public String regularClass() {
        return "class Test<@A K extends @TB Date, @C V extends @TD Object& @TE Cloneable> { }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 0, boundIndex = 0),
        @TADescription(annotation = "TB", type = CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY, paramIndex = 0, boundIndex = 0, genericLocation = {1}),
        @TADescription(annotation = "TC", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 1, boundIndex = 1),
        @TADescription(annotation = "TD", type = CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY, paramIndex = 1, boundIndex = 1, genericLocation = {0}),
        @TADescription(annotation = "TE", type = CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY, paramIndex = 1, boundIndex = 1, genericLocation = {0, 0}),
        @TADescription(annotation = "TF", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 1, boundIndex = 0)
    })
    public String regularClassParameterized() {
        return "class Test<K extends @TA Map<String, @TB String>, V extends @TF Object& @TC List<@TD List<@TE Object>>> { }";
    }

    @TADescriptions({
        @TADescription(annotation = "A", type = CLASS_TYPE_PARAMETER, paramIndex = 0),
        @TADescription(annotation = "TB", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 0, boundIndex = 0),
        @TADescription(annotation = "C", type = CLASS_TYPE_PARAMETER, paramIndex = 1),
        @TADescription(annotation = "TD", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 1, boundIndex = 0),
        @TADescription(annotation = "TE", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 1, boundIndex = 1)
    })
    public String abstractClass() {
        return "abstract class Test<@A K extends @TB Date, @C V extends @TD Object& @TE Cloneable> { }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 0, boundIndex = 0),
        @TADescription(annotation = "TB", type = CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY, paramIndex = 0, boundIndex = 0, genericLocation = {1}),
        @TADescription(annotation = "TC", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 1, boundIndex = 1),
        @TADescription(annotation = "TD", type = CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY, paramIndex = 1, boundIndex = 1, genericLocation = {0}),
        @TADescription(annotation = "TE", type = CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY, paramIndex = 1, boundIndex = 1, genericLocation = {0, 0}),
        @TADescription(annotation = "TF", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 1, boundIndex = 0)
    })
    public String abstractClassParameterized() {
        return "abstract class Test<K extends @TA Map<String, @TB String>, V extends @TF Object& @TC List<@TD List<@TE Object>>> { }";
    }

    @TADescriptions({
        @TADescription(annotation = "A", type = CLASS_TYPE_PARAMETER, paramIndex = 0),
        @TADescription(annotation = "TB", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 0, boundIndex = 0),
        @TADescription(annotation = "C", type = CLASS_TYPE_PARAMETER, paramIndex = 1),
        @TADescription(annotation = "TD", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 1, boundIndex = 0),
        @TADescription(annotation = "TE", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 1, boundIndex = 1)
    })
    public String regularInterface() {
        return "interface Test<@A K extends @TB Date, @C V extends @TD Object& @TE Cloneable> { }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 0, boundIndex = 0),
        @TADescription(annotation = "TB", type = CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY, paramIndex = 0, boundIndex = 0, genericLocation = {1}),
        @TADescription(annotation = "TC", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 1, boundIndex = 1),
        @TADescription(annotation = "TD", type = CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY, paramIndex = 1, boundIndex = 1, genericLocation = {0}),
        @TADescription(annotation = "TE", type = CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY, paramIndex = 1, boundIndex = 1, genericLocation = {0, 0}),
        @TADescription(annotation = "TF", type = CLASS_TYPE_PARAMETER_BOUND, paramIndex = 1, boundIndex = 0)
    })
    public String regularInterfaceParameterized() {
        return "interface Test<K extends @TA Map<String, @TB String>, V extends @TF Object& @TC List<@TD List<@TE Object>>> { }";
    }
}
