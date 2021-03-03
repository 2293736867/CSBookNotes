use test;

drop table if exists idcard;
drop table if exists person;

create table idcard(
    id int(10) primary key auto_increment,
    code char(18) collate utf8mb4_unicode_ci default null
);

create table person(
    id int(10) primary key,
    name varchar(20) collate utf8mb4_unicode_ci default null,
    age smallint default null,
    idcard_id int(10) default null,
    key idcard_id(idcard_id),
    constraint idcard_id foreign key (idcard_id) references idcard(id)
);