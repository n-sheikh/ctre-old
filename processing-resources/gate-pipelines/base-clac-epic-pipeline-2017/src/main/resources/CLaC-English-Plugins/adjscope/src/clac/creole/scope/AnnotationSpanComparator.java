package clac.creole.scope;

import java.util.Comparator;
import java.util.List;
import gate.Annotation;

/** Comparator used to order annotations by span.
 * a1 &gt; a2 if the span of a1 is strictly larger than the span of a2
 * or if the ID of a2 is in a1's consists list.
 * */
public class AnnotationSpanComparator
        implements Comparator<Annotation> {
    @Override
    public int compare(Annotation a1, Annotation a2) {
        List<Integer> a1consists = (List<Integer>) a1.getFeatures().get("consists");
        List<Integer> a2consists = (List<Integer>) a2.getFeatures().get("consists");
        // A1 dominates
        if ( span(a1) > span(a2) ||
                ( a1consists != null && a1consists.contains(a2.getId()) ) ) {
            return 1;
        }
        // A2 dominates
        if ( span(a2) > span(a1) ||
                ( a2consists != null && a2consists.contains(a1.getId()) ) ) {
            return -1;
        }
        // Neither nodes dominate each other
        // (This should not happen if nodes are part of the same tree)
        return 0;
    }

    private long span(Annotation a) {
        return a.getEndNode().getOffset()
             - a.getStartNode().getOffset();
    }
}
