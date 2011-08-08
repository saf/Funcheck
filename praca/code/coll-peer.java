public class PeerStack<T> {
    private static class Cell<CT> {
        private @Peer CT data;
        private @Immutable @Peer Cell next;
        @Anonymous
        public Cell(@Peer CT data, @Immutable @Peer Cell next) {
            this.data = data;
            this.next = next;
        }
        @ReadOnly public @Peer CT getData() { return data; }
        @ReadOnly public @Immutable @Peer Cell getNext() { return next; }
    }
    private @Immutable @Peer Cell<T> head;
    public PeerStack() { head = null; }
    public @Peer T pop() {
        if (head == null) return null;
        else {
            @Peer T result = head.getData();
            head = head.getNext();
            return result;
        }
    }
    public void push(@Peer T elem) {
        head = new @Immutable @Peer Cell(elem, head);
    }
}