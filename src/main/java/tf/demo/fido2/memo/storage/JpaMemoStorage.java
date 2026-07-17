package tf.demo.fido2.memo.storage;

import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tf.demo.fido2.memo.data.Memo;
import tf.demo.fido2.memo.entity.MemoEntity;
import tf.demo.fido2.memo.repository.MemoRepository;

@Component
@ConditionalOnProperty(name = "memo.storage", havingValue = "db", matchIfMissing = true)
public class JpaMemoStorage implements MemoStorage {

    private final MemoRepository memoRepository;

    public JpaMemoStorage(MemoRepository memoRepository) {
        this.memoRepository = memoRepository;
    }

    @Override
    @Transactional
    public Memo save(String content) {
        validateContent(content);

        MemoEntity saved = memoRepository.save(new MemoEntity(content.trim()));
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Memo> findById(long id) {
        return memoRepository.findById(id).map(JpaMemoStorage::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Memo> findAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt").and(Sort.by(Sort.Direction.ASC, "id"));
        return memoRepository.findAll(sort).stream().map(JpaMemoStorage::toDomain).toList();
    }

    @Override
    @Transactional
    public boolean deleteById(long id) {
        if (!memoRepository.existsById(id)) {
            return false;
        }

        memoRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public void clear() {
        memoRepository.deleteAllInBatch();
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("memo content must not be blank");
        }
    }

    private static Memo toDomain(MemoEntity entity) {
        return new Memo(entity.getId(), entity.getContent(), entity.getCreatedAt());
    }
}
