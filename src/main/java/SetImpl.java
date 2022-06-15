import snapcollector.SnapCollector;
import snapcollector.SnapCollectorDummy;
import snapcollector.SnapshotManager;
import utils.Node;
import utils.Pair;

import java.util.Iterator;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;


public class SetImpl<T extends Comparable<T>> implements Set<T> {

  private final Node<T> head;
  private final AtomicReference<SnapCollector<T>> currSnapCollectorRef;

  public SetImpl() {
    head = new Node<>(null, new AtomicMarkableReference<>(null, false));
    currSnapCollectorRef = new AtomicReference<SnapCollector<T>>(new SnapCollectorDummy<>());
  }

  @Override
  public boolean add(T value) {
    /* maintains list sorted in ascending order */
    while (true) {
      Pair<Node<T>, Node<T>> predAndCurr = find(value);
      Node<T> pred = predAndCurr.getFirst();
      Node<T> curr = predAndCurr.getSecond();
      /* if value already exists in the set */
      if ((curr != null) && curr.getValue().compareTo(value) == 0) {
        /* should report about value existing to SnapCollector */
        SnapshotManager.reportAdd(curr, currSnapCollectorRef);
        return false;
      } else {
        Node<T> node = new Node<>(value, new AtomicMarkableReference<>(curr, false));
        if (pred.getNextAndMark().compareAndSet(curr, node, false, false)) {
          /* should report about new value to SnapCollector */
          SnapshotManager.reportAdd(node, currSnapCollectorRef);
          return true;
        }
      }
    }
  }

  @Override
  public boolean remove(T value) {
    Objects.requireNonNull(value, "Null value is used for inner purposes.");

    while (true) {
      Pair<Node<T>, Node<T>> prevAndCurr = find(value);
      Node<T> previous = prevAndCurr.getFirst();
      Node<T> current = prevAndCurr.getSecond();

      if ((current == null) || current == head || current.getValue().compareTo(value) != 0) {
        /* if no value found, no report needed */
        return false;
      } else {
        Node<T> successor = current.getNextAndMark().getReference();
        /* logical deletion of current */
        boolean logicallyDeleted =
            current.getNextAndMark().compareAndSet(successor, successor, false, true);
        if (!logicallyDeleted) continue;

        /*
         * Removing should be reported before physical deletion, as a result if node is no longer in
         * the list, it is guaranteed to have been reported as deleted.
         */
        SnapshotManager.reportRemove(current, currSnapCollectorRef);
        /* physical deletion */
        previous.getNextAndMark().compareAndSet(current, successor, false, false);
        /* memory reclamation provided by java GC */
        return true;
      }
    }
  }

  @Override
  public boolean contains(T value) {
    Node<T> curr = head.getNextAndMark().getReference();

    /* until node with value is not found */
    while (Objects.nonNull(curr) && (curr.getValue().compareTo(value) != 0)) {
      curr = curr.getNextAndMark().getReference();
    }
    /* if was not found, no report needed */
    if (curr == null) return false;

    /* was found bearing a deleted mark -> a supporting report should be provided */
    if (curr.getNextAndMark().isMarked()) {
      SnapshotManager.reportRemove(curr, currSnapCollectorRef);
      return false;
    }

    /* was found -> a supporting report should be provided */
    SnapshotManager.reportAdd(curr, currSnapCollectorRef);
    return true;
  }

  /**
   * Находит наиболее подходящее место для нового узла с value в сортированном возрастанию списке.
   */
  private Pair<Node<T>, Node<T>> find(T value) {
    retry: 
    while (true) {
      Node<T> previous = head;
      Node<T> current = previous.getNextAndMark().getReference();
      while (true) {
        // helping as much as possible
        while (current != null && current.getNextAndMark().isMarked()) {
          /* Helping with deletion. Before physical deletion the node has to be reported as deleted. */
          SnapshotManager.reportRemove(current, currSnapCollectorRef);
          Node<T> successor = current.getNextAndMark().getReference();
          boolean helped = /* delete physically */
              previous.getNextAndMark().compareAndSet(current, successor, false, false);
          if (!helped) continue retry;
          current = successor;
        }
        if (current == null)
          return new Pair<>(previous, null);

        if ((!(current == head)) && current.getValue().compareTo(value) >= 0) {
          return new Pair<>(previous, current); /* Insertion report is redundant */
        }
        previous = current;
        current = current.getNextAndMark().getReference();
      }
    }
  }

  @Override
  public boolean isEmpty() {
    return !iterator().hasNext();
  }

  /**
   * wait-free
   */
  @Override
  public Iterator<T> iterator() {
    HashSet<T> snapshot = SnapshotManager.scanSnapshot(head, currSnapCollectorRef);
    return snapshot.iterator();
  }
}
