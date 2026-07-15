package com.dobak.backend.repository;

import com.dobak.backend.entity.StuckSentence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StuckSentenceRepository extends JpaRepository<StuckSentence, Long> {

    /** L4: 이 책에서 이 아이가 표시한 막힌 문장 전체 목록 */
    List<StuckSentence> findByChildIdAndBookIdOrderByCreatedAtAsc(Long childId, Long bookId);

    /** 아직 반복학습으로 안 풀린(익히지 못한) 막힌 문장만 */
    List<StuckSentence> findByChildIdAndBookIdAndResolvedFalseOrderByCreatedAtAsc(Long childId, Long bookId);

    /** L2 상세 화면 "막힌 문장 N개" 배지, L6 완료 집계용 */
    long countByChildIdAndBookId(Long childId, Long bookId);

    long countByChildIdAndBookIdAndResolvedTrue(Long childId, Long bookId);

    /** L3에서 같은 문장을 다시 탭했을 때 이미 막힌 문장으로 표시돼 있는지 확인(토글용) */
    Optional<StuckSentence> findByChildIdAndSentenceId(Long childId, Long sentenceId);
}
