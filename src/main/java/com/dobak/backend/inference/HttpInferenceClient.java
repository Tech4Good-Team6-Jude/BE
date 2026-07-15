package com.dobak.backend.inference;

import com.dobak.backend.dto.KeyWordDetail;
import com.dobak.backend.dto.MismatchSegment;
import com.dobak.backend.dto.WordTimestamp;
import com.dobak.backend.inference.dto.ErrorTypeAnalysisResult;
import com.dobak.backend.inference.dto.PronunciationEvalResult;
import com.dobak.backend.inference.dto.SimilarSentenceGenerationResult;
import com.dobak.backend.inference.dto.SimplifyResult;
import com.dobak.backend.inference.dto.TtsResult;
import com.dobak.backend.inference.dto.WordCardResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * AI팀 Inference Server(/internal/v1, FastAPI)를 실제로 호출하는 구현체.
 * application.yml의 inference.provider=http 일 때만 활성화되고, MockInferenceClient보다 우선한다
 * (provider=mock 이면 이 빈은 아예 생성되지 않고 기존 MockInferenceClient가 그대로 쓰임).
 *
 * AI 서버에 대응 엔드포인트가 없는 3개(단어풀이/오류유형분석/문장패턴분석)와 진단 관련 기능은
 * 의도적으로 fallback(MockInferenceClient)에 위임한다 — 나중에 AI팀이 엔드포인트를 추가하면
 * 이 클래스에서 해당 메서드만 실제 호출로 바꾸면 되고, 지금은 필수값이 아니게 처리하는 게 목적.
 *
 * 필드 매핑 관련 TODO(AI팀과 확인 필요):
 * - simplify의 level(BE 1~3, 클수록 쉬움) <-> target_reading_level(AI 1~10) 방향 확인 필요.
 *   지금은 "숫자가 클수록 더 쉬운 문장"이라는 표준 리딩레벨 관례를 가정해 역방향으로 매핑함.
 * - text/similar, reading/evaluate 응답엔 pattern(소리 패턴 태그) 필드가 없어서 BE가
 *   MockInferenceClient.analyzeSentencePattern()의 한글 자모 분해 로직으로 직접 채운다.
 * - reading/evaluate 응답엔 단어별 시범 발음 오디오(modelAudioUrl)가 없어서 null로 내려간다.
 */
@Service
@ConditionalOnProperty(prefix = "inference", name = "provider", havingValue = "http")
@Primary
public class HttpInferenceClient implements InferenceClient {

    private final RestClient restClient;
    private final MockInferenceClient fallback;

    public HttpInferenceClient(RestClient.Builder restClientBuilder,
                                @Value("${inference.service.base-url}") String baseUrl,
                                MockInferenceClient fallback) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.fallback = fallback;
    }

    @Override
    public List<String> ocr(MultipartFile file) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", toResource(file));
        body.add("language", "ko");
        body.add("include_bounding_boxes", "true");

        OcrHttpResponse response = restClient.post()
                .uri("/ocr")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(OcrHttpResponse.class);

        String text = response != null ? response.text() : "";
        return splitIntoSentences(text);
    }

    @Override
    public SimplifyResult simplify(String originalText, int level) {
        int targetReadingLevel = mapToAiReadingLevel(level);
        SimplifyHttpRequest request = new SimplifyHttpRequest(originalText, targetReadingLevel, true);

        SimplifyHttpResponse response = restClient.post()
                .uri("/text/simplify")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(SimplifyHttpResponse.class);

        if (response == null) {
            return new SimplifyResult(originalText, List.of());
        }
        List<KeyWordDetail> keyWords = response.difficultWords() == null
                ? List.of()
                : response.difficultWords().stream()
                        .map(dw -> new KeyWordDetail(dw.word(), dw.meaning()))
                        .toList();
        return new SimplifyResult(response.simplifiedText(), keyWords);
    }

    @Override
    public TtsResult tts(String text) {
        TtsHttpRequest request = new TtsHttpRequest(text, "female-01", 0.8, true);

        TtsHttpResponse response = restClient.post()
                .uri("/speech/synthesize")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TtsHttpResponse.class);

        if (response == null) {
            return new TtsResult(null, List.of());
        }
        List<WordTimestamp> words = response.timings() == null
                ? List.of()
                : response.timings().stream()
                        .map(t -> new WordTimestamp(t.text(), t.startMs(), t.endMs()))
                        .toList();
        return new TtsResult(response.audioUrl(), words);
    }

    @Override
    public String stt(MultipartFile audioFile) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", toResource(audioFile));
        body.add("language", "ko-KR");

        SttHttpResponse response = restClient.post()
                .uri("/speech/transcribe")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(SttHttpResponse.class);

        return response != null ? response.transcript() : "";
    }

    @Override
    public ErrorTypeAnalysisResult analyzeErrorType(List<String> unknownTexts) {
        // AI 서버에 대응 엔드포인트 없음(진단 기능 범위 밖) — 필요해지기 전까지 mock 유지
        return fallback.analyzeErrorType(unknownTexts);
    }

    @Override
    public PronunciationEvalResult evaluatePronunciation(MultipartFile audioFile, String targetText) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", toResource(audioFile));
        body.add("expected_text", targetText);
        body.add("language", "ko-KR");

        ReadingEvalHttpResponse response = restClient.post()
                .uri("/reading/evaluate")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(ReadingEvalHttpResponse.class);

        if (response == null) {
            return new PronunciationEvalResult("", 0.0, List.of(), fallback.analyzeSentencePattern(targetText));
        }

        double accuracy = response.scores() != null ? response.scores().accuracy() / 100.0 : 0.0;
        List<MismatchSegment> mismatches = response.wordResults() == null
                ? List.of()
                : response.wordResults().stream()
                        .filter(w -> !"CORRECT".equals(w.status()))
                        .map(w -> new MismatchSegment(w.expected(), w.spoken(), mapStatusToCorrectionType(w.status()), null))
                        .toList();

        // AI 응답엔 소리 패턴(겹받침/된소리/긴문장) 태그가 없어서 BE의 자모 분해 로직으로 직접 채움
        String pattern = fallback.analyzeSentencePattern(targetText);

        return new PronunciationEvalResult(response.transcript(), accuracy, mismatches, pattern);
    }

    @Override
    public WordCardResult lookupWord(String word) {
        // AI 서버에 대응 엔드포인트 없음 — 필요해지기 전까지 mock 유지
        return fallback.lookupWord(word);
    }

    @Override
    public SimilarSentenceGenerationResult generateSimilarSentences(String sourceText, int count, String difficulty) {
        SimilarHttpRequest request = new SimilarHttpRequest(sourceText, count, difficulty);

        SimilarHttpResponse response = restClient.post()
                .uri("/text/similar")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(SimilarHttpResponse.class);

        List<String> sentences = response != null && response.similarSentences() != null
                ? response.similarSentences()
                : List.of();
        // AI 응답엔 패턴명이 없어서 BE의 자모 분해 로직으로 직접 채움
        String pattern = fallback.analyzeSentencePattern(sourceText);
        return new SimilarSentenceGenerationResult(pattern, sentences);
    }

    @Override
    public String analyzeSentencePattern(String text) {
        // AI 서버에 대응 엔드포인트 없음 — 지금 mock 로직 자체가 한글 자모 실분해라 그대로 사용
        return fallback.analyzeSentencePattern(text);
    }

    /** AI의 word_results[].status(CORRECT/MISPRONOUNCED/OMITTED/INSERTED) -> Mock이 쓰던 correctionType 문자열 근사 매핑 */
    private String mapStatusToCorrectionType(String status) {
        if (status == null) {
            return "발음";
        }
        return switch (status) {
            case "OMITTED" -> "생략";
            case "INSERTED" -> "삽입";
            default -> "발음";
        };
    }

    /** BE의 level(1~3, 클수록 쉬움) -> AI의 target_reading_level(1~10, 표준 관례상 작을수록 쉬움) 역방향 매핑 */
    private int mapToAiReadingLevel(int level) {
        int clamped = Math.max(1, Math.min(level, 3));
        return Math.max(1, 10 - clamped * 3);
    }

    private String[] splitIntoSentencesRaw(String text) {
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            return new String[0];
        }
        return normalized.split("(?<=[.!?])\\s+");
    }

    /** OCR이 문장 분할까지 해주지 않아서(전체 텍스트만 줌) BE에서 마침표/물음표/느낌표 기준으로 직접 분리 */
    private List<String> splitIntoSentences(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(splitIntoSentencesRaw(text))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private ByteArrayResource toResource(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (IOException e) {
            throw new IllegalStateException("업로드 파일을 읽을 수 없습니다: " + file.getOriginalFilename(), e);
        }
    }

    // ---- AI 서버 JSON 계약과 매핑되는 내부 전용 DTO (snake_case) ----

    private record SimplifyHttpRequest(String text,
                                        @JsonProperty("target_reading_level") int targetReadingLevel,
                                        @JsonProperty("include_definitions") boolean includeDefinitions) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SimplifyHttpResponse(@JsonProperty("original_text") String originalText,
                                         @JsonProperty("simplified_text") String simplifiedText,
                                         String explanation,
                                         @JsonProperty("difficult_words") List<DifficultWordHttp> difficultWords) {
    }

    private record DifficultWordHttp(String word, String meaning) {
    }

    private record TtsHttpRequest(String text,
                                   @JsonProperty("voice_id") String voiceId,
                                   double speed,
                                   @JsonProperty("include_timings") boolean includeTimings) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TtsHttpResponse(@JsonProperty("audio_url") String audioUrl,
                                    @JsonProperty("audio_format") String audioFormat,
                                    @JsonProperty("duration_ms") long durationMs,
                                    List<WordTimingHttp> timings) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WordTimingHttp(String text,
                                   @JsonProperty("start_ms") long startMs,
                                   @JsonProperty("end_ms") long endMs) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OcrHttpResponse(String text) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SttHttpResponse(String transcript, double confidence, List<WordTimingHttp> words) {
    }

    private record SimilarHttpRequest(String text, int count, String difficulty) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SimilarHttpResponse(@JsonProperty("original_text") String originalText,
                                        @JsonProperty("similar_sentences") List<String> similarSentences) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ReadingEvalHttpResponse(String transcript,
                                            ReadingScoresHttp scores,
                                            @JsonProperty("word_results") List<WordEvaluationHttp> wordResults,
                                            String feedback) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ReadingScoresHttp(int accuracy, int fluency, int completeness, int pronunciation,
                                      @JsonProperty("words_per_minute") double wordsPerMinute) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WordEvaluationHttp(String expected, String spoken, String status, int score,
                                       @JsonProperty("weak_phoneme") String weakPhoneme) {
    }
}
