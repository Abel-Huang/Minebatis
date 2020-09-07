create table student(
    id BIGINT(13) NOT NULL,
    name VARCHAR(120) NOT NULL,
    address VARCHAR(200) NOT NULL,
    age BIGINT(13) NOT NULL,
    PRIMARY KEY (id)
)
ENGINE=InnoDB;

insert into student(id, name, address, age) values (1, 'Abel', 'No1. Street', 12);
insert into student(id, name, address, age) values (2, 'Cindy', 'Green Park', 11);
insert into student(id, name, address, age) values (3, 'Judy', 'Central Avenue', 12);
insert into student(id, name, address, age) values (4, 'Bob', 'No1. Street', 13);