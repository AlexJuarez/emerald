create table mixpo.adtags (
  id serial primary key,
  name text not null,
  template text,
  version_suffix text,
  attributes jsonb
);
