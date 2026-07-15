package com.dobak.backend.service;

import com.dobak.backend.dto.CaptureResponse;
import com.dobak.backend.dto.CaptureSentenceSummary;
import com.dobak.backend.entity.CaptureSentence;
import com.dobak.backend.entity.ReadingSession;
import com.dobak.backend.entity.User;
import com.dobak.backend.inference.InferenceClient;
import com.dobak.backend.repository.CaptureSentenceRepository;
import com.dobak.backend.repository.ReadingSessionRepository;
import com.dobak.backend.repository.UserRepository;
import com.dobak.backend.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 기능명세서 1번(텍스트 캡처 및 문장 추출). 이미지를 업로드하면
 * OCR로 문장 단위 분할해서 목록으로 돌려주고(1.2.1), 이후 이해보조 등 다음 단계는
 * 사용자가 그 목록 중 하나를 선택/수정한 뒤(1.2.2) 별도로 호출한다.
 */
@Service
public class CaptureService {

    private final InferenceClient inferenceClient;
    private final ReadingSessionRepository readingSessionRepository;
    private final CaptureSentenceRepository captureSentenceRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public CaptureService(InferenceClient inferenceClient,
                           ReadingSessionRepository readingSessionRepository,
                           CaptureSentenceRepository captureSentenceRepository,
                           UserRepository userRepository,
                           FileStorageService fileStorageService) {
        this.inferenceClient = inferenceClient;
        this.readingSessionRepository = readingSessionRepository;
        this.captureSentenceRepository = captureSentenceRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    public CaptureResponse createCapture(Long childId, MultipartFile file) {
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이 계정: " + childId));

        String imageUrl = fileStorageService.save(file, "captures");
        List<String> sentences = inferenceClient.ocr(file);

        ReadingSession session = new ReadingSession(child, imageUrl);
        readingSessionRepository.save(session);

        List<CaptureSentence> entities = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i++) {
            entities.add(new CaptureSentence(session, i, sentences.get(i)));
        }
        captureSentenceRepository.saveAll(entities);

        return toResponse(session, entities);
    }

    /** OCR 오인식 문장을 사용자가 직접 수정 (1.2.2) */
    public CaptureSentenceSummary editSentence(Long sentenceId, String newText) {
        CaptureSentence sentence = captureSentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문장: " + sentenceId));
        sentence.edit(newText);
        captureSentenceRepository.save(sentence);
        return toSummary(sentence);
    }

    private CaptureResponse toResponse(ReadingSession session, List<CaptureSentence> sentences) {
        List<CaptureSentenceSummary> summaries = sentences.stream()
                .map(this::toSummary)
                .toList();
        return new CaptureResponse(session.getId(), session.getImageUrl(), summaries);
    }

    private CaptureSentenceSummary toSummary(CaptureSentence sentence) {
        return new CaptureSentenceSummary(sentence.getId(), sentence.getOrderIndex(), sentence.getEffectiveText());
    }
}
