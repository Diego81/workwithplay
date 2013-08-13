# --- !Ups

create table UserToUserGroup (
    userid int,
    userGroupid int,
    PRIMARY KEY(userId, userGroupId)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8;


# --- !Downs

drop table UserToUserGroup;
