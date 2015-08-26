create table mixpo.media (
  id serial primary key,
  client_id uuid references mixpo.clients,
  name text not null,
  type text not null
);
