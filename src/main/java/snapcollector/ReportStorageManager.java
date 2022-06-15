package snapcollector;

import java.util.Collections;
import java.util.Iterator;

/**
 * Helping class to manage many {@link ReportStorage} from threads simultaniously.
 *
 * @param <R> type of element stored with report.
 */
class ReportStorageManager<R> extends GenericStorage<ReportStorage<R>> implements Iterable<ReportStorage<R>> {
  @Override
  public Iterator<ReportStorage<R>> iterator() {
    return super.iterator();
  }

  Iterator<Report<R>> readReports() {
    return new ReportStoragesIterator();
  }

  private class ReportStoragesIterator implements Iterator<Report<R>>{
    private final Iterator<ReportStorage<R>> it2D;
    private Iterator<Report<R>> it1D;
    ReportStoragesIterator() {
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
