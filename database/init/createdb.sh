#!/bin/bash

psql -d "postgres" -U "postgres" -w -f /docker-entrypoint-initdb.d/db_create_user.psql
psql -d "postgres" -U "postgres" -w -f /docker-entrypoint-initdb.d/db_create_database.psql
psql -d "pw" -U "pw" -w -f /docker-entrypoint-initdb.d/db_init_database.psql
