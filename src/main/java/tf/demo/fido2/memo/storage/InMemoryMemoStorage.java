package tf.demo.fido2.memo.storage;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tf.demo.fido2.memo.data.Memo;

@Component
@ConditionalOnProperty(name = "memo.storage", havingValue = "memory")
public class InMemoryMemoStorage implements MemoStorage {

    private final ConcurrentHashMap<Long, Memo> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0L);

    @Override
    public Memo save(String content) {
        validateContent(content);

        long id = sequence.incrementAndGet();
        Memo memo = new Memo(id, content.trim(), Instant.now());
        store.put(id, memo);
        return memo;
    }

    @Override
    public Optional<Memo> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Memo> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Memo::createdAt).thenComparing(Memo::id))
                .toList();
    }

    @Override
    public boolean deleteById(long id) {
        return store.remove(id) != null;
    }

    @Override
    public void clear() {
        store.clear();
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("memo content must not be blank");
        }
    }
}
