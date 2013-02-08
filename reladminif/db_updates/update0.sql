-- table storing latest database update

CREATE TABLE ras_dbupdates (
  id serial NOT NULL,
  update integer,
  primary key (id)
);

insert into ras_dbupdates values (1, 0);

