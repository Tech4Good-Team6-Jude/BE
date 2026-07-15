package com.dobak.backend.service;

import com.dobak.backend.dto.ChildReportResponse;
import com.dobak.backend.dto.DayCheckIn;
import com.dobak.backend.dto.ErrorPatternSummary;
import com.dobak.backend.dto.PracticeAttemptSummary;
import com.dobak.backend.dto.ProgressResponse;
import com.dobak.backend.dto.PronunciationComparison;
import com.dobak.backend.dto.ReportResponse;
import com.dobak.backend.dto.ReportSummary;
import com.dobak.backend.entity.PracticeAttempt;
import com.dobak.backend.entity.ReadingSession;
import com.dobak.backend.entity.Report;
import com.dobak.backend.entity.User;
import com.dobak.backend.repository.CaptureSentenceRepository;
import com.dobak.backend.repository.PracticeAttemptRepository;
import com.dobak.backend.repository.ReadingSessionRepository;
import com.dobak.backend.repository.ReportRepository;
import com.dobak.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 리포트. 보호자용(generateLatest)은 최근 7일(이번주) vs 그 이전 7일(지난주)을 비교해서
 * 스냅샷을 저장하고, 아이 본인용(getChildReport)은 매번 최신 상태를 계산만 해서 보여준다.
 */
@Service
public class ReportService {

    private static final int PERIOD_DAYS = 7;
    /** 알 지급 기준. PracticeService.EGG_THRESHOLD와 반드시 같은 값으로 맞춰야 함. */
    private static final double EGG_THRESHOLD = 0.8;
    /** "그날 학습함" 판정 기준 — 발음점검 1회 이상 */
    private static final int MAX_STREAK_LOOKBACK_DAYS = 60;

    private final UserRepository userRepository;
    private final PracticeAttemptRepository practiceAttemptRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final CaptureSentenceRepository captureSentenceRepository;
    private final ReportRepository reportRepository;
    private final ProgressService progressService;

    public ReportService(UserRepository userRepository,
                          PracticeAttemptRepository practiceAttemptRepository,
                          ReadingSessionRepository readingSessionRepository,
                          CaptureSentenceRepository captureSentenceRepository,
                          ReportRepository reportRepository,
                          ProgressService progressService) {
        this.userRepository = userRepository;
        this.practiceAttemptRepository = practiceAttemptRepository;
        this.readingSessionRepository = readingSessionRepository;
        this.captureSentenceRepository = captureSentenceRepository;
        this.reportRepository = reportRepository;
        this.progressService = progressService;
    }

    /**
     * 보호자용 최신 리포트 (메인 화면).
     * 같은 날 다시 호출되면 새 리포트를 또 만들지 않고, 오늘 이미 만들어둔 리포트를 최신 데이터로
     * 갱신만 한다(reportId 유지). 날짜가 바뀌면(자정 넘어가면) 그때 새 리포트가 생긴다.
     */
    public ReportResponse generateLatest(Long childId) {
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이 계정: " + childId));

        LocalDateTime periodEnd = LocalDateTime.now();
        LocalDateTime periodStart = periodEnd.minusDays(PERIOD_DAYS);
        LocalDateTime prevPeriodStart = periodStart.minusDays(PERIOD_DAYS);

        List<PracticeAttempt> attempts = practiceAttemptRepository
                .findByChildIdAndCreatedAtBetween(childId, periodStart, periodEnd);
        List<PracticeAttempt> prevAttempts = practiceAttemptRepository
                .findByChildIdAndCreatedAtBetween(childId, prevPeriodStart, periodStart);
        List<ReadingSession> sessions = readingSessionRepository
                .findByChildIdAndCreatedAtBetween(childId, periodStart, periodEnd);
        List<ErrorPatternSummary> patternCounts = buildPatternCounts(attempts);

        double accuracy = average(attempts);
        double previousAccuracy = average(prevAttempts);

        int learningSessionCount = sessions.size();
        int additionalSentenceCount = attempts.size();
        int weeklyEggGain = countEggGain(attempts);

        String highlight = buildHighlight(attempts.size(), accuracy, previousAccuracy);
        String summary = buildSummary(attempts.size(), accuracy, patternCounts);
        PronunciationComparison comparison = buildComparison(attempts);

        Report report = reportRepository.findFirstByChildIdOrderByGeneratedAtDesc(childId)
                .filter(existing -> existing.getGeneratedAt().toLocalDate().equals(LocalDate.now()))
                .map(existing -> {
                    existing.updateSnapshot(periodStart, periodEnd, highlight, summary,
                            accuracy, previousAccuracy, learningSessionCount, additionalSentenceCount);
                    return existing;
                })
                .orElseGet(() -> new Report(child, periodStart, periodEnd, highlight, summary,
                        accuracy, previousAccuracy, learningSessionCount, additionalSentenceCount));
        reportRepository.save(report);

        return toResponse(report, attempts, patternCounts, comparison, weeklyEggGain);
    }

    /**
     * 리포트 목록에서 과거 리포트 하나를 클릭했을 때의 상세 조회.
     * 저장된 periodStart~periodEnd 기준으로 그 당시 시도들을 다시 집계해서 보여준다
     * (Report 자체엔 attempts/errorPatterns 같은 상세를 안 남기고 요약값만 저장하기 때문).
     */
    public ReportResponse getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리포트: " + reportId));

        List<PracticeAttempt> attempts = practiceAttemptRepository.findByChildIdAndCreatedAtBetween(
                report.getChild().getId(), report.getPeriodStart(), report.getPeriodEnd());
        List<ErrorPatternSummary> patternCounts = buildPatternCounts(attempts);
        PronunciationComparison comparison = buildComparison(attempts);
        int weeklyEggGain = countEggGain(attempts);

        return toResponse(report, attempts, patternCounts, comparison, weeklyEggGain);
    }

    /**
     * 아이 본인용 "나의 읽기" 화면. 스냅샷을 저장하지 않고 항상 최신으로 계산한다.
     * 스트릭/요일별 도장은 "발음점검 1회 이상 = 그날 학습함" 기준으로 판정한다.
     */
    public ChildReportResponse getChildReport(Long childId) {
        userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이 계정: " + childId));

        LocalDateTime periodEnd = LocalDateTime.now();
        LocalDateTime periodStart = periodEnd.minusDays(PERIOD_DAYS);
        LocalDateTime prevPeriodStart = periodStart.minusDays(PERIOD_DAYS);

        List<PracticeAttempt> attempts = practiceAttemptRepository
                .findByChildIdAndCreatedAtBetween(childId, periodStart, periodEnd);
        List<PracticeAttempt> prevAttempts = practiceAttemptRepository
                .findByChildIdAndCreatedAtBetween(childId, prevPeriodStart, periodStart);

        double accuracy = average(attempts);
        double previousAccuracy = average(prevAttempts);
        double delta = round1((accuracy - previousAccuracy) * 100);

        long sentencesRead = captureSentenceRepository
                .countBySession_ChildIdAndSession_CreatedAtBetween(childId, periodStart, periodEnd);

        List<ErrorPatternSummary> patternCounts = buildPatternCounts(attempts);
        int streakDays = calculateStreak(childId);
        List<DayCheckIn> weeklyCheckIns = calculateWeeklyCheckIns(childId);

        ProgressResponse progress = progressService.getProgress(childId);

        String encouragementMessage = buildEncouragementMessage(attempts.size(), accuracy, previousAccuracy, patternCounts);

        return new ChildReportResponse(
                (int) sentencesRead,
                accuracy,
                previousAccuracy,
                delta,
                streakDays,
                progress.eggCount(),
                progress.currentHatchProgress(),
                progress.eggsPerHatch(),
                patternCounts,
                weeklyCheckIns,
                encouragementMessage
        );
    }

    public List<ReportSummary> listReports(Long childId) {
        return reportRepository.findByChildIdOrderByGeneratedAtDesc(childId).stream()
                .map(r -> new ReportSummary(r.getId(), r.getPeriodStart(), r.getPeriodEnd(),
                        r.getHighlight(), r.isStampSent()))
                .toList();
    }

    /** 보호자가 "칭찬 도장 보내기" 눌렀을 때 — 리포트에 전송 여부 기록 + 칭찬도장 누적 카운트 +1 */
    public void sendStamp(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리포트: " + reportId));
        report.sendStamp();
        reportRepository.save(report);
        progressService.addStamp(report.getChild().getId());
    }

    private double average(List<PracticeAttempt> attempts) {
        return attempts.stream().mapToDouble(PracticeAttempt::getAccuracy).average().orElse(0);
    }

    /** 이 기간 동안 알 지급 기준(EGG_THRESHOLD)을 넘긴 시도 수 = 이번 기간에 새로 얻은 알 개수 */
    private int countEggGain(List<PracticeAttempt> attempts) {
        return (int) attempts.stream().filter(a -> a.getAccuracy() >= EGG_THRESHOLD).count();
    }

    /** 발음점검 시도들을 패턴(겹받침/된소리/긴문장 등)별로 집계 — "자주 막힌 유형"/"소리 연습" 차트용 */
    private List<ErrorPatternSummary> buildPatternCounts(List<PracticeAttempt> attempts) {
        Map<String, Long> counts = attempts.stream()
                .filter(a -> a.getPattern() != null)
                .collect(Collectors.groupingBy(PracticeAttempt::getPattern, LinkedHashMap::new, Collectors.counting()));
        return counts.entrySet().stream()
                .map(e -> new ErrorPatternSummary(e.getKey(), e.getValue().intValue()))
                .toList();
    }

    /** 오늘부터 거꾸로 세면서 "발음점검 1회 이상 한 날"이 며칠 연속인지 계산 */
    private int calculateStreak(Long childId) {
        LocalDate day = LocalDate.now();
        int streak = 0;
        while (streak < MAX_STREAK_LOOKBACK_DAYS && hasPracticeOn(childId, day)) {
            streak++;
            day = day.minusDays(1);
        }
        return streak;
    }

    /** 이번 주(월~일) 요일별로 발음점검을 했는지 체크 */
    private List<DayCheckIn> calculateWeeklyCheckIns(Long childId) {
        String[] labels = {"월", "화", "수", "목", "금", "토", "일"};
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);

        List<DayCheckIn> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            result.add(new DayCheckIn(labels[i], hasPracticeOn(childId, day)));
        }
        return result;
    }

    private boolean hasPracticeOn(Long childId, LocalDate day) {
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.plusDays(1).atStartOfDay();
        return !practiceAttemptRepository.findByChildIdAndCreatedAtBetween(childId, start, end).isEmpty();
    }

    private String buildHighlight(int attemptCount, double accuracy, double previousAccuracy) {
        if (attemptCount == 0) {
            return "이번 주에는 아직 연습 기록이 없어요.";
        }
        if (accuracy > previousAccuracy) {
            return "발음 정확도가 지난주보다 좋아졌어요.";
        }
        if (accuracy < previousAccuracy) {
            return "이번 주엔 조금 어려운 문장이 많았어요.";
        }
        return "꾸준히 연습을 이어가고 있어요.";
    }

    private String buildSummary(int attemptCount, double accuracy, List<ErrorPatternSummary> patternCounts) {
        if (attemptCount == 0) {
            return "이번 주에는 아직 발음 연습 기록이 없어요.";
        }
        String topPattern = topPattern(patternCounts);

        return String.format(
                "이번 주 %d회 발음 연습, 평균 정확도 %.0f%%. 가장 자주 막힌 유형: %s.",
                attemptCount, accuracy * 100, topPattern
        );
    }

    private String buildEncouragementMessage(int attemptCount, double accuracy, double previousAccuracy,
                                              List<ErrorPatternSummary> patternCounts) {
        if (attemptCount == 0) {
            return "이번 주에도 소리 연습을 시작해볼까요?";
        }
        String topPattern = topPattern(patternCounts);
        if (accuracy > previousAccuracy) {
            return String.format("이번 주 정말 잘했어요! %s 소리가 눈에 띄게 좋아졌어요.", topPattern);
        }
        return String.format("%s 소리를 조금 더 연습하면 훨씬 좋아질 거예요!", topPattern);
    }

    private String topPattern(List<ErrorPatternSummary> patternCounts) {
        return patternCounts.stream()
                .max((a, b) -> Integer.compare(a.occurrenceCount(), b.occurrenceCount()))
                .map(ErrorPatternSummary::errorType)
                .orElse("아직 뚜렷한 유형 없음");
    }

    /** comparedToAttemptId가 있는 가장 최근 시도 하나를 골라 "지난 시도 vs 이번 시도" 오디오 쌍을 만든다. */
    private PronunciationComparison buildComparison(List<PracticeAttempt> attempts) {
        return attempts.stream()
                .filter(a -> a.getComparedToAttemptId() != null)
                .reduce((a, b) -> a.getCreatedAt().isAfter(b.getCreatedAt()) ? a : b)
                .flatMap(current -> {
                    Optional<PracticeAttempt> previous = practiceAttemptRepository.findById(current.getComparedToAttemptId());
                    return previous.map(prev -> new PronunciationComparison(
                            current.getTargetText(), prev.getAudioFileUrl(), current.getAudioFileUrl()
                    ));
                })
                .orElse(null);
    }

    private ReportResponse toResponse(Report report, List<PracticeAttempt> attempts,
                                       List<ErrorPatternSummary> patternCounts, PronunciationComparison comparison,
                                       int weeklyEggGain) {
        List<PracticeAttemptSummary> attemptSummaries = attempts.stream()
                .map(a -> new PracticeAttemptSummary(a.getId(), a.getTargetText(), a.getAccuracy(), a.getCreatedAt()))
                .toList();
        double delta = round1((report.getAccuracy() - report.getPreviousAccuracy()) * 100);

        return new ReportResponse(
                report.getId(), report.getPeriodStart(), report.getPeriodEnd(),
                report.getHighlight(), report.getSummary(),
                report.getAccuracy(), report.getPreviousAccuracy(), delta,
                report.getLearningSessionCount(), report.getAdditionalSentenceCount(), weeklyEggGain,
                attemptSummaries, patternCounts, comparison, report.isStampSent()
        );
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
