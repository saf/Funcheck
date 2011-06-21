public class RepStack {

    protected static class Cell {
        private Cell next;
        private @Rep @Myaccess Integer data;

        public Cell(Cell next, Integer data) {
            this.next = next;
            this.data = new @Rep Integer(data); 
        }

	/* Cannot return 'data' directly    */
	/* AND cannot copy to @World due to */ 
	/* encapsulation of representation! */
	public Integer getData() {   
              return new Integer(data);
        }                            

        public Cell getNext() {
            return next;
        }
    }

    protected Cell head;

    public RepStack() {
        head = null;
    }

    public void push(Integer n) {
        head = new Cell(head, n);
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