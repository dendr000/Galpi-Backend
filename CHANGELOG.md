# Changelog

이 프로젝트의 주요 변경 사항을 기록한다.
형식은 [Keep a Changelog](https://keepachangelog.com/ko/1.1.0/), 버전은 [유의적 버전](https://semver.org/lang/ko/)을 따른다.
시각은 한국 표준시(KST)이다. (2026-10-09부터 기록 시작 -- 그 이전 변경은 git 커밋 이력을 본다. 아직 버전 번호를 매기지 않아 `[Unreleased]` 에 모은다.)

## [Unreleased]

### 변경 (Changed)
- 개발 서버 기본 포트를 8080에서 18080으로 바꿨다(2026-10-09 12:01). 8080은 Oracle XE 등과 자주 충돌한다. **호환 깨짐** 프론트 개발 서버를 쓴다면 `Galpi-Frontend/.env`의 `VITE_API_BASE_URL`을 `http://localhost:18080`으로 바꾼다.

### 추가 (Added)
- 로그인 게이트, 토큰, 인증 필터, 숨김 분류, 서버 포트 설정의 단위 테스트 42개를 추가했다(`gradlew test`).
