package checkers.fun;

import checkers.fun.jimuva.JimuvaChecker;
import checkers.source.SourceChecker;
import checkers.util.AggregateChecker;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * The aggregate checker.
 * @author saf
 */
public class FunChecker extends AggregateChecker {

    @Override
    protected Collection<Class<? extends SourceChecker>> getSupportedCheckers() {
        List<Class<? extends SourceChecker>> checkers = new LinkedList<Class<? extends SourceChecker>>();
        checkers.add(JimuvaChecker.class);
        return checkers;
    }
}
