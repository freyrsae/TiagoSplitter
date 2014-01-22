# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "contacts" ("id" bigserial NOT NULL PRIMARY KEY,"kennitala" VARCHAR NOT NULL,"name" VARCHAR NOT NULL,"userEmail" VARCHAR NOT NULL);
create table "demands" ("id" bigserial NOT NULL PRIMARY KEY,"user_email" VARCHAR NOT NULL,"amount" INTEGER NOT NULL,"perall" VARCHAR NOT NULL,"description" VARCHAR NOT NULL,"recipients" VARCHAR NOT NULL,"status" VARCHAR NOT NULL);
create table "users" ("email" VARCHAR NOT NULL PRIMARY KEY,"name" VARCHAR NOT NULL,"pass" VARCHAR NOT NULL,"role" VARCHAR);
alter table "contacts" add constraint "user_contact_fk" foreign key("userEmail") references "users"("email") on update NO ACTION on delete NO ACTION;
alter table "demands" add constraint "user_demand_fk" foreign key("user_email") references "users"("email") on update NO ACTION on delete NO ACTION;

# --- !Downs

alter table "contacts" drop constraint "user_contact_fk";
alter table "demands" drop constraint "user_demand_fk";
drop table "contacts";
drop table "demands";
drop table "users";

