# --- !Ups

alter table User drop column email;
create table Contact (
    id int primary key not null auto_increment,
    contactType varchar(255) not null,
    contact varchar(255) not null,
    userId int not null
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8;


# --- !Downs

alter table User add column email varchar(255) not null;
drop table Contact;