/*
 * @test /nodynamiccopyright/
 * @bug 6843077 6919944
 * @summary check for duplicate annotation values for type parameter
 * @author Mahmood Ali
 * @compile/fail/ref=DuplicateAnnotationValue.out -XDrawDiagnostics -source 1.7 DuplicateAnnotationValue.java
 */
class DuplicateAnnotationValue<@A(value = 2, value = 1) K> {
}

@interface A { int value(); }
