# 또박또박 백엔드 (Service Server)

난독증 지원 서비스 "또박또박" 백엔드 서버 (Spring Boot). 멘토 피드백 기준 전체 구조:

```
Service Server: /api/v1  (이 레포)
  ├─ 사용자·세션·진단·학습·리포트 DB (H2, 파일 기반)
  ├─ 파일 저장소 (로컬 디스크, ./uploads)
  └─ Inference Server 호출: /internal/v1  (AI팀 서버, 현재는 Mock)
```

## 요구사항
- JDK 17 이상
- Maven

## 실행
```bash
mvn spring-boot:run
```
`http://localhost:8080` 에서 뜸. 확인: `GET /api/v1/health` -> `ok`

DB는 프로젝트 루트에 `./data/dobak.mv.db` 파일로 생성됨 (H2 file 모드, 재시작해도 데이터 유지).
H2 콘솔: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/dobak`)

## 프로젝트 구조
```
src/main/java/com/dobak/backend/
  controller/    # REST 엔드포인트 (/api/v1/*)
  service/       # 비즈니스 로직
  entity/        # JPA 엔티티 (User, ReadingSession, PracticeAttempt, Report, Progress 등)
  repository/    # Spring Data JPA 리포지토리
  inference/     # AI팀 Inference Server(/internal/v1) 호출 경계
    InferenceClient.java       # 인터페이스
    MockInferenceClient.java   # 현재 구현체 (mock)
  dto/           # 요청/응답 모델
  storage/       # 파일 저장 (로컬 디스크)
  config/        # CORS 등 설정
```

## 현재 상태
`InferenceClient`가 `MockInferenceClient`로 동작 중 — OCR/TTS/STT/문장단순화/오류유형분석/발음평가 전부 mock.
AI팀 서버(`/internal/v1`)가 준비되면 `InferenceClient`의 새 구현체(HTTP 호출)를 추가하고 그쪽으로 교체하면 되고,
컨트롤러/서비스/DTO는 그대로 유지됨. 상세 계약은 [API_CONTRACT.md](./API_CONTRACT.md) 참고.

## 엔드포인트 요약
| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | /api/v1/health | 헬스체크 |
| POST | /api/v1/auth/signup | 회원가입 (아이/보호자) |
| POST | /api/v1/auth/login | 로그인 |
| POST | /api/v1/auth/link | 보호자-아이 계정 연결 |
| POST | /api/v1/simplify | (A모드) 이미지 업로드 -> 쉬운 문장+TTS+하이라이트, ReadingSession 생성 |
| POST | /api/v1/sessions/{id}/explain | (A모드) 드래그한 부분 재설명 + TTS |
| GET | /api/v1/diagnose/questions | (B모드) 진단 문항 5개 |
| POST | /api/v1/diagnose/submit | (B모드) 진단 제출 -> 오류유형 분류, ErrorPattern 누적 |
| POST | /api/v1/practice/attempts | (B모드) 발음 연습 녹음 제출 -> 정확도 평가, 포도알 지급 |
| GET | /api/v1/children/{id}/report | 보호자용 리포트 |
| GET | /api/v1/children/{id}/progress | 포도알 현황 |
| POST | /api/v1/children/{id}/progress/grape | 포도알 수동 지급(테스트용) |

## TODO
- [ ] AI팀 Inference Server(/internal/v1) 실제 계약 확정 후 HttpInferenceClient 구현
- [ ] 진단 문항 실제 콘텐츠로 교체
- [ ] 비밀번호 해시 처리 (현재 평문, 해커톤 단계 한정)
- [ ] 파일 저장소 S3 등으로 교체 검토 (현재 로컬 디스크)
- [ ] 포도알/판 기준치(GRAPES_PER_BUNCH=10) 기획 확정
