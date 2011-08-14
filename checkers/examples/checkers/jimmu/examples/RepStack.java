/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.jimmu.examples;

import checkers.jimmu.quals.*;

/**
 * Trying to define a collection of protected elements using @Rep only.
 */
public class RepStack {

    protected static class Cell {
        private Cell next;
        private @Rep @Myaccess Integer data;

        public Cell(Cell next, Integer data) {
            this.next = next;
            this.data = new /*@Rep*/ Integer(data); 
        }

        public Integer getData() {     /* Cannot return 'data' directly    */
            return new Integer(data); /* AND cannot copy to @World due to */
        }                            /* encapsulation of representation! */

        public Cell getNext() {
            return next;
        }
    }

    protected Cell head;

    public RepStack() {
        head = null;
    }

    public void push(Integer n) {
        head = new Cell(null, n);
    }

    public Integer pop(Integer n) {
        if (head == null) return null;
        else {
            Integer result = head.getData();
            head = head.getNext();
            return result;
        }
    }
}