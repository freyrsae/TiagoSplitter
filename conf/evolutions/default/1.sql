# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table `contacts` (`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,`kennitala` VARCHAR(254) NOT NULL,`name` VARCHAR(254) NOT NULL,`contactEmail` VARCHAR(254) NOT NULL,`userEmail` VARCHAR(254) NOT NULL);
create table `demands` (`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,`user_email` VARCHAR(254) NOT NULL,`amount` INTEGER NOT NULL,`description` VARCHAR(254) NOT NULL,`status` VARCHAR(254) NOT NULL,`timestamp` TIMESTAMP NOT NULL);
create table `recipients` (`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,`demandId` BIGINT NOT NULL,`name` VARCHAR(254) NOT NULL,`amount` INTEGER NOT NULL,`paid` BOOLEAN NOT NULL);
create table `users` (`email` VARCHAR(254) NOT NULL PRIMARY KEY,`name` VARCHAR(254) NOT NULL,`pass` VARCHAR(254) NOT NULL,`kennitala` VARCHAR(254) NOT NULL,`account_no` VARCHAR(254) NOT NULL,`role` VARCHAR(254));
alter table `contacts` add constraint `user_contact_fk` foreign key(`userEmail`) references `users`(`email`) on update NO ACTION on delete NO ACTION;
alter table `demands` add constraint `user_demand_fk` foreign key(`user_email`) references `users`(`email`) on update NO ACTION on delete NO ACTION;
alter table `recipients` add constraint `demand_recipient_fk` foreign key(`demandId`) references `demands`(`id`) on update NO ACTION on delete NO ACTION;

# --- !Downs

ALTER TABLE contacts DROP FOREIGN KEY user_contact_fk;
ALTER TABLE demands DROP FOREIGN KEY user_demand_fk;
ALTER TABLE recipients DROP FOREIGN KEY demand_recipient_fk;
drop table `contacts`;
drop table `demands`;
drop table `recipients`;
drop table `users`;

