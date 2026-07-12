package tf.demo.fido2.memo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.demo.fido2.memo.entity.MemoEntity;

public interface MemoRepository extends JpaRepository<MemoEntity, Long> {}
