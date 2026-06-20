package tf.demo.fido2.memo.storage;

import java.util.List;
import java.util.Optional;
import tf.demo.fido2.memo.data.Memo;

public interface MemoStorage {

  Memo save(String content);

  Optional<Memo> findById(long id);

  List<Memo> findAll();

  boolean deleteById(long id);

  void clear();

  default List<Memo> saveAll(List<String> contents) {
    if (contents == null) {
      return List.of();
    }

    return contents.stream().map(this::save).toList();
  }
}
