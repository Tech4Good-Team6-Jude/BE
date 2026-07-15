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

`/internal/v1`에 아래 기능이 필요합니다. 실제 엔드포인트 경로가 정해지면 `InferenceClient`
구현체(현재 `MockInferenceClient`)만 새 구현체로 교체할 예정이라, Service Server 쪽 컨트롤러/DTO는
안 건드려도 됩니다. 파일(이미지/오디오)이 껴있는 두 기능(OCR, 발음평가)만 `multipart/form-data`이고
나머지는 JSON입니다.

### 기능 1 · OCR (텍스트 캡처)

호출 시점: `POST /api/v1/captures` (사진 업로드) 처리 중

**AI 모델이 받아야 할 값** — `multipart/form-data`

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `file` | File | O | 촬영/업로드된 이미지 파일 |

**AI 모델이 줘야 할 값**
```json
["옛날 옛적, 넓적한 바위 위에 앉아 있던 개구리가", "훌쩍 뛰어올랐어요.", "연못 건너편에는 낡은 나무 다리가 놓여 있었죠."]
```
| 필드 | 타입 | 설명 |
| --- | --- | --- |
| (배열 원소) | String | OCR로 인식해서 문장 단위로 분할한 텍스트 하나 |

---

### 기능 2 · 문장 단순화

호출 시점: `POST /api/v1/sentences/{id}/explain?level=1~3`(캡처 흐름) 또는 `POST /api/v1/stuck-sentences/{id}/explain?level=1~3`(도서관 L5 반복학습 "이해" 탭) — 재설명 요청. level=0이면 원문 그대로라 이 기능은 호출 안 하고 TTS만 호출

**AI 모델이 받아야 할 값**
```json
{
  "originalText": "참다못한 아기 오리는 집을 떠났어요.",
  "level": 2
}
```
| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `originalText` | String | O | 원본 문장 |
| `level` | Integer | O | 1~3, 클수록 더 쉬운 문장 |

**AI 모델이 줘야 할 값**
```json
{
  "simplifiedText": "너무 힘든 아기 오리는 집을 나갔어요.",
  "keyWords": [
    { "word": "참다못한", "meaning": "더 참을 수 없다는 뜻이에요" }
  ]
}
```
| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `simplifiedText` | String | 지정한 level 난이도로 쉬워진 문장 |
| `keyWords` | List\<Object\> | 원문에서 이 재설명이 필요했던 근거가 된 핵심 단어(들) + 그 뜻. `word`는 FE가 원문 표시 시 하이라이트용, `meaning`은 "'{word}'는 ~라는 뜻이에요" 같은 보조 설명용 (기능명세서 2.2.2 "근거 단서 제공") |
| `keyWords[].word` | String | 근거가 된 단어 |
| `keyWords[].meaning` | String | 그 단어의 뜻 풀이 |

---

### 기능 3 · TTS

호출 시점: 재설명 응답 음성 생성 시(`/explain`), 유사문장 세트의 문장 하나하나마다(`/similar-sets`, `/stuck-sentences/{id}/similar-sets`)

**AI 모델이 받아야 할 값**
```json
{ "text": "아주 쉬운 문장으로 바뀐 텍스트입니다." }
```
| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `text` | String | O | 읽어줄 텍스트 |

**AI 모델이 줘야 할 값**
```json
{
  "audioUrl": "https://example.com/audio/xxxx.mp3",
  "words": [
    { "word": "아주", "startMs": 0, "endMs": 300 },
    { "word": "쉬운", "startMs": 300, "endMs": 600 }
  ]
}
```
| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `audioUrl` | String | 생성된 낭독 음성 파일 URL |
| `words` | List\<Object\> | 단어별 타임스탬프 (하이라이트 동기화용). **word-level timestamp 지원 여부부터 확인 필요** — A모드 하이라이트 기능의 전제조건 |
| `words[].word` | String | 단어 |
| `words[].startMs` / `endMs` | Integer | 이 단어가 재생되는 구간(ms) |

---

### 기능 4 · 단어 풀이

호출 시점: `GET /api/v1/words/{word}/card` (문장 내 단어를 탭했을 때)

**AI 모델이 받아야 할 값**
```json
{ "word": "사과" }
```
| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `word` | String | O | 뜻을 찾을 단어 |

**AI 모델이 줘야 할 값**
```json
{
  "word": "사과",
  "meaning": "새콤달콤한 빨간 과일",
  "example": "사과 하나를 맛있게 먹었어요.",
  "audioUrl": "https://example.com/audio/사과.mp3"
}
```
| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `word` | String | 요청받은 단어 (그대로 반환) |
| `meaning` | String | 뜻 풀이 |
| `example` | String | 예문 |
| `audioUrl` | String | 단어 발음 오디오 URL |

---

### 기능 5 · 발음·유창성 평가

호출 시점: `POST /api/v1/practice/attempts` (발음 연습 녹음 제출)

**AI 모델이 받아야 할 값** — `multipart/form-data`

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `audio` | File | O | 아이가 녹음한 음성 파일 |
| `targetText` | String | O | 읽어야 했던 목표 문장 |

**AI 모델이 줘야 할 값**
```json
{
  "sttText": "나비 구룸 하늘",
  "accuracy": 0.82,
  "mismatches": [
    { "expectedWord": "구름", "heardAs": "구룸", "correctionType": "받침", "modelAudioUrl": "https://example.com/audio/구름.mp3" }
  ],
  "pattern": "겹받침"
}
```
| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `sttText` | String | 녹음을 인식한 텍스트 |
| `accuracy` | Double | 정확도 (0~1) |
| `mismatches` | List\<Object\> | 목표 문장과 다르게 발음된 구간 목록 |
| `mismatches[].expectedWord` | String | 원래 있어야 할 단어 |
| `mismatches[].heardAs` | String | 실제로 들린 발음(텍스트화) |
| `mismatches[].correctionType` | String | 오류 유형 (예: "받침"/"발음"/"띄어읽기") |
| `mismatches[].modelAudioUrl` | String | 이 단어만 정확하게 발음한 시범 음성 URL |
| `pattern` | String | 이 문장이 속한 소리 패턴 ("겹받침"/"된소리"/"긴문장" 등) |

---

### 기능 6 · 유사문장 생성

호출 시점: `POST /api/v1/sentences/{id}/similar-sets`(사진 캡처 흐름), `POST /api/v1/stuck-sentences/{id}/similar-sets`(도서관 흐름) — 둘 다 막힌 문장 기반 반복학습 세트를 만들 때

**AI 모델이 받아야 할 값**
```json
{
  "sourceText": "널찍한 바위에 앉았어요.",
  "count": 4,
  "difficulty": "보통"
}
```
| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `sourceText` | String | O | 막힌 원문 |
| `count` | Integer | O | 생성할 유사문장 개수 (보통 3~5) |
| `difficulty` | String | O | "쉬움" / "보통" |

**AI 모델이 줘야 할 값**
```json
{
  "pattern": "겹받침",
  "sentences": ["넓적한 돌을 밟았다.", "책을 읽고 앉았다.", "값이 너무 비싸다.", "여덟 개를 세었다."]
}
```
| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `pattern` | String | sourceText에서 뽑아낸 공통 소리 패턴명 |
| `sentences` | List\<String\> | 같은 패턴의 유사문장 목록 (count개) |

---

### 기능 7 · 문장 패턴 분석

호출 시점: `POST /api/v1/books/sentences/{id}/stuck` (도서관 읽기 중 문장을 "막힘"으로 표시할 때)

**AI 모델이 받아야 할 값**
```json
{ "text": "널찍한 바위에 앉았어요." }
```
| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `text` | String | O | 막힌 문장 텍스트 |

**AI 모델이 줘야 할 값**
```json
{ "pattern": "겹받침" }
```
| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `pattern` | String | 소리 패턴 태그 — "겹받침" / "된소리" / "긴문장" / "일반" |

---

### 참고: 선언만 돼 있고 아직 어디서도 호출 안 되는 기능 (우선순위 낮음)

| 기능 | 현재 Mock 인터페이스 | 비고 |
|---|---|---|
| STT(단독) | `stt(MultipartFile audioFile) -> String` | 발음평가(기능 5)가 STT 결과(sttText)까지 같이 반환해서 지금은 단독 호출 지점이 없음 |
| 오류 유형 분석 | `analyzeErrorType(List<String> texts) -> { errorType, description }` | 예전 진단(B모드) 화면용으로 설계됐던 것으로 보이는데 현재 컨트롤러/서비스에 연결 안 돼 있음 |

## FE팀에게

지금 이 mock 응답 그대로 화면 개발 시작해도 됩니다. 실제 AI 연동 후에도 필드명/구조는
유지할 예정이라 나중에 큰 수정 없이 붙을 수 있을 거예요.
