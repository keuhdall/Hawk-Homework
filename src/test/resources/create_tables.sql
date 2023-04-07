CREATE SCHEMA IF NOT EXISTS public;

CREATE SEQUENCE public.movies_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE movies (
   id integer NOT NULL PRIMARY KEY DEFAULT nextval('public.movies_seq'::regclass),
   title varchar(250) NOT NULL,
   country varchar(3) NOT NULL,
   year integer NOT NULL,
   original_title text,
   french_release date,
   synopsis text,
   genres text[] NOT NULL,
   ranking integer NOT NULL
);