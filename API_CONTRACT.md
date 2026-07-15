# 또박또박 API 계약

Base URL: `http://localhost:8080`

## 전체 구조 (멘토 피드백 반영)

```
Service Server: /api/v1   (이 레포, 우리가 만드는 것)
  ├─ 사용자·세션·진단·학습·리포트 DB
  ├─ 파일 저장소 (녹음/이미지)
  └─ Inference Server 호출: /internal/v1  (AI팀 서버)
         ├─ OCR
         ├─ STT
         ├─ TTS
         ├─ 문장 단순화 LLM
         ├─ 오류 유형 분석
         └─ 발음·유창성 평가
```

현재 `InferenceClient`는 `MockInferenceClient`로 동작해서 위 6개 기능 모두 mock 데이터를 반환합니다.
AI팀 서버(`/internal/v1`) 계약이 확정되면 `InferenceClient`의 새 구현체(HTTP 호출)로 갈아끼울 예정이고,
이 문서의 `/api/v1/*` 엔드포인트 계약 자체는 바뀌지 않으므로 FE는 지금 바로 붙어서 개발하면 됩니다.

---

## 인증 (간이 버전, 해커톤용)

### POST /api/v1/auth/signup
```json
{ "email": "child1@test.com", "password": "1234", "name": "민준", "role": "CHILD" }
```
`role`: `CHILD` | `GUARDIAN`

응답: `{ "userId": 1, "name": "민준", "role": "CHILD" }`

### POST /api/v1/auth/login
```json
{ "email": "child1@test.com", "password": "1234" }
```
응답은 signup과 동일한 형태. 별도 토큰 없이 `userId`를 클라이언트가 들고 있다가 이후 요청에 실어보내는 방식(보안 강화는 TODO).

### POST /api/v1/auth/link
보호자 계정과 아이 계정 연결.
```json
{ "guardianId": 2, "childId": 1 }
```

---

## A모드 — 즉시 보조 모드

### POST /api/v1/simplify
사진/PDF 업로드 -> 쉬운 문장 + 낭독 음성 + 하이라이트 타임스탬프. `ReadingSession`으로 저장됨.

**요청**: `multipart/form-data`
| key | type |
|---|---|
| childId | number |
| file | file |

**응답**
```json
{
  "sessionId": 10,
  "originalText": "OCR 원본 텍스트",
  "simplifiedText": "쉬운 문장으로 바뀐 텍스트입니다.",
  "audioUrl": "https://example.com/mock-audio.mp3",
  "words": [
    { "word": "쉬운", "startMs": 0, "endMs": 400 }
  ]
}
```

### POST /api/v1/sessions/{sessionId}/explain
읽던 중 드래그로 선택한 부분을 쉬운 문장으로 재설명 + TTS. `UnknownWordQuery`로 저장되어
오류 유형(공통분모) 분석의 재료가 됨.

**요청**
```json
{ "selectedText": "모르는 단어나 문장" }
```
**응답**
```json
{
  "queryId": 5,
  "selectedText": "모르는 단어나 문장",
  "explanation": "쉽게 설명한 문장",
  "audioUrl": "https://example.com/mock-audio.mp3"
}
```

---

## B모드 — 적응형 훈련 모드

### GET /api/v1/diagnose/questions
3분 진단용 문항 5개 조회.

### POST /api/v1/diagnose/submit
```json
{
  "childId": 1,
  "answers": [{ "questionId": 1, "answer": "..." }]
}
```
응답: `{ "errorType": "phonological", "description": "...", "recommendedLevel": "level_1" }`
→ 내부적으로 `ErrorPattern`에 누적됨.

### POST /api/v1/practice/attempts
발음 연습(듀오링고식 따라읽기) 녹음 제출. 정확도 80% 이상이면 포도알 자동 지급.

**요청**: `multipart/form-data`
| key | type | 설명 |
|---|---|---|
| childId | number | |
| targetText | string | 읽어야 할 목표 문장 |
| audio | file | 녹음 파일 |
| compareToAttemptId | number (optional) | 이전 시도와 비교 |

**응답**
```json
{
  "attemptId": 7,
  "sttText": "나비 구룸 하늘",
  "accuracy": 0.82,
  "misreadWords": ["구름"],
  "feedback": "전반적으로 잘 읽었어요! 구름 발음을 다시 연습해봐요."
}
```

---

## 보호자용

### GET /api/v1/children/{childId}/report
최근 7일 발음 연습 이력 + 오류 유형 요약 리포트.

```json
{
  "reportId": 3,
  "periodStart": "2026-07-08T00:00:00",
  "periodEnd": "2026-07-15T00:00:00",
  "summary": "이번 주 4회 발음 연습, 평균 정확도 82%. 가장 자주 나타난 오류 유형: phonological.",
  "attempts": [{ "attemptId": 7, "targetText": "...", "accuracy": 0.82, "createdAt": "..." }],
  "errorPatterns": [{ "errorType": "phonological", "occurrenceCount": 3 }]
}
```

### GET /api/v1/children/{childId}/progress
포도알 현황. `POST .../progress/grape`로 수동 지급도 가능(테스트용).

```json
{ "grapeCount": 23, "currentBunchCount": 3, "totalBunchesCompleted": 2, "grapesPerBunch": 10 }
```

---

## AI팀에게

`/internal/v1`에 아래 6개 기능이 필요합니다. 실제 엔드포인트 경로/요청·응답 형식이 정해지면
`InferenceClient` 구현체만 교체할 예정이니 알려주세요.

| 기능 | 현재 Mock 인터페이스 |
|---|---|
| OCR | `ocr(MultipartFile file) -> String` |
| 문장 단순화 | `simplify(String originalText) -> String` |
| TTS | `tts(String text) -> { audioUrl, words[{word,startMs,endMs}] }` |
| STT | `stt(MultipartFile audioFile) -> String` |
| 오류 유형 분석 | `analyzeErrorType(List<String> texts) -> { errorType, description }` |
| 발음·유창성 평가 | `evaluatePronunciation(MultipartFile audio, String targetText) -> { sttText, accuracy, misreadWords[] }` |

**특히 TTS는 word-level timestamp 지원 여부부터 확인 필요** (A모드 하이라이트의 전제조건).

## FE팀에게

지금 이 mock 응답 그대로 화면 개발 시작해도 됩니다. 실제 AI 연동 후에도 필드명/구조는
유지할 예정이라 나중에 큰 수정 없이 붙을 수 있을 거예요.
