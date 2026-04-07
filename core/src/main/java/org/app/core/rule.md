Code with generic type
Rule: PECS → Producer Extends, Consumer Super

Action read/get use extends(T or children)
```java
void printNumbers(List<? extends Number> list) {
  for (Number n : list) {
    System.out.println(n);
  }
}
```

Action write/set use super(T or parent)
```java
void addIntegers(List<? super Integer> list) {
  list.add(10);
}
```
