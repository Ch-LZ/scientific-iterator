package snapcollector;

import utils.Node;

import java.util.Iterator;

public abstract class SnapCollector<T> {
  /**
   * Returns true if deactivate() method has not been called, and false otherwise.
   *
   * True value means the scanning is still ongoing
   * and further references might still be added in the snapshot object.
   */
  abstract boolean isActive();

  /**
   * Register a given report. May fail to add the report if the blockFurtherReports() method has
   * previously been invoked.
   */
  abstract void addReport(Node<T> victim, ReportType reportType);

  /**
   * Register a reference to a given node. May fail to register the reference if the
   * blockFurtherNodes() method has previously been invoked.
   *
   * If nodes in list-based set are sorted in ascending order of keys(values//data) (they are by implementation),
   * this method may provide wait-freedom by intentionally failing adding any
   * node whose key is smaller or equal to the key of the last node added to the snap-collector and
   * returning the pointer to last added node. In such a way, a new iterating thread, that joins to
   * snapshot scanning can jump to the current location.
   */
  abstract Node<T> addNode(Node<T> node);

  /**
   * Required to synchronize between multiple iterators.
   *
   * After this method is completed, any further calls to addNode will do nothing. Calls to addNode
   * concurrent with blockFurtherNodes may fail or succeed arbitrarily.
   *
   * Similarly to blockFurtherReports inserts a dummy via CAS to the end of list.
   * The success of this CAS need not be checked by implementation:
   * after being closed, either lastDummyNode is inserted or only one last added node.
   */
  abstract void blockFurtherNodes();

  /**
   * Required to synchronize between multiple iterators.
   * This method should only be invoked after the execution of the Deactivate method is completed.
   *
   * After this method is competed, any further calls to report will do nothing. Calls to report
   * concurrent with blockFurtherReports may succeed or fail arbitrarily.
   *
   * This method goes over all the threads local linked-lists of reports, instead it goes to only one storage,
   * and attempts by a CAS to add a special dummy report at the end to block further addition of reports.
   *
   * The success of this CAS need not be checked. If the CAS succeeds, no further reports can be added to this list,
   * because no thread will add a report after a dummy. If the CAS fails, then either another iterating thread
   * has added a dummy, or a report has just been added. The first case guarantees blocking
   * further reports, but even in the latter case, no further reports can now be added to this list,
   * because the thread that just added this report will see that the snap-collector is inactive
   * and will not attempt to add another report.
   */
  abstract void blockFurtherReports();

  /**
   * After this method is complete, any call of isActive return false, whereas before this method is
   * invoked for the first time, isActive returns true.
   */
  abstract void deactivate();

  /**
   * Provides ability to read scanned Notes. Should be called only after blockFurtherReports is
   * called by some thread.
   */
  abstract Iterator<Node<T>> readScannedNodes();

  /**
   * Provides ability to read all reports collected in the snapshot object. Should be called only
   * after blockFurtherReports is called by some thread.
   */
  abstract Iterator<Report<T>> readReports();
}
