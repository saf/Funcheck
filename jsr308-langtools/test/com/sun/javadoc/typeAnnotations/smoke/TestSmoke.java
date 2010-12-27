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

/*
 * @test
 * @summary  Smoke test for ensuring that annotations are emited to javadoc
 *
 * @author   Mahmood Ali <mali>
 * @library  ../../lib/
 * @build    JavadocTester
 * @build    TestSmoke
 * @run main TestSmoke
 */

public class TestSmoke extends JavadocTester {

    //Test information.
    private static final String BUG_ID = "NOT_SPECIFIED_YET";

    //Javadoc arguments.
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-private", "-sourcepath", SRC_DIR, "pkg"
    };

    //Input for string search tests.
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "T0x1C.html", "@DA"},
        {BUG_ID + FS + "pkg" + FS + "T0x1D.html", "@DA"},
        {BUG_ID + FS + "pkg" + FS + "T0x0D.html", "@DA"},
        {BUG_ID + FS + "pkg" + FS + "T0x06.html", "@DA"},
        {BUG_ID + FS + "pkg" + FS + "T0x0B.html", "@DA"},
        {BUG_ID + FS + "pkg" + FS + "T0x0F.html", "@DA"},
        {BUG_ID + FS + "pkg" + FS + "T0x20.html", "@DA"},
        {BUG_ID + FS + "pkg" + FS + "T0x22.html", "@DA"},
        {BUG_ID + FS + "pkg" + FS + "T0x10.html", "@DA"},
        {BUG_ID + FS + "pkg" + FS + "T0x10A.html", "@DA"},
        {BUG_ID + FS + "pkg" + FS + "T0x12.html", "@DA"},
        {BUG_ID + FS + "pkg" + FS + "T0x11.html", "@DA"},
        {BUG_ID + FS + "pkg" + FS + "T0x13.html", "@DA"},
        {BUG_ID + FS + "pkg" + FS + "T0x15.html", "@DA"},
        {BUG_ID + FS + "pkg" + FS + "T0x14.html", "@DA"},
        {BUG_ID + FS + "pkg" + FS + "T0x16.html", "@DA"}
    };

    private static final String[][] NEGATED_TEST = {
        {BUG_ID + FS + "pkg" + FS + "T0x1C.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x1D.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x00.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x01.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x02.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x04.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x1E.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x08.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x0D.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x06.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x0B.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x0F.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x20.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x22.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x10.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x10A.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x12.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x11.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x13.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x15.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x14.html", "@A"},
        {BUG_ID + FS + "pkg" + FS + "T0x16.html", "@A"}
    };

    /**
     * The entry point of the test.
     * @param args the array of command line arguments.
     */
    public static void main(String[] args) {
        TestSmoke tester = new TestSmoke();
        run(tester, ARGS, TEST, NEGATED_TEST);
        tester.printSummary();
    }

    /**
     * {@inheritDoc}
     */
    public String getBugId() {
        return BUG_ID;
    }

    /**
     * {@inheritDoc}
     */
    public String getBugName() {
        return getClass().getName();
    }
}
