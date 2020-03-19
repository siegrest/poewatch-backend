#!/usr/bin/env bash

name="pwpg"
image="pw/postgresql"

docker rm -vf $name || true
docker run -d --name=$name \
  -e TZ='Europe/Tallinn' \
  -e POSTGRES_PASSWORD=postgres \
  -p 5533:5432 \
  --restart=unless-stopped \
  -d $image
