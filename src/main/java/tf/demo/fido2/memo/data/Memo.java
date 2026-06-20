package tf.demo.fido2.memo.data;

import java.time.Instant;

public record Memo(long id, String content, Instant createdAt) {}
