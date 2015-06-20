drop table if exists person;

create table person(first varchar(255), last varchar(255), title varchar(255)) ENGINE=MyISAM;

insert into person values('Mark', 'Florence', 'Mr');
insert into person values('Lucky', 'Florence', 'Cat');
insert into person values('Lynn', 'Hendrickson', 'Ms');
insert into person values('Max', 'Hendrickson', 'Cat');

drop table if exists title;

create table title(title varchar(255), description varchar(255)) ENGINE=MyISAM;

insert into title values('Cat', 'I am a cat');
insert into title values('Mr', 'I am a man');
insert into title values('Ms', 'I am a woman');
