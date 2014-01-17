# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table `demands` (`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,`user_email` VARCHAR(254) NOT NULL,`amount` INTEGER NOT NULL,`perall` VARCHAR(254) NOT NULL,`description` VARCHAR(254) NOT NULL,`recipients` VARCHAR(254) NOT NULL);
create table `users` (`email` VARCHAR(254) NOT NULL PRIMARY KEY,`name` VARCHAR(254) NOT NULL,`pass` VARCHAR(254) NOT NULL,`role` VARCHAR(254));
alter table `demands` add constraint `user_demand_fk` foreign key(`user_email`) references `users`(`email`) on update NO ACTION on delete NO ACTION;

# --- !Downs

ALTER TABLE demands DROP FOREIGN KEY user_demand_fk;
drop table `demands`;
drop table `users`;

