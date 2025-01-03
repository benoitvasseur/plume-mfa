DROP TABLE IF EXISTS `PLM_MFA_BROWSER`;

CREATE TABLE  `PLM_MFA_BROWSER` (
  `id` bigint(20) NOT NULL,
  `id_user` bigint(20) NOT NULL,
  `key_id` BLOB NOT NULL,
  `public_key_cose` BLOB NOT NULL,
  `attestation` BLOB NOT NULL,
  `client_data_json` BLOB NOT NULL,
  `user_handle` BLOB DEFAULT NULL,
  `is_discoverable` tinyint(1) DEFAULT NULL,
  `creation_date` datetime NOT NULL,
  `last_used_date` datetime DEFAULT NULL,
  `signature_count` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;