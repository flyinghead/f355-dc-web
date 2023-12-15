create database f355;
use f355;
create table `player` (id integer not null auto_increment, reg_id varchar(16) UNIQUE KEY, name varchar(12), score_name varchar(3), country varchar(2), reg_data BLOB,
	created datetime(6), last_seen datetime(6), created_ip varchar(255), last_seen_ip varchar(255), primary key (id));
create table `result` (id integer not null auto_increment, player_id integer, circuit integer, semi_auto tinyint, run_date timestamp(6),
	race_mode integer, tuned tinyint, assisted tinyint, arcade tinyint, run_time integer, data_path varchar(255), primary key (id));
alter table `result` add constraint result_player_constraint foreign key (player_id) references player (id);
create index result_index on result (circuit, semi_auto, player_id);

GRANT INSERT, UPDATE, DELETE, SELECT on f355.* TO 'f355'@'localhost' WITH GRANT OPTION;
