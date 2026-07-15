package com.dobak.backend.service;

import com.dobak.backend.dto.DiagnosisAnswer;
import com.dobak.backend.dto.DiagnosisQuestion;
import com.dobak.backend.dto.DiagnosisResult;
import com.dobak.backend.dto.DiagnosisSubmitRequest;
import com.dobak.backend.entity.ErrorPattern;
import com.dobak.backend.entity.User;
import com.dobak.backend.inference.InferenceClient;
import com.dobak.backend.inference.dto.ErrorTypeAnalysisResult;
import com.dobak.backend.repository.ErrorPatternRepository;
import com.dobak.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiagnosisService {

    private final InferenceClient inferenceClient;
    private final ErrorPatternRepository errorPatternRepository;
    private final UserRepository userRepository;

    public DiagnosisService(InferenceClient inferenceClient,
                             ErrorPatternRepository errorPatternRepository,
                             UserRepository userRepository) {
        this.inferenceClient = inferenceClient;
        this.errorPatternRepository = errorPatternRepository;
        this.userRepository = userRepository;
    }

    // TODO: 실제 진단 문항으로 교체 (음운/시각/글자뒤집힘 등 오류유형 판별용 5문항)
    public List<DiagnosisQuestion> getQuestions() {
        return List.of(
                new DiagnosisQuestion(1, "다음 단어를 소리내어 읽어보세요: 나비", "read_aloud"),
                new DiagnosisQuestion(2, "다음 두 글자 중 같은 소리로 시작하는 것을 고르세요.", "phoneme_match"),
                new DiagnosisQuestion(3, "빠르게 지나가는 글자를 보고 맞는 것을 고르세요.", "visual_span"),
                new DiagnosisQuestion(4, "다음 문장을 읽고 이해한 내용을 답하세요.", "comprehension"),
                new DiagnosisQuestion(5, "다음 글자와 똑같이 생긴 글자를 고르세요. (b/d 구분 등)", "letter_reversal")
        );
    }

    /**
     * 진단 답변 제출 -> Inference Server(오류유형분석)에 답변 텍스트를 넘겨 오류유형을 받고,
     * 그 아이의 ErrorPattern(공통분모)을 누적/갱신한다.
     */
    public DiagnosisResult submit(DiagnosisSubmitRequest request) {
        User child = userRepository.findById(request.childId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이 계정: " + request.childId()));

        List<String> answerTexts = request.answers().stream().map(DiagnosisAnswer::answer).toList();
        ErrorTypeAnalysisResult analysis = inferenceClient.analyzeErrorType(answerTexts);

        ErrorPattern pattern = errorPatternRepository
                .findByChildIdAndErrorType(child.getId(), analysis.errorType())
                .orElseGet(() -> new ErrorPattern(child, analysis.errorType(), 0));
        pattern.incrementOccurrence();
        errorPatternRepository.save(pattern);

        return new DiagnosisResult(analysis.errorType(), analysis.description(), "level_1");
    }
}
