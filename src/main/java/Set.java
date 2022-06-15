/**
 * Lock-Free множество.
 * 
 * @param <T> Тип ключей
 */
public interface Set<T extends Comparable<T>> {
  /**
   * Add value in set
   *
   * At least lock-free.
   *
   * @param value
   * @return false if value is already exist in set, true if value was added successfully.
   */
  boolean add(T value);
  


  /**
   * Remove value from set.
   *
   * At least lock-free.
   *
   * @param value
   * @return false if value was not found in set, true if value was successfully removed.
   */
  boolean remove(T value);


  /**
   * Checking if the value exist in set.
   *
   * At least wait-free for types of finite size, lock-free for others.
   *
   * @param value
   * @return true if element was found, false overwise.
   */
  boolean contains(T value);

  /**
   * Checking if set is empty.
   *
   * At least lock-free
   *
   * @return true if set is empty, false overwise.
   */
  boolean isEmpty();

  /**
   * Provide at least lock-free iterator for set.
   *
   * Iterator should be linearizable in terms of providing a set of elements that has ever existed.
   *
   * @return iterator on set without remove operation
   */
  java.util.Iterator<T> iterator();
}
