static Integer getSum(List list) {
  Integer s = 0;
  Iterator it = list.iterator();
  while(it.hasNext()) {
      Integer n = it.next();
      s += n;
  }
  return s;
}
