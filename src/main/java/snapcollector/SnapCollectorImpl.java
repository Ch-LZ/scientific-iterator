package snapcollector;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import utils.Node;

public class SnapCollectorImpl<T extends Comparable<T>> extends SnapCollector<T> {
  private final AtomicBoolean ongoingScan; // indicates if it is closed
  private final ThreadLocal<Boolean> wasRegistered;  // is expected to be deleted with SnapCollector
  private final ThreadLocal<ReportStorage<T>> reportsStorage; // is expected to be deleted with SnapCollector
  private ReportStorageManager<T> thredLocalMap;
  private final ScannerStorage<T> scannerStorage;

  public SnapCollectorImpl() {
    ongoingScan = new AtomicBoolean(true);
    wasRegistered = ThreadLocal.withInitial(() -> false);
    reportsStorage = ThreadLocal.withInitial(ReportStorage::new);
    scannerStorage = new ScannerStorage<T>();
    thredLocalMap = new ReportStorageManager<>();
  }

  @Override
  public boolean isActive() {
    return ongoingScan.get();
  }

  @Override
  public void addReport(Node<T> victim, ReportType reportType) {
    if (!wasRegistered.get()) {
      thredLocalMap.put(reportsStorage.get());
      wasRegistered.set(true);
    }
    reportsStorage.get().append(victim, reportType);
  }

  @Override
  Node<T> addNode(Node<T> node) {
    return scannerStorage.append(node);
  }

  @Override
  void blockFurtherNodes() {
    scannerStorage.close();
  }

  @Override
  void blockFurtherReports() {
    thredLocalMap.close();
    for (ReportStorage<T> st : thredLocalMap) {
      st.close();
    }
  }

  @Override
  void deactivate() {
    ongoingScan.set(false);
  }

  @Override
  Iterator<Node<T>> readScannedNodes() {
    return scannerStorage.iterator();
  }

  @Override
  Iterator<Report<T>> readReports() {
    return thredLocalMap.readReports();
  }
}
