# --- !Ups

create table User (
    id int primary key auto_increment,
    username varchar(255) not null,
    email varchar(255) not null,
    age int not null
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8;


# --- !Downs

drop table User;