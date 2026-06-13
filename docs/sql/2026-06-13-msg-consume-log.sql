CREATE TABLE IF NOT EXISTS `msg_consume_log` (
  `id` BIGINT NOT NULL,
  `msg_key` VARCHAR(255) NOT NULL,
  `msg_id` VARCHAR(128) NOT NULL,
  `topic` VARCHAR(64) NOT NULL,
  `tag` VARCHAR(64) DEFAULT NULL,
  `consumer_group` VARCHAR(64) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-processing 1-success 2-fail',
  `retry_count` INT NOT NULL DEFAULT 0,
  `error_message` VARCHAR(500) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_msg_consume_log_msg_key` (`msg_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ消费幂等日志';
