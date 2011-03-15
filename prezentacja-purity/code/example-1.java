interface Iterator {
    boolean hasNext();
    Integer next();
}
class ListItr implements Iterator {
  ListItr(Cell head) {
    cell = head;
  }
  Cell cell;
  public boolean hasNext() {
    return cell != null;
  }
  public Integer next() {
    Integer result = cell.data;
    cell = cell.next;
  }
}
