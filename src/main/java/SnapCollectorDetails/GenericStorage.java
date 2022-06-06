package SnapCollectorDetails;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Реализует логику работы очереди Майкла Скотта при добавлении. Дополнена возможностью закрытия
 * специальным узлом lastDummyNode.
 */
abstract class GenericStorage<N> {
  protected AtomicBoolean closed;

  protected final AtomicReference<StorageNode<N>> head;
  protected final AtomicReference<StorageNode<N>> tail;
  protected static final StorageNode lastDummyNode = new StorageNode<>(null);

  /**
   * Collecting nodes with their values. Collecting nodes instead of values is essential for
   * correctness of algorithm
   */
  protected static class StorageNode<N> {
    public final N storedItem;
    protected final AtomicReference<StorageNode<N>> next;

    StorageNode(N itemToStore) {
      this.storedItem = itemToStore;
      next = new AtomicReference<>(null);
    }

    public N storedItem() {
      return storedItem;
    }

  }

  protected GenericStorage() {
    closed = new AtomicBoolean(false);
    StorageNode<N> dummy = new StorageNode<>(null);
    head = new AtomicReference<>(dummy);
    tail = new AtomicReference<>(dummy);
  }

  protected boolean oneTryPut(StorageNode<N> storageNode) {
    StorageNode<N> tailNode = tail.get();
    StorageNode<N> afterTail = tailNode.next.get();
    /*
     * thread should help to move the tail, but, if tail node is already the lastDummyNode, there is
     * no need to move it
     */
    if (tailNode == lastDummyNode) return true;
    if (afterTail == null) {
      /* if tailNode is the last, we can add another one element and try to move tail */
      if (tailNode.next.compareAndSet(null, storageNode)) {
        tail.compareAndSet(tailNode, storageNode);
        return true;
      }
    } else {
      tail.compareAndSet(tailNode, afterTail);
    }
    return false;
  }

  /**
   * TestOnly
   * <p>
   * Designed to single-thread tests only.
   */
  public N peekLastEnqueuedUnsafe() {
    return tail.get().storedItem;
  }

  protected void close() {
    /* insert dummy node, ensuring that at least one thread has put a dummy */
    closed.set(true);
    oneTryPut(lastDummyNode);
  }

  public boolean isOpened() {
    return !closed.get();
  }

  protected void put(N itemToStore) {
    StorageNode<N> node = new StorageNode<>(itemToStore);
    while (isOpened()) {
      if (oneTryPut(node)) {
        break;
      }
    }
  }


  protected Iterator<N> iterator() {
    if (isOpened())
      throw new IllegalStateException("Implementation error. Attempt to begin iteration over open storage.");
    return new StorageIterator();
  }

  protected class StorageIterator implements Iterator<N> {
    StorageIterator() {
      curr = head.get().next.get();
    }

    private StorageNode<N> curr;

    @Override
    public boolean hasNext() {
      return curr != lastDummyNode && curr != null;
    }

    @Override
    public N next() {
      if (!hasNext()) throw new NoSuchElementException("Iterator in the storage is already ended!");
      StorageNode<N> result = curr;
      curr = curr.next.get();
      return result.storedItem();
    }
  }
}
