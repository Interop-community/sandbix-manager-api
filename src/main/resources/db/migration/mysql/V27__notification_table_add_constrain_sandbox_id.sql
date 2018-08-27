CREATE TABLE notification (
  id                  INT(11)   NOT NULL AUTO_INCREMENT,
  created_timestamp   DATETIME  DEFAULT NULL,
  user_id             INT(11)   DEFAULT NULL,
  news_item_id        INT(11)   DEFAULT NULL,
  seen                BIT(1)    NOT NULL,
  hidden              BIT(1)    NOT NULL,
  PRIMARY KEY (id),
  KEY (user_id),
  KEY (news_item_id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id),
  CONSTRAINT FOREIGN KEY (news_item_id) REFERENCES news_item (id)
);

ALTER TABLE sandbox ADD UNIQUE (sandbox_id);

