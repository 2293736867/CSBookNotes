use test;

create table product(
    id int(10) primary key auto_increment,
    name varchar(10) collate utf8mb4_unicode_ci default null,
    price double default null
);

create table orders_detail(
    id int(10) primary key auto_increment,
    orders_id int(10) default null,
    product_id int(10) default null,
    key orders_id(orders_id),
    key product_id(product_id),
    constraint orders_id foreign key (orders_id) references orders(id),
    constraint product_id foreign key (product_id) references product(id)
);