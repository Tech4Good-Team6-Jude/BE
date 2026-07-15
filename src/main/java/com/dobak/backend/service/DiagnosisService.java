package com.dobak.backend.service;

import com.dobak.backend.dto.DiagnosisQuestion;
import com.dobak.backend.dto.DiagnosisResult;
import com.dobak.backend.dto.DiagnosisSubmitRequest;
import com.dobak.backend.dto.ReadingScoreResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class DiagnosisService {

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

    // TODO(AI팀 연동): 답변 패턴 분석 -> 오류 유형 분류 로직/모델 호출
    public DiagnosisResult submit(DiagnosisSubmitRequest request) {
        return new DiagnosisResult(
                "phonological",
                "음운 처리에 어려움이 관찰됩니다. 음운 인식 훈련부터 시작하는 것을 권장해요.",
                "level_1"
        );
    }

    // TODO(AI팀 연동): STT로 음성 -> 텍스트 변환 후 정답과 비교, 오독 단어 추출
    public ReadingScoreResponse scoreReading(MultipartFile audio) {
        return new ReadingScoreResponse(
                0.82,
                List.of("나비", "구름"),
                "전반적으로 잘 읽었어요! '나비'와 '구름' 발음을 다시 연습해봐요."
        );
    }
}
