package SnapCollectorDetails;

import utils.Node;

import java.util.Iterator;

/**
 * Хранит предоставленные отчёты. Отвечает за lock-free сбор report'ов c потоков-писателей.
 * Вся содержательная логика в родительском классе.
 */
class ReportStorage<R> extends GenericStorage<Report<R>> implements Iterable<Report<R>> {

  void append(Node<R> node, ReportType reportType) {
    super.put(new Report<R>(node, reportType));
  }

  @Override
  public Iterator<Report<R>> iterator() {
    return super.iterator();
  }
}

class Report<R> {
  private final Node<R> reportedNode;
  private final ReportType reportType;

  Report(Node<R> reportedNode, ReportType reportType) {
    this.reportedNode = reportedNode;
    this.reportType = reportType;
  }

  public Node<R> reportedNode() {
    return reportedNode;
  }

  public ReportType reportType() {
    return reportType;
  }
}
