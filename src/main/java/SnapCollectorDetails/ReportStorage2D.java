package SnapCollectorDetails;

import java.util.Collections;
import java.util.Iterator;

class ReportStorage2D<R> extends GenericStorage<ReportStorage<R>> implements Iterable<ReportStorage<R>> {
  @Override
  public Iterator<ReportStorage<R>> iterator() {
    return super.iterator();
  }

  Iterator<Report<R>> readReports() {
    return new ReportStorage2dIterator();
  }

  private class ReportStorage2dIterator implements Iterator<Report<R>>{
    private final Iterator<ReportStorage<R>> it2D;
    private Iterator<Report<R>> it1D;
    ReportStorage2dIterator() {
      it2D = iterator();
      it1D = Collections.emptyIterator();
    }

    @Override
    public boolean hasNext() {
      if (!it1D.hasNext()) {
        while (it2D.hasNext()) {
          it1D = it2D.next().iterator();
          if (it1D.hasNext()) return true;
        }
      }
      return it1D.hasNext();
    }

    @Override
    public Report<R> next() {
      return it1D.next();
    }
  }
}
