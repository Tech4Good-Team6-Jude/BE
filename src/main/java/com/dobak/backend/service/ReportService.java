package com.dobak.backend.service;

import com.dobak.backend.dto.ErrorPatternSummary;
import com.dobak.backend.dto.PracticeAttemptSummary;
import com.dobak.backend.dto.ReportResponse;
import com.dobak.backend.entity.ErrorPattern;
import com.dobak.backend.entity.PracticeAttempt;
import com.dobak.backend.entity.Report;
import com.dobak.backend.entity.User;
import com.dobak.backend.repository.ErrorPatternRepository;
import com.dobak.backend.repository.PracticeAttemptRepository;
import com.dobak.backend.repository.ReportRepository;
import com.dobak.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 보호자용 리포트. 최근 PracticeAttempt(발음 이력)와 ErrorPattern(오류 공통분모)을
 * 기간(기본: 최근 7일) 단위로 모아서 요약한다.
 */
@Service
public class ReportService {

    private final UserRepository userRepository;
    private final PracticeAttemptRepository practiceAttemptRepository;
    private final ErrorPatternRepository errorPatternRepository;
    private final ReportRepository reportRepository;

    public ReportService(UserRepository userRepository,
                          PracticeAttemptRepository practiceAttemptRepository,
                          ErrorPatternRepository errorPatternRepository,
                          ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.practiceAttemptRepository = practiceAttemptRepository;
        this.errorPatternRepository = errorPatternRepository;
        this.reportRepository = reportRepository;
    }

    public ReportResponse generateLatest(Long childId) {
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이 계정: " + childId));

        LocalDateTime periodEnd = LocalDateTime.now();
        LocalDateTime periodStart = periodEnd.minusDays(7);

        List<PracticeAttempt> attempts = practiceAttemptRepository
                .findByChildIdAndCreatedAtBetween(childId, periodStart, periodEnd);
        List<ErrorPattern> patterns = errorPatternRepository.findByChildId(childId);

        double avgAccuracy = attempts.stream().mapToDouble(PracticeAttempt::getAccuracy).average().orElse(0);
        String summary = buildSummary(attempts.size(), avgAccuracy, patterns);

        Report report = new Report(child, periodStart, periodEnd, summary);
        reportRepository.save(report);

        List<PracticeAttemptSummary> attemptSummaries = attempts.stream()
                .map(a -> new PracticeAttemptSummary(a.getId(), a.getTargetText(), a.getAccuracy(), a.getCreatedAt()))
                .toList();
        List<ErrorPatternSummary> patternSummaries = patterns.stream()
                .map(p -> new ErrorPatternSummary(p.getErrorType(), p.getOccurrenceCount()))
                .toList();

        return new ReportResponse(report.getId(), periodStart, periodEnd, summary, attemptSummaries, patternSummaries);
    }

    private String buildSummary(int attemptCount, double avgAccuracy, List<ErrorPattern> patterns) {
        if (attemptCount == 0) {
            return "이번 주에는 아직 발음 연습 기록이 없어요.";
        }
        String topPattern = patterns.stream()
                .max((a, b) -> Integer.compare(a.getOccurrenceCount(), b.getOccurrenceCount()))
                .map(ErrorPattern::getErrorType)
                .orElse("아직 뚜렷한 유형 없음");

        return String.format(
                "이번 주 %d회 발음 연습, 평균 정확도 %.0f%%. 가장 자주 나타난 오류 유형: %s.",
                attemptCount, avgAccuracy * 100, topPattern
        );
    }
}
