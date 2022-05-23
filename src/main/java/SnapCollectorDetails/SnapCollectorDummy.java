package SnapCollectorDetails;

import utils.Node;

import java.util.Iterator;

/**
 * Intended to hold a place for real SnapCollector before first snapshot is needed. Should just be inactive permanently.
 * It serves to prevent redundant report collection at the start of concurrent set lifetime.
 * */
public class SnapCollectorDummy<T>  extends SnapCollector<T> {
  @Override
  boolean isActive() { return false;}

  @Override
  void addReport(Node<T> victim, ReportType reportType) {
    unsupportedOp();
  }

  @Override
  Node<T> addNode(Node<T> node) {
    unsupportedOp();
    return null;
  }

  @Override
  void blockFurtherNodes() {
    unsupportedOp();
  }

  @Override
  void blockFurtherReports() {
    unsupportedOp();
  }

  @Override
  void deactivate() {
    unsupportedOp();
  }

  @Override
  Iterator<Node<T>> readScannedNodes() {
    unsupportedOp();
    return null;
  }

  @Override
  Iterator<Report<T>> readReports() {
    unsupportedOp();
    return null;
  }

  /**
   * Intended to explicitly block all unsupported operations.
   * */
  void unsupportedOp() {
    throw new UnsupportedOperationException("This operation is not expected to be called on this object.");
  }
}
