package tf.demo.fido2.memo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tf.demo.fido2.memo.data.Memo;
import tf.demo.fido2.memo.service.MemoService;

@SpringBootTest(properties = "memo.storage=db")
class MemoServiceTest {

    @Autowired private MemoService memoService;

    @AfterEach
    void tearDown() {
        memoService.clear();
    }

    @Test
    void saveStoresTrimmedMemo() {
        Memo memo = memoService.save("  hello memo  ");

        assertThat(memo.id()).isPositive();
        assertThat(memo.content()).isEqualTo("hello memo");
        assertThat(memoService.findAll()).containsExactly(memo);
    }

    @Test
    void findByIdReturnsSavedMemo() {
        Memo saved = memoService.save("memo");

        assertThat(memoService.findById(saved.id())).hasValue(saved);
    }

    @Test
    void deleteByIdRemovesMemo() {
        Memo saved = memoService.save("memo");

        assertThat(memoService.deleteById(saved.id())).isTrue();
        assertThat(memoService.findById(saved.id())).isEmpty();
    }

    @Test
    void saveRejectsBlankContent() {
        assertThatThrownBy(() -> memoService.save("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("memo content must not be blank");
    }

    @Test
    void saveAllStoresEachMemo() {
        List<Memo> saved = memoService.saveAll(List.of("a", "b"));

        assertThat(saved).hasSize(2);
        assertThat(memoService.findAll()).containsExactlyElementsOf(saved);
    }
}
