package com.dobak.backend.service;

import com.dobak.backend.dto.EggRewardInfo;
import com.dobak.backend.dto.PracticeAttemptResponse;
import com.dobak.backend.dto.ProgressResponse;
import com.dobak.backend.entity.PracticeAttempt;
import com.dobak.backend.entity.User;
import com.dobak.backend.inference.InferenceClient;
import com.dobak.backend.inference.dto.PronunciationEvalResult;
import com.dobak.backend.repository.PracticeAttemptRepository;
import com.dobak.backend.repository.UserRepository;
import com.dobak.backend.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PracticeService {

    /** 알(리워드) 지급 기준 — 이 이상이어야 알 +1 */
    private static final double EGG_THRESHOLD = 0.8;
    /** "관대한 채점" 통과 기준(4.2.2) — 좌절감을 줄이기 위해 알 지급 기준보다 낮게 잡음 */
    private static final double LENIENT_PASS_THRESHOLD = 0.6;

    private final InferenceClient inferenceClient;
    private final PracticeAttemptRepository practiceAttemptRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ProgressService progressService;

    public PracticeService(InferenceClient inferenceClient,
                            PracticeAttemptRepository practiceAttemptRepository,
                            UserRepository userRepository,
                            FileStorageService fileStorageService,
                            ProgressService progressService) {
        this.inferenceClient = inferenceClient;
        this.practiceAttemptRepository = practiceAttemptRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.progressService = progressService;
    }

    /**
     * 발음 연습 1회 제출: 녹음 저장 -> Inference Server(발음평가) 호출 -> 이전 시도와 비교 가능하도록 저장.
     * 정확도가 알 지급 기준을 넘으면 알 +1, "관대한 채점" 기준을 넘으면 통과(passed) 처리.
     * 정확도는 곧바로 저장하지 않고 Progress의 누적 평균에 반영한다.
     */
    public PracticeAttemptResponse submit(Long childId, String targetText, MultipartFile audio, Long compareToAttemptId) {
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이 계정: " + childId));

        String audioFileUrl = fileStorageService.save(audio, "practice-audio");
        PronunciationEvalResult evalResult = inferenceClient.evaluatePronunciation(audio, targetText);

        PracticeAttempt attempt = new PracticeAttempt(
                child, targetText, audioFileUrl, evalResult.sttText(), evalResult.accuracy(),
                evalResult.pattern(), compareToAttemptId
        );
        practiceAttemptRepository.save(attempt);

        boolean passed = evalResult.accuracy() >= LENIENT_PASS_THRESHOLD;

        EggRewardInfo eggReward;
        if (evalResult.accuracy() >= EGG_THRESHOLD) {
            eggReward = progressService.addEgg(childId);
        } else {
            ProgressResponse current = progressService.getProgress(childId);
            eggReward = EggRewardInfo.none(current.currentHatchProgress(), current.eggsPerHatch());
        }
        progressService.recordAccuracy(childId, evalResult.accuracy());

        String feedback = buildFeedback(evalResult, passed);

        return new PracticeAttemptResponse(
                attempt.getId(), evalResult.sttText(), evalResult.accuracy(), passed, evalResult.mismatches(),
                feedback, eggReward
        );
    }

    private String buildFeedback(PronunciationEvalResult result, boolean passed) {
        if (result.mismatches().isEmpty()) {
            return "완벽하게 읽었어요!";
        }
        if (!passed) {
            return "조금 더 천천히 다시 읽어볼까요? 하나씩 같이 연습해봐요.";
        }
        String words = result.mismatches().stream()
                .map(m -> m.expectedWord())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return "전반적으로 잘 읽었어요! " + words + " 발음을 다시 연습해봐요.";
    }
}
