# 문제 해결 기록

## 2026-11-10 exe 에서 메모 화면(/memo)을 새로고침하면 오류 화면이 뜬다

- **증상**: exe(패키징된 앱)에서 메모 화면을 연 상태로 새로고침(F5, 우클릭 "새로고침")하거나 `/memo` 주소로 바로 들어가면 오류 화면이 뜬다. 개발 서버(Vite)에서는 보이지 않는다.
- **원인**: 화면 주소 폴백(`WebConfig`)이 `/memo` 만 따로 `forward:/memo.html` 로 넘기는데 `memo.html` 이 없다. 게다가 `MemoViewController` 가 같은 주소에서 템플릿이 없는 뷰 `memo` 를 돌려줬다(더 높은 우선순위). 다른 화면(`/work/15` 등)은 `index.html` 로 넘어가 정상이었다. 코드를 읽어서 찾았고, 시험으로 `/memo` 만 `/memo.html` 로 넘어가는 것을 재현했다.
- **해결**: `/memo` 전용 설정과 `MemoViewController` 를 지웠다. 이제 `/memo` 도 확장자 없는 다른 주소와 같이 `index.html` 로 넘어가 React Router 가 처리한다. `WebConfigRoutingTest` 가 `/`, `/memo`, 작품, 분류, 편집기, 일괄, 뷰어 주소를 확인한다.
- **재발 방지**: 새 화면 주소를 만들 때 백엔드에 같은 주소의 컨트롤러나 별도 forward 를 두지 않는다(폴백이 처리한다). 주소 규칙은 `WebConfigRoutingTest` 에 추가해 확인한다.
