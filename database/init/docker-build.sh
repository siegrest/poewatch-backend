#!/bin/bash

this=$(pwd)/$(dirname "$0")
docker build -t pw/postgresql "$this" || exit 1
