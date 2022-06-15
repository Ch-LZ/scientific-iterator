package snapcollector;

import utils.Node;
import utils.Pair;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import static snapcollector.ReportType.DELETED;
import static snapcollector.ReportType.INSERTED;

/**
 * Initnally implemented aspecially for lock-free linked list based set.
 *
 * Require from
 */
public class SnapshotManager {

  public static <T extends Comparable<T>> void reportRemove(Node<T> victim, AtomicReference<SnapCollector<T>> currSnapCollectorRef) {
    SnapCollector<T> sc = currSnapCollectorRef.getAcquire();
    if (sc.isActive()) {
      sc.addReport(victim, DELETED);
    }
  }

  public static <T extends Comparable<T>> void reportAdd(Node<T> newNode, AtomicReference<SnapCollector<T>> currSnapCollectorRef) {
    SnapCollector<T> sc = currSnapCollectorRef.getAcquire();
    /* isMarked must be checked to prevent false-add reporting about already marked nodes. */
    if (sc.isActive() && (!newNode.getNextAndMark().isMarked())) {
      sc.addReport(newNode, INSERTED);
    }
  }

  /**
   * Scan consistent snspshot.
   */
  public static <T extends Comparable<T>> HashSet<T> scanSnapshot(Node<T> head, AtomicReference<SnapCollector<T>> currSnapCollectorRef) {
    SnapCollector<T> sc = acquireSnapCollector(currSnapCollectorRef);
    SnapshotManager.collectSnapshot(sc, head);
    return SnapshotManager.reconstructUsingReports(sc);
  }

  /**
   * Get local copy of currSnapCollectorRef or create a new one.
   */
  private static <T extends Comparable<T>> SnapCollector<T> acquireSnapCollector(AtomicReference<SnapCollector<T>> currSnapCollectorRef) {
    SnapCollector<T> sc = currSnapCollectorRef.get();
    if (sc.isActive()) {
      /* even if the thread would be suspended between these lines,
      snapshot will be correct due to linearization point of snap collector (then deactivate called) */
      return sc;
    }
    /* if the was no ongoing snapshot - new one is required to create */
    SnapCollector<T> newSc = new SnapCollectorImpl<>();

    currSnapCollectorRef.compareAndSet(sc, newSc);
    /*
     * if CAS fails, then another iterating thread must succeed, so current thread can just grab
     * generated one. Even if snapCollector will be already deactivated it would be actual. It must have started scanning before
     * "iterator" invocation, so it is expected to be relevant.
     */
    newSc = currSnapCollectorRef.get();
    return newSc;
  }

  /**
   * Walk through the list of nodes and stores them. Support many threads.
   */
  private static <T extends Comparable<T>> void collectSnapshot(SnapCollector<T> sc, Node<T> head) {
    Node<T> current = head.getNextAndMark().getReference();
    /* should be checked every time, because of many iterating threads can work on the same snapshot
     * simultaneously. */
    while (sc.isActive()) {
      if (current != null && !(current.getNextAndMark().isMarked())) {
        /* jump straight to last added node for optimization proposes. Become null is end is reached. */
        current = sc.addNode(current);
      }
      /* current become the last only if the end is reached */
      if (current == null || current.getNextAndMark().getReference() == null) {
        sc.blockFurtherNodes(); // prohibit other threads to further scan
        sc.deactivate();
        break;
      }

      current = current.getNextAndMark().getReference();
    }
    sc.blockFurtherReports();
  }

  /* only applied after deactivate */
  static <T extends Comparable<T>> HashSet<T> reconstructUsingReports(SnapCollector<T> sc) {
    Iterator<Node<T>> scannedNodes = sc.readScannedNodes();
    /* The link serves as identifier, providing ability
      to distinct different nodes with the same values to apply reports correctly it is essential for correctness. */
    HashSet<Node<T>> scannedNodesSet = new HashSet<>();
    while (scannedNodes.hasNext()) {
      scannedNodesSet.add(scannedNodes.next());
    }

    Pair<HashSet<Node<T>>, HashSet<Node<T>>> insertedAndDeleted = processReports(sc);
    HashSet<Node<T>> snapshotNodes = new HashSet<>();
    /* A node (not value) is in the snapshot if it (is in the scannedStorage OR reported as INSERTED)
     AND not reported as DELETED */
    snapshotNodes.addAll(scannedNodesSet);
    snapshotNodes.addAll(insertedAndDeleted.getFirst());
    snapshotNodes.removeAll(insertedAndDeleted.getSecond());

    HashSet<T> snapshot = new HashSet<>();
    for (Node<T> node : snapshotNodes) {
      snapshot.add(node.getValue());
    }
    return snapshot;
  }

  private static <T extends Comparable<T>> Pair<HashSet<Node<T>>, HashSet<Node<T>>> processReports(SnapCollector<T> sc) {
    Iterator<Report<T>> reports = sc.readReports();
    HashSet<Node<T>> deletedSet = new HashSet<>();
    HashSet<Node<T>> insertedSet = new HashSet<>();
    while (reports.hasNext()) {
      Report<T> eachReport = reports.next();
      switch (eachReport.reportType()) {
        case DELETED:
          deletedSet.add(eachReport.reportedNode());
          break;
        case INSERTED:
          insertedSet.add(eachReport.reportedNode());
          break;
        default:
          break;
      }
    }
    return new Pair<>(insertedSet, deletedSet);
  }
}
