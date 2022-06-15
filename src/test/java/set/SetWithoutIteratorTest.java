package set;

import org.jetbrains.kotlinx.lincheck.*;
import org.jetbrains.kotlinx.lincheck.annotations.Operation;
import org.jetbrains.kotlinx.lincheck.annotations.Param;
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen;
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingCTestConfiguration;
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions;
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressCTestConfiguration;
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions;
import org.junit.Test;



@Param(name = "value", gen = IntGen.class, conf = "1:5")
public class SetWithoutIteratorTest {
  private final Set<Integer> set; // concurrent set

  public SetWithoutIteratorTest() {
    set = new SetImpl<>();
  }

  @Operation
  public boolean add(@Param(name = "value") int value) {
    return set.add(value);
  }

  @Operation
  public boolean remove(@Param(name = "value") int value) {
    return set.remove(value);
  }

  @Operation
  public boolean contains(@Param(name = "value") int value) {
    return set.contains(value);
  }

  /**
   * Left for further debugging purposes
   * */
//  @Test
  public void runTest() {
    Options<ModelCheckingOptions, ModelCheckingCTestConfiguration> options = new ModelCheckingOptions()
        .iterations(10)
        .threads(3)
        .actorsPerThread(3)
        .actorsBefore(0)
        .actorsAfter(0)
        .logLevel(LoggingLevel.INFO);

    LinChecker.check(SetTest.class, options);
  }

  @Test
  public void setStressTest3Threads() {
    Options<StressOptions, StressCTestConfiguration> options = new StressOptions()
        .iterations(100)
        .threads(3)
        .actorsBefore(0)
        .actorsPerThread(4)
        .actorsAfter(0)
        .logLevel(LoggingLevel.INFO);

    LinChecker.check(SetTest.class, options);
  }
}


