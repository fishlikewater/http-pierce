create table main.service_mapping
(
    id                INTEGER           not null
        constraint service_mapping_pk
            primary key,
    name              TEXT              not null,
    address           TEXT              not null,
    local_port        INTEGER           not null,
    register_name     TEXT              not null,
    del_register_name INTEGER default 0 not null,
    new_server_port   INTEGER default 0 not null,
    new_port          INTEGER,
    protocol          TEXT              not null,
    enable            INTEGER default 0 not null
);