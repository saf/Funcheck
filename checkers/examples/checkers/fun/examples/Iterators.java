/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.fun.examples;

import checkers.fun.quals.Immutable;
import checkers.fun.quals.OwnedBy;
import checkers.fun.quals.Peer;
import checkers.fun.quals.ReadOnly;
import checkers.fun.quals.Rep;

/**
 * Type-checked implementation for iterators over a stack whose nodes
 * are encapsulated inside the stack.
 *
 * @author saf
 */
public class Iterators {

    public static class Node<NT> {
        private @Peer Node<NT> next;
        private NT data;

        public Node(@Peer Node<NT> n, NT d) {
            next = n;
            data = d;
        }

        @ReadOnly
        @Peer public Node<NT> getNext() {
            return next;
        }

        @ReadOnly
        public NT getData() {
            return data;
        }
    }

    public static class StackIterator<ST> {

        @Immutable Node<ST> node;

        public StackIterator(@Immutable Node<ST> node) {
            this.node = node;
        }

        Boolean hasNext() {
            return node != null;
        }

        ST next() {
            ST result = node.getData();
            node = node.getNext();
            return result;
        }

    }

    public static class Stack<T> {
        @Rep Node<T> head;

        public Stack() {
            head = null;
        }

        void push(T data) {
            @Rep Node<T> newHead = new /*@Rep*/ Node(head, data);
            head = newHead;
        }

        @ReadOnly
        Boolean isEmpty() {
            return head == null;
        }

        T pop() {
            if (isEmpty()) {
                return null;
            } else {
                T result = head.getData();
                head = head.getNext();
                return result;
            }
        }

        @ReadOnly T peek() {
            if (isEmpty()) {
                return null;
            } else {
                return head.getData();
            }
        }

        @ReadOnly
        StackIterator<T> iterator() {
            return new StackIterator<T>(head);
        }
    }

    public static void main(String [] args) {
        Stack<Integer> s = new Stack<Integer>();
        for (int i = 1; i <= 5; i++) {
            s.push(i);
        }

        StackIterator<Integer> it = s.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }
}
