package tf.demo.fido2.memo.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import tf.demo.fido2.memo.data.Memo;
import tf.demo.fido2.memo.storage.MemoStorage;

@Service
public class MemoService {

  private final MemoStorage memoStorage;

  public MemoService(MemoStorage memoStorage) {
    this.memoStorage = memoStorage;
  }

  public Memo save(String content) {
    return memoStorage.save(content);
  }

  public Optional<Memo> findById(long id) {
    return memoStorage.findById(id);
  }

  public List<Memo> findAll() {
    return memoStorage.findAll();
  }

  public boolean deleteById(long id) {
    return memoStorage.deleteById(id);
  }

  public void clear() {
    memoStorage.clear();
  }

  public List<Memo> saveAll(List<String> contents) {
    return memoStorage.saveAll(contents);
  }
}
