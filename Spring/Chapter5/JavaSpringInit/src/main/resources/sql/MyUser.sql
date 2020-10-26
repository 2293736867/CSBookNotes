USE test;

DROP TABLE IF EXISTS MyUser;

create table MyUser(
    id INT AUTO_INCREMENT PRIMARY KEY ,
    uname varchar(20),
    usex varchar(20)
)