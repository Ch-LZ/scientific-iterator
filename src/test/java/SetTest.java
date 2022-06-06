import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlinx.lincheck.*;
import org.jetbrains.kotlinx.lincheck.annotations.Operation;
import org.jetbrains.kotlinx.lincheck.annotations.Param;
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen;
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingCTestConfiguration;
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions;
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressCTestConfiguration;
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@Param(name = "value", gen = IntGen.class, conf = "1:5")
public class SetTest {
  private final Set<Integer> set; // concurrent set

  public SetTest() {
    set = new SetImpl<>();
  }

  @Operation
  public boolean add(@Param(name = "value") @NotNull int value) {
    return set.add(value);
  }

  @Operation
  public boolean remove(@Param(name = "value") @NotNull int value) {
    return set.remove(value);
  }

  @Operation
  public boolean contains(@Param(name = "value") int value) {
    return set.contains(value);
  }

  @Operation
  public boolean isEmpty() {
    return set.isEmpty();
  }

  @Operation
  public List<Integer> fullSnapshot() {
    Iterator<Integer> it = set.iterator();

    List<Integer> outList = new ArrayList<>();

    while (it.hasNext()) {
      outList.add(it.next());
    }
    outList.sort(Comparator.naturalOrder());
    return outList;
  }

  /**
   * Single threaded guard. Located here in order to defence correctness. Simple, yet efficient.
   */
  @Test
  public void singleThreadTest() {
    Set<Integer> set = new SetImpl<>();

    assertTrue(set.isEmpty());

    set.add(3);
    assertTrue(set.contains(2));
    assertFalse(set.isEmpty());

    set.remove(3);
    assertFalse(set.contains(3));
    assertTrue(set.isEmpty());
  }


  /**
   * Left for further debugging purposes
   */
//  @Test
  public void runTest() {
    Options<ModelCheckingOptions, ModelCheckingCTestConfiguration> options = new ModelCheckingOptions()
        .iterations(10)
        .threads(2)
        .actorsPerThread(3)
        .actorsBefore(0)
        .actorsAfter(0)
        .logLevel(LoggingLevel.INFO);

    LinChecker.check(SetTest.class, options);
  }

  /**
   * Fast check small changes
   * */
  @Test
  public void devStressTest() {
    Options<StressOptions, StressCTestConfiguration> options = new StressOptions()
        .iterations(10)
        .threads(3)
        .actorsBefore(0)
        .actorsPerThread(4)
        .actorsAfter(0)
        .logLevel(LoggingLevel.INFO);

    LinChecker.check(SetTest.class, options);
  }

  @Test
  public void setStressTest3Threads() {
    Options<StressOptions, StressCTestConfiguration> options = new StressOptions()
        .iterations(300)
        .threads(3)
        .actorsBefore(0)
        .actorsPerThread(4)
        .actorsAfter(0)
        .logLevel(LoggingLevel.INFO);

    LinChecker.check(SetTest.class, options);
  }

  @Test
  public void setHardStressTest3Threads() {
    Options<StressOptions, StressCTestConfiguration> options = new StressOptions()
        .iterations(10)
        .threads(4)
        .actorsBefore(0)
        .actorsPerThread(4)
        .actorsAfter(0)
        .logLevel(LoggingLevel.INFO);

    LinChecker.check(SetTest.class, options);
  }
}
