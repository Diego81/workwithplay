# --- !Ups

create table UserGroup (
    id int primary key auto_increment,
    name varchar(255) not null,
    description text not null
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8;


# --- !Downs

drop table UserGroup;
