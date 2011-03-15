@Pure
public Integer getSum(List<Integer> l) {
    Integer s = 0;
    Iterator<Integer> it = list.iterator();
    while (it.hasNext()) {
	s += it.next();
    }
    return s;
}
