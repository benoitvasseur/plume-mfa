DROP TABLE IF EXISTS `PLM_MFA_AUTHENTICATOR`;

CREATE TABLE  `PLM_MFA_AUTHENTICATOR` (
  `id` bigint(20) NOT NULL,
  `id_user` bigint(20) NOT NULL,
  `secret_key` varchar(255) DEFAULT NULL,
  `creation_date` datetime NOT NULL,
  `last_used_date` datetime DEFAULT NULL,
  `is_enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
