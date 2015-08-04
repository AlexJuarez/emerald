create table mixpo.adtags (
  id serial primary key,
  name text not null,
  template text,
  attributes jsonb
);
