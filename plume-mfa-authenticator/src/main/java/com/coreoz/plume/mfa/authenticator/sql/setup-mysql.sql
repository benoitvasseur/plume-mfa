DROP TABLE IF EXISTS `PLM_MFA_AUTHENTICATOR`;

CREATE TABLE  `PLM_MFA_AUTHENTICATOR` (
  `id` bigint(20) NOT NULL,
  `id_user` bigint(20) NOT NULL,
  `secret_key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
