package com.sun.source.tree;

import javax.lang.model.element.Name;
import checkers.javari.quals.*;

public interface MemberSelectTree extends ExpressionTree {
    @PolyRead ExpressionTree getExpression() @PolyRead;
    @PolyRead Name getIdentifier() @PolyRead;
}
