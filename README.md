# [https://poe.watch](https://poe.watch)

![Prices page](resources/images/img01.png)

## Overview

PoeWatch is a Path of Exile statistics and price data collection page that's been in the works since 2017. It gathers data over time for various items (such as uniques, gems, currency, you name it) from public trade listings and finds the average prices.

This repository contains the app and the API. The web front is located in a separate repository over at [poewatch-frontend](https://github.com/siegrest/poewatch-frontend).

## The general idea

The general goal was to make a statistics website with everything in one place. Users can check prices of almost any item type from the current or past leagues and look up character names.

## Getting Started

Run commands in project root

1. Run db in docker
    ```shell script
    ./database/init/docker-build.sh
    ./database/init/docker-run.sh
    ```

2. Initialize database

    1. Copy 
        ```shell script
         cp ./database/liquibase.properties.sample ./database/liquibase.properties
        ```
    2. Todo: write database initialization scripts for liquibase
    3. Run
        ```shell script
        ./gradlew update
        ```

3. Run app
    
    1. Copy 
        ```shell script
        cp ./modules/app/src/main/resources/application.properties.sample ./modules/app/src/main/resources/application.properties
        ```
    2. Edit config
    3. Run
        ```shell script
        ./gradlew bootRun
        ```
