# 또박또박 백엔드

난독증 지원 서비스 "또박또박" 백엔드 서버 (Spring Boot).

## 요구사항
- JDK 17 이상
- Maven (또는 IDE 내장 Maven)

## 실행
```bash
mvn spring-boot:run
```
서버는 `http://localhost:8080` 에서 뜹니다.

확인: `GET http://localhost:8080/api/health` -> `ok`

## 프로젝트 구조
```
src/main/java/com/dobak/backend/
  controller/   # REST 엔드포인트
  service/      # 비즈니스 로직 (현재 mock, AI팀 연동 지점 TODO 표시)
  dto/          # 요청/응답 모델 (record)
  config/       # CORS 등 설정
```

## 현재 상태
모든 엔드포인트는 mock 데이터를 반환합니다. 상세 요청/응답 형식은
[API_CONTRACT.md](./API_CONTRACT.md) 참고. AI팀 파이프라인이 준비되면
`SimplifyService`, `DiagnosisService` 내부만 실제 호출로 교체합니다.

## 엔드포인트 요약
| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | /api/health | 헬스체크 |
| POST | /api/simplify | (A모드) 이미지 업로드 -> 쉬운 문장 + TTS + 하이라이트 타임스탬프 |
| GET | /api/diagnose/questions | (B모드) 진단 문항 5개 조회 |
| POST | /api/diagnose/submit | (B모드) 진단 답변 제출 -> 오류 유형 분류 |
| POST | /api/diagnose/reading-score | (B모드) 낭독 녹음 제출 -> STT 채점 |

## TODO
- [ ] AI팀 OCR/LLM/TTS 연동 (SimplifyService)
- [ ] AI팀 오류유형분류/STT 연동 (DiagnosisService)
- [ ] 파일 저장소 결정 (로컬 vs S3 등) — 업로드 이미지/오디오 저장용
- [ ] 진단 문항 실제 콘텐츠로 교체
