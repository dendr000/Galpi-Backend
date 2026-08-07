SHOW CREATE TABLE note;

CREATE TABLE `note` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tb_character` (
  `char_id` bigint NOT NULL AUTO_INCREMENT COMMENT ''등장인물 고유 식별자'',
  `work_id` bigint NOT NULL COMMENT ''소속 작품의 고유 식별자 (FK)'',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `age` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `birthday` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gender` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `species` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT ''프로필 이미지 코드/경로'',
  `dynamic_properties` json DEFAULT NULL COMMENT ''동적 속성 데이터 (JSON 형태)'',
  PRIMARY KEY (`char_id`),
  KEY `fk_character_work` (`work_id`),
  CONSTRAINT `fk_character_work` FOREIGN KEY (`work_id`) REFERENCES `tb_work` (`work_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1307 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT=''등장인물 정보 및 동적 속성 테이블'';

CREATE TABLE `tb_character_tag_map` (
  `char_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`char_id`,`tag_id`),
  KEY `tag_id` (`tag_id`),
  CONSTRAINT `tb_character_tag_map_ibfk_1` FOREIGN KEY (`char_id`) REFERENCES `tb_character` (`char_id`) ON DELETE CASCADE,
  CONSTRAINT `tb_character_tag_map_ibfk_2` FOREIGN KEY (`tag_id`) REFERENCES `tb_tag_master` (`tag_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tb_system_preset` (
  `preset_id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `preset_value` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`preset_id`),
  UNIQUE KEY `uq_category_value` (`category`,`preset_value`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tb_tag_master` (
  `tag_id` bigint NOT NULL AUTO_INCREMENT,
  `tag_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tag_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `uq_type_name` (`tag_type`,`tag_name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tb_work` (
  `work_id` bigint NOT NULL AUTO_INCREMENT COMMENT ''작품 고유 식별자'',
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT ''작품 제목'',
  `check_date` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `creator` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `genre` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rating` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci COMMENT ''작품 소개 및 줄거리'',
  PRIMARY KEY (`work_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT=''작품 기본 정보 테이블'';

CREATE TABLE `tb_work_tag_map` (
  `work_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`work_id`,`tag_id`),
  KEY `tag_id` (`tag_id`),
  CONSTRAINT `tb_work_tag_map_ibfk_1` FOREIGN KEY (`work_id`) REFERENCES `tb_work` (`work_id`) ON DELETE CASCADE,
  CONSTRAINT `tb_work_tag_map_ibfk_2` FOREIGN KEY (`tag_id`) REFERENCES `tb_tag_master` (`tag_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;