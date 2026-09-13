# MySQL → H2 데이터 마이그레이션

exe(Electron) 패키징용 H2 DB로 실제 운영 데이터를 옮기는 일회성 스크립트.
2026-09-14에 한 번 실행해 검증까지 마쳤음 — 그 뒤로 실제 데이터가 더
쌓였다면(작품/메모 추가 등), **배포 직전에 한 번 더 실행**해서 최신
상태로 다시 옮기면 된다.

## 실행 전 확인

1. `Galpi-Backend`에서 `exe` 프로파일로 한 번 기동해 깨끗한 스키마를
   만들어둔다(기존 H2 파일이 있다면 먼저 지운다):
   ```
   rm ~/.galpi/data/galpi.*
   ./gradlew bootRun --args="--spring.profiles.active=exe"
   ```
   기동 확인 후 종료.
2. 실제 운영 MySQL(`note` 스키마)이 `localhost:3306`에서 접속 가능해야 함.

## 실행

MySQL 커넥터와 H2 드라이버가 클래스패스에 필요하다 (Gradle 캐시에서
찾아 이 폴더로 복사해두거나, 경로를 직접 지정). DB 계정 정보는
`Galpi-Backend/.env`와 같은 값을 환경변수로 넘긴다:

```
DB_USERNAME=root DB_PASSWORD=<.env의 값> java -cp "mysql-connector-j-*.jar;h2-*.jar" Migrate.java
```

끝나면 행 개수 비교 결과가 전부 `[OK]`인지 확인한다.

## 알아둘 것

- **`tb_work_tag_map`, `tb_character_tag_map`은 마이그레이션 대상에서
  제외**했다 — 어떤 JPA 엔티티도 참조하지 않는 죽은 테이블이고 원본도
  0행이라 데이터 손실 없음.
- **`tb_system_preset`**(캐릭터 프리셋: CUP_SIZE/ENNEAGRAM/MBTI, 45행)은
  마찬가지로 JPA 엔티티가 없어 Hibernate가 테이블을 안 만들지만, 실제
  데이터가 있어서 스크립트가 직접 `CREATE TABLE`부터 하고 옮긴다.
- **JSON 컬럼(`tb_character.dynamic_properties`)은 일반 `?` 바인딩으로
  넣으면 H2가 이중 인코딩한다** — 반드시 `? FORMAT JSON`으로 넣어야
  한다(스크립트에 이미 반영됨). 이걸 빼먹으면 저장된 값이
  `"{\"a\":\"b\"}"`처럼 통째로 문자열 하나로 감싸진다.
- **`CHAR_LENGTH()`로 MySQL과 H2를 비교하면 이모지 등 대리쌍(surrogate
  pair) 문자가 있는 글에서 항상 몇 글자씩 차이 나는 것처럼 보인다** —
  H2는 Java의 UTF-16 코드 유닛 기준, MySQL은 실제 유니코드 문자 기준으로
  세기 때문. 실제 내용이 같은지는 길이가 아니라 문자열 자체(해시나
  equals)로 비교해야 한다.
- 마이그레이션은 단일 트랜잭션이라, 중간에 실패하면 그때까지 옮긴 것도
  전부 롤백된다(DDL로 만든 테이블 자체는 예외 — H2는 DDL을 즉시 커밋).
  실패하면 원인만 고치고 그대로 다시 실행하면 된다.
- 미디어 파일(`Galpi-Media/img`, `Galpi-Media/fonts`)은 이 스크립트
  범위 밖 — 그냥 `~/.galpi/media/img`, `~/.galpi/media/fonts`로
  복사하면 된다.
