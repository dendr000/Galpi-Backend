import java.sql.*;
import java.util.*;

public class Migrate {

    // FK 의존관계 순서 — 부모 테이블부터
    // tb_work_tag_map / tb_character_tag_map는 MySQL엔 있지만 어떤 JPA 엔티티도
    // 참조하지 않아 Hibernate가 H2에 테이블을 만들지 않는다. 원본에도 0행이라
    // 마이그레이션 대상에서 제외해도 데이터 손실 없음.
    static final String[] TABLES = {
        "tb_work", "tb_character", "tb_tag_master",
        "tb_hidden_category", "tb_wiki_page", "tb_boilerplate", "tb_system_preset",
        "tb_dict", "tb_auth_gate", "galpi_memo", "galpi_memo_relation"
    };

    // 테이블별 auto-increment PK 컬럼 (identity 시퀀스 재조정용). 없으면 null.
    static final Map<String, String> IDENTITY_COL = new HashMap<>();
    static {
        IDENTITY_COL.put("tb_work", "work_id");
        IDENTITY_COL.put("tb_character", "char_id");
        IDENTITY_COL.put("tb_tag_master", "tag_id");
        IDENTITY_COL.put("tb_hidden_category", "id");
        IDENTITY_COL.put("tb_wiki_page", "page_id");
        IDENTITY_COL.put("tb_boilerplate", "id");
        IDENTITY_COL.put("tb_system_preset", "preset_id");
        IDENTITY_COL.put("tb_dict", "id");
        IDENTITY_COL.put("galpi_memo", "id");
        IDENTITY_COL.put("galpi_memo_relation", "id");
        // tb_work_tag_map, tb_character_tag_map: 복합 PK, auto-increment 없음
        // tb_auth_gate: theme_key(문자열) PK, auto-increment 없음
    }

    public static void main(String[] args) throws Exception {
        String mysqlUrl = "jdbc:mysql://localhost:3306/note?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useUnicode=true";
        String mysqlUser = System.getenv().getOrDefault("DB_USERNAME", "root");
        String mysqlPass = System.getenv("DB_PASSWORD");
        if (mysqlPass == null) {
            System.err.println("DB_PASSWORD 환경변수가 필요합니다 (Galpi-Backend/.env 참고).");
            System.exit(1);
        }
        String h2Home = System.getProperty("user.home").replace('\\', '/');
        String h2Url = "jdbc:h2:file:" + h2Home + "/.galpi/data/galpi";

        try (Connection my = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection h2 = DriverManager.getConnection(h2Url, "sa", "")) {

            h2.setAutoCommit(false);

            // tb_system_preset: 실제 데이터(45행: CUP_SIZE/ENNEAGRAM/MBTI 프리셋)는 있지만
            // 이 테이블을 쓰는 JPA 엔티티가 코드에 없어 Hibernate가 만들지 않는다.
            // 지금 앱이 안 읽는 데이터지만 버리지 않고 수동으로 테이블을 만들어 옮겨둔다.
            try (Statement st = h2.createStatement()) {
                st.execute(
                    "CREATE TABLE IF NOT EXISTS tb_system_preset (" +
                    "preset_id BIGINT NOT NULL PRIMARY KEY, " +
                    "category CHARACTER VARYING(50) NOT NULL, " +
                    "preset_value CHARACTER VARYING(100) NOT NULL)"
                );
            }

            // tb_auth_gate는 앱이 @PostConstruct에서 빈 테마마다 랜덤 값을 자동 시드해두므로,
            // 실제 값을 넣기 전에 그 자동 시드분부터 비운다.
            try (Statement st = h2.createStatement()) {
                st.execute("DELETE FROM tb_auth_gate");
            }

            for (String table : TABLES) {
                long copied = copyTable(my, h2, table);
                System.out.println(table + " : " + copied + " rows copied");
            }

            h2.commit();

            System.out.println("=== identity 시퀀스 재조정 ===");
            for (Map.Entry<String, String> e : IDENTITY_COL.entrySet()) {
                fixIdentity(h2, e.getKey(), e.getValue());
            }
            h2.commit();

            System.out.println("=== 검증: 행 개수 비교 ===");
            for (String table : TABLES) {
                long myCount = count(my, table);
                long h2Count = count(h2, table);
                String mark = (myCount == h2Count) ? "OK" : "MISMATCH!!";
                System.out.println(table + " : mysql=" + myCount + " h2=" + h2Count + " [" + mark + "]");
            }
        }
    }

    // MySQL JSON 컬럼 — H2 쪽에 INSERT할 때 일반 ?가 아니라 "? FORMAT JSON"으로 바인딩해야
    // 문자열이 JSON 문자열 리터럴로 이중 인코딩되지 않는다(setString만으로는 안 됨 — 직접 확인함).
    static final Set<String> JSON_COLUMNS = Set.of("tb_character.dynamic_properties");

    static long copyTable(Connection my, Connection h2, String table) throws SQLException {
        try (Statement s = my.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM " + table)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            List<String> cols = new ArrayList<>();
            for (int i = 1; i <= colCount; i++) cols.add(meta.getColumnName(i));

            StringBuilder sb = new StringBuilder("INSERT INTO ").append(table).append(" (");
            sb.append(String.join(", ", cols));
            sb.append(") VALUES (");
            for (int i = 0; i < colCount; i++) {
                boolean isJson = JSON_COLUMNS.contains(table + "." + cols.get(i));
                sb.append(i == 0 ? "" : ", ").append(isJson ? "? FORMAT JSON" : "?");
            }
            sb.append(")");

            long total = 0;
            int batch = 0;
            try (PreparedStatement ps = h2.prepareStatement(sb.toString())) {
                while (rs.next()) {
                    for (int i = 1; i <= colCount; i++) {
                        Object val = rs.getObject(i);
                        if (val instanceof String) {
                            ps.setString(i, (String) val);
                        } else {
                            ps.setObject(i, val);
                        }
                    }
                    ps.addBatch();
                    batch++;
                    total++;
                    if (batch >= 500) {
                        ps.executeBatch();
                        batch = 0;
                    }
                }
                if (batch > 0) ps.executeBatch();
            }
            return total;
        }
    }

    static void fixIdentity(Connection h2, String table, String col) throws SQLException {
        try (Statement st = h2.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(" + col + "), 0) + 1 FROM " + table);
            rs.next();
            long next = rs.getLong(1);
            st.execute("ALTER TABLE " + table + " ALTER COLUMN " + col + " RESTART WITH " + next);
            System.out.println(table + "." + col + " -> RESTART WITH " + next);
        }
    }

    static long count(Connection conn, String table) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
