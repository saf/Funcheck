class List {  
  Cell head = null;
  void add(Integer e) {
    head = new Cell(e, head);
  }
  Iterator iterator() {
    return new ListItr(head);
  }
}
class Cell {
  Cell(Integer d, Cell n) {
    data = d; next = n;
  }
  Integer data;
  Cell    next;
}

