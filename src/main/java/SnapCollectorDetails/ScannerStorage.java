package SnapCollectorDetails;

import utils.Node;

import java.util.Iterator;

/**
 * Хранит отсканированные узлы. Отвечает за wait-free сканирование множества.
 * Вся содержательная логика в родительском классе.
 */
public class ScannerStorage<M extends Comparable<M>> extends GenericStorage<Node<M>> implements Iterable<Node<M>> {

  /**
   * Concurrent set implementation is based on sorted lock-free linked list.
   * Owing to optimization this method should intentionally fail to add {@param node}
   * if it's value is not greater than value of last added node.
   * <p>
   * The simplest way to achieve this is to re-write oneTryPut with very slight changes in it.
   */

  public Node<M> append(Node<M> node) {
    if (node == null || node.getValue() == null) throw new NullPointerException("Attempt to add null node or value");

    boolean tryToInsert = true;
    while (isOpened() && tryToInsert) {
      tryToInsert = oneTryPutOrdered(node);
    }
    StorageNode<Node<M>> lastAdded = tail.get();
    return (lastAdded == lastDummyNode) ? null : lastAdded.storedItem();
  }

  /**
   * Return whether nodeToAdd wasn't inserted and another attempt is required.
   *
   * @param nodeToAdd should be nonNull similar to it's value
   */
  private boolean oneTryPutOrdered(Node<M> nodeToAdd) {
    StorageNode<Node<M>> tailNode = tail.get();
    StorageNode<Node<M>> afterTail = tailNode.next.get();
    if (tailNode == lastDummyNode) return false; // the greatest. Should be checked first.

    boolean needToAdd = true; // we don't know before comparing
    if (tailNode != head.get()) {
      needToAdd = nodeToAdd.getValue().compareTo(tailNode.storedItem().getValue()) > 0;
    }

    if (afterTail != null) {
      tail.compareAndSet(tailNode, afterTail);
      return needToAdd; // might still don't know, but are to help
    }

    if (needToAdd) {
      StorageNode<Node<M>> candidateToStore = new StorageNode<>(nodeToAdd);
      if (tailNode.next.compareAndSet(null, candidateToStore)) {
        tail.compareAndSet(tailNode, candidateToStore);
        return false; // was inserted no more needs of insertion
      } // if cas fails, the need still remaining
    }
    return needToAdd;
  }

  @Override
  public Iterator<Node<M>> iterator() {
    return super.iterator();
  }
}
