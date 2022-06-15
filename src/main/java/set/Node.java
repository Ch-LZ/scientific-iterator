package set;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class Node<T> {

  private final T value;
  /* mark is related to node itself, but reference next points to the next node */
  private final AtomicMarkableReference<Node<T>> nextAndMark;


  public Node(T value, AtomicMarkableReference<Node<T>> nextMarked) {
    this.value = value;
    this.nextAndMark = nextMarked;
  }

  public Node(T value) {
    this.value = value;
    this.nextAndMark = new AtomicMarkableReference<>(null, false);
  }

  public T getValue() {
    return this.value;
  }

  public AtomicMarkableReference<Node<T>> getNextAndMark() {
    return nextAndMark;
  }
}
