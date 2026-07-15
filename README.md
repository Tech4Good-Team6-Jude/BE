# 또박또박 백엔드 (Service Server)

난독증 지원 서비스 "또박또박" 백엔드 서버 (Spring Boot). 실제 구현 기준 전체 구조:

```
Service Server: /api/v1  (이 레포)
  ├─ 사용자·도서관·게임학습·발음연습·진행률·리포트·리워드 DB (H2, 파일 기반)
  ├─ 파일 저장소 (로컬 디스크, ./uploads, /files/** 로 서빙)
  └─ Inference Server 호출: /internal/v1  (AI팀 서버, hana-skt-ai 레포)
        - inference.provider=mock: MockInferenceClient (AI 서버 없이 개발용, 고정 응답)
        - inference.provider=http: HttpInferenceClient (실제 AI 서버 호출, 현재 기본값)
```

## 요구사항
- JDK 17 이상
- Maven
- (선택) AI 추론 서버(hana-skt-ai)를 같이 띄우려면 해당 레포 참고 — 없어도 `inference.provider: mock`으로 바꾸면 백엔드 단독 실행 가능

## 실행
```bash
mvn spring-boot:run
```
`http://localhost:8080` 에서 뜸. 확인: `GET /api/v1/health` -> `ok`

DB는 프로젝트 루트에 `./data/dobak.mv.db` 파일로 생성됨 (H2 file 모드, 재시작해도 데이터 유지).
H2 콘솔: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/dobak`)

## AI 추론 서버 연동
`application.yml`의 `inference.provider` 값으로 전환한다.
- `mock`: AI 서버 없이 개발/데모용, 고정된 가짜 응답만 내려줌
- `http`: 실제 AI 서버(hana-skt-ai, FastAPI, `/internal/v1`) 호출. 주소는 `inference.service.base-url`(기본값 `http://localhost:8001/internal/v1`)

`http` 모드로 쓰려면 `hana-skt-ai`를 별도로 띄워야 하고, 그 레포 폴더 안에 `.env`를 만들어 `PROVIDER=real`, `GEMINI_API_KEY`를 직접 설정해야 한다. `.env`는 두 레포 모두 git에 올라가지 않으므로(민감정보 보호 목적) **로컬마다 각자 따로 설정**해야 하고, git pull로는 절대 전달되지 않는다.

AI 서버가 TTS 오디오를 자기 host 기준 상대경로(`/internal/v1/speech/audio/{filename}`)로 내려주는데, AI 서버에는 CORS 설정이 없어 브라우저에서 그 주소로 직접 접근하면 재생이 막힐 수 있다. 그래서 백엔드가 `TtsAudioProxyController`(`GET /api/v1/tts-audio/{filename}`)로 한 번 중계해서 내려준다 — 프론트는 다른 `/files/**` 리소스와 동일하게 항상 백엔드(`localhost:8080`) 기준 상대경로로만 오디오 URL을 받는다고 생각하면 된다.

## 프로젝트 구조
```
src/main/java/com/dobak/backend/
  controller/    # REST 엔드포인트 (/api/v1/*)
  service/       # 비즈니스 로직
  entity/        # JPA 엔티티 (User, Book, BookSentence, StuckSentence, WordMatchPair, HatchReward, Progress, Report 등)
  repository/    # Spring Data JPA 리포지토리
  inference/     # AI팀 Inference Server(/internal/v1) 호출 경계
    InferenceClient.java       # 인터페이스
    MockInferenceClient.java   # mock 구현체 (provider=mock, fallback으로도 쓰임)
    HttpInferenceClient.java   # 실제 AI 서버 호출 구현체 (provider=http)
  dto/           # 요청/응답 모델
  storage/       # 파일 저장 (로컬 디스크)
  config/        # CORS, 시드 데이터 등 설정
```

## 현재 상태
AI팀 Inference Server(hana-skt-ai) 연동 완료 — OCR / TTS / STT / 문장단순화 / 유사문장생성은 실제 AI 서버를 호출한다. 단어풀이·오류유형분석·문장패턴분석은 AI 서버에 대응 엔드포인트가 없어 `HttpInferenceClient`가 의도적으로 `MockInferenceClient`에 위임하도록 되어 있다(코드 내 주석 참고).

인증/인가는 별도 구현이 없고, 사전에 등록된 테스트 계정 식별자(`childId`, `guardianId`)를 요청 파라미터로 그대로 전달하는 방식이다.

전체 엔드포인트의 요청/응답 필드 상세는 `또박또박_API_명세서.docx` 또는 `openapi.yaml`(Swagger, 서버 실행 중 `/v3/api-docs.yaml`에서 재생성 가능) 참고.

## 엔드포인트 요약 (대표 예시)
| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | /api/v1/health | 헬스체크 |
| GET | /api/v1/children/{childId} | 아동 프로필 조회 |
| POST | /api/v1/captures | 사진 업로드 → OCR → 문장 분할 |
| POST | /api/v1/sentences/{sentenceId}/explain | 문장 재설명 + TTS (캡처 흐름) |
| POST | /api/v1/stuck-sentences/{stuckSentenceId}/explain | 문장 재설명 + TTS (도서관 흐름) |
| GET | /api/v1/words/{word}/card | 단어 뜻풀이 카드 |
| POST | /api/v1/sentences/{sentenceId}/similar-sets | 유사문장 세트 생성 |
| POST | /api/v1/practice/attempts | 발음 연습 녹음 제출 → 정확도 평가 |
| GET | /api/v1/children/{childId}/progress | 학습 진행률(알/부화) 조회 |
| GET | /api/v1/children/{childId}/report | 보호자용 리포트 |
| GET | /api/v1/books | 도서 목록 |
| POST | /api/v1/books/sentences/{sentenceId}/stuck | 막힌 문장 표시/해제 |
| GET | /api/v1/games/word-match/round | 짝맞추기 라운드 조회 |
| GET | /api/v1/rewards/catalog | 리워드 카탈로그 조회 |
| POST | /api/v1/hatch-rewards/{hatchRewardId}/claim | 리워드 수령 처리 |
| GET | /api/v1/tts-audio/{filename} | AI 서버 TTS 오디오 프록시 |

## TODO
- [ ] 단어풀이/오류유형분석/문장패턴분석 AI 서버 엔드포인트가 추가되면 HttpInferenceClient에 실제 연동으로 교체
- [ ] 인증/비밀번호 처리 (현재 미구현, 해커톤 단계 한정)
- [ ] 파일 저장소 S3 등으로 교체 검토 (현재 로컬 디스크)
- [ ] AI 서버 자체에 CORS 설정 추가되면 백엔드의 TTS 오디오 프록시 제거 검토
