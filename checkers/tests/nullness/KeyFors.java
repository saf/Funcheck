import checkers.nullness.quals.*;

import java.util.*;

public class KeyFors {

    public void withoutKeyFor() {
        Map<String, String> map = new HashMap<String, String>();
        String key = "key";

        //:: (assignment.type.incompatible)
        @NonNull String value = map.get(key);
    }

    public void withKeyFor() {
        Map<String, String> map = new HashMap<String, String>();
        @KeyFor("map") String key = "key";

        @NonNull String value = map.get(key);
    }

    public void withCollection() {
        Map<String, String> map = new HashMap<String, String>();
        List<@KeyFor("map") String> keys = new ArrayList<@KeyFor("map") String>();

        @NonNull String value = map.get(keys.get(0));
    }

    public void withIndirectReference() {
        class Container {
            Map<String, String> map = new HashMap<String, String>();
        }

        Container container = new Container();
        @KeyFor("container.map") String key = "m";

        @NonNull String value = container.map.get(key);
    }

    // Should this be '@KeyFor("#0")', or '@KeyFor("m")'?
    public static
    <K extends Comparable<? super K>,V> Collection<@KeyFor("#0") K>
    sortedKeySet(Map<K,V> m) {
        throw new RuntimeException();
    }

    public void testForLoop(HashMap<String,String> lastMap) {
        // TODO: support Flow for KeyFor
        for (@KeyFor("lastMap") String key : sortedKeySet(lastMap)) {
            @NonNull String al = lastMap.get(key);
        }
    }


  public class Graph<T> {

    HashMap<T, List<@KeyFor("childMap") T>> childMap;

    public void addNode( T n ) {
      // body omitted, not relevant to test case
    }

    public void addEdge2( T parent, T child ) {
      addNode(parent);
      @KeyFor("childMap") T parent2 = (@KeyFor("childMap") T) parent;
      @NonNull List<T> l = childMap.get(parent2);
    }

    // This is a feature request to have KeyFor can be inferred
//    public void addEdge3( T parent, T child ) {
//      addNode(parent);
//      parent = (@KeyFor("childMap") T) parent;
//      @NonNull List<T> l = childMap.get(parent);
//    }

  }

}
