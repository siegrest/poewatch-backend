FROM postgres:11.2

ENV LC_ALL="en_US.UTF-8"
ENV LC_CTYPE="en_US.UTF-8"

ENV LANG et_EE.utf8
ENV TZ "Europe/Tallinn"
ENV POSTGRES_PASSWORD "postgres"
ENV PGDATA=/data

ADD scripts /docker-entrypoint-initdb.d
ADD createdb.sh /docker-entrypoint-initdb.d

RUN sed -i 's/\r//' /docker-entrypoint-initdb.d/createdb.sh

CMD ["postgres", "-c", "max_connections=500"]
