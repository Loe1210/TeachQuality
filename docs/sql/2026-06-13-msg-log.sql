CREATE TABLE IF NOT EXISTS `msg_log` (
  `id` BIGINT NOT NULL,
  `topic` VARCHAR(64) NOT NULL,
  `tag` VARCHAR(64) NOT NULL,
  `destination` VARCHAR(128) NOT NULL,
  `payload` TEXT NOT NULL,
  `sender_id` INT DEFAULT NULL,
  `object_id` BIGINT DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-pending 1-success 2-fail',
  `retry_count` INT NOT NULL DEFAULT 0,
  `next_retry_time` DATETIME DEFAULT NULL,
  `error_message` VARCHAR(500) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_msg_log_status_retry_time` (`status`, `next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='可靠消息发送日志';
