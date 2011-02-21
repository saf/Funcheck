/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.func;

import com.sun.source.tree.Tree;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates and stores a mapping between node keys, which are strings representing
 * Java instructions that are passed to Prolog, and the AST nodes.
 *
 * @author saf
 */
public class NodeKey {

    private Map<String, Tree> mapping;
    private Integer index;

    public NodeKey() {
        mapping = new HashMap<String, Tree>();
        index = 0;
    }

    public String add(Tree el) {
        String key = Integer.toString(index);
        mapping.put(key, el);
        index++;
        return key;
    }

    public Tree get(String s) {
        return mapping.get(s);
    }

}
