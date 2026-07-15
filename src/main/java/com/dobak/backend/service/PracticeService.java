package com.dobak.backend.service;

import com.dobak.backend.dto.PracticeAttemptResponse;
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
     * 정확도가 일정 기준을 넘으면 포도알 하나 지급.
     */
    public PracticeAttemptResponse submit(Long childId, String targetText, MultipartFile audio, Long compareToAttemptId) {
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이 계정: " + childId));

        String audioFileUrl = fileStorageService.save(audio, "practice-audio");
        PronunciationEvalResult evalResult = inferenceClient.evaluatePronunciation(audio, targetText);

        PracticeAttempt attempt = new PracticeAttempt(
                child, targetText, audioFileUrl, evalResult.sttText(), evalResult.accuracy(), compareToAttemptId
        );
        practiceAttemptRepository.save(attempt);

        if (evalResult.accuracy() >= 0.8) {
            progressService.addGrape(childId);
        }

        String feedback = buildFeedback(evalResult);

        return new PracticeAttemptResponse(
                attempt.getId(), evalResult.sttText(), evalResult.accuracy(), evalResult.misreadWords(), feedback
        );
    }

    private String buildFeedback(PronunciationEvalResult result) {
        if (result.misreadWords().isEmpty()) {
            return "완벽하게 읽었어요!";
        }
        return "전반적으로 잘 읽었어요! " + String.join(", ", result.misreadWords()) + " 발음을 다시 연습해봐요.";
    }
}
