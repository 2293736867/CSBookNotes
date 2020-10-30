use test;
drop table if exists orders;

create table orders(
    id int(10) primary key auto_increment,
    ordersn varchar(10) collate utf8mb4_unicode_ci default null,
    user_id int(10) default null,
    key user_id(user_id),
    constraint user_id foreign key (user_id) references user(id)
);