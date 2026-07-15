# 또박또박 API 계약 (초안)

Base URL: `http://localhost:8080`

현재 모든 엔드포인트는 **mock 데이터**를 반환합니다. AI팀 파이프라인이 준비되는 대로
`SimplifyService` / `DiagnosisService` 내부 로직만 교체되고, 아래 요청/응답 형식(계약)은
그대로 유지될 예정이니 FE는 지금 바로 붙어서 개발하면 됩니다.

---

## 공통

### GET /api/health
서버 상태 확인용.

응답: `"ok"` (200)

---

## A모드 — 즉시 보조 모드

### POST /api/simplify
사진/PDF 이미지를 업로드하면 쉬운 문장 + 낭독 음성 + 하이라이트용 타임스탬프를 반환.

**요청**: `multipart/form-data`
| key | type | 설명 |
|---|---|---|
| file | file | 업로드한 이미지/PDF |

**응답 (200)**
```json
{
  "originalText": "OCR로 추출된 원본 텍스트",
  "simplifiedText": "쉬운 문장으로 바뀐 텍스트입니다.",
  "audioUrl": "https://example.com/mock-audio.mp3",
  "words": [
    { "word": "쉬운", "startMs": 0, "endMs": 400 },
    { "word": "문장으로", "startMs": 400, "endMs": 900 },
    { "word": "바뀐", "startMs": 900, "endMs": 1200 },
    { "word": "텍스트입니다.", "startMs": 1200, "endMs": 1800 }
  ]
}
```

FE는 `words` 배열의 `startMs`/`endMs`와 오디오 재생 시간을 비교해서 현재 읽는 단어를
하이라이트하면 됨 (노래방식).

---

## B모드 — 적응형 훈련 모드 (해커톤 MVP: 얇게 구현)

### GET /api/diagnose/questions
3분 진단용 문항 5개 조회.

**응답 (200)**
```json
[
  { "id": 1, "prompt": "다음 단어를 소리내어 읽어보세요: 나비", "type": "read_aloud" },
  { "id": 2, "prompt": "...", "type": "phoneme_match" },
  { "id": 3, "prompt": "...", "type": "visual_span" },
  { "id": 4, "prompt": "...", "type": "comprehension" },
  { "id": 5, "prompt": "...", "type": "letter_reversal" }
]
```

### POST /api/diagnose/submit
진단 답변 제출 -> 오류 유형 분류 결과.

**요청**
```json
{
  "answers": [
    { "questionId": 1, "answer": "..." },
    { "questionId": 2, "answer": "..." }
  ]
}
```

**응답 (200)**
```json
{
  "errorType": "phonological",
  "description": "음운 처리에 어려움이 관찰됩니다. 음운 인식 훈련부터 시작하는 것을 권장해요.",
  "recommendedLevel": "level_1"
}
```
`errorType`: `phonological` | `visual` | `letter_reversal` 등 (AI팀과 확정 필요)

### POST /api/diagnose/reading-score
소리내어 읽기 녹음 제출 -> STT 채점/피드백.

**요청**: `multipart/form-data`
| key | type | 설명 |
|---|---|---|
| audio | file | 녹음 파일 |

**응답 (200)**
```json
{
  "accuracy": 0.82,
  "misreadWords": ["나비", "구름"],
  "feedback": "전반적으로 잘 읽었어요! '나비'와 '구름' 발음을 다시 연습해봐요."
}
```

---

## AI팀에게

이 문서의 엔드포인트/응답 필드는 초안입니다. 각 파이프라인(OCR/LLM재작성/TTS/STT/오류유형분류)의
실제 입출력 형태가 정해지면 알려주세요 — 백엔드 서비스 레이어(`SimplifyService`,
`DiagnosisService`)에 그대로 반영해서 FE에는 동일한 계약을 유지한 채 실제 데이터로 교체합니다.

특히 TTS 쪽은 **단어별 타임스탬프(word-level timing)를 반환하는 API인지 먼저 확인**해주세요
(Google Cloud TTS, Azure Speech는 지원; OpenAI TTS는 미지원). 이게 A모드 하이라이트 기능의
핵심 전제라 API 선택에 따라 계약이 바뀔 수 있습니다.

## FE팀에게

지금 이 mock 응답 그대로 화면 개발 시작해도 됩니다. 실제 AI 연동 후에도 필드명/구조는
유지할 예정이라 나중에 큰 수정 없이 붙을 수 있을 거예요.
