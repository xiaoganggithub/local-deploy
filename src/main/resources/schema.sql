create table if not exists file_meta (
    id bigserial primary key,
    biz_id varchar(64) not null,
    filename varchar(255) not null,
    content_type varchar(128),
    size_bytes bigint not null,
    bucket varchar(128) not null,
    object_key varchar(512) not null,
    created_at timestamptz default now(),
    updated_at timestamptz default now()
);

create index if not exists idx_file_meta_biz_id on file_meta(biz_id);
create table if not exists file_meta (
    id bigserial primary key,
    biz_id varchar(64) not null,
    filename varchar(255) not null,
    content_type varchar(128),
    size_bytes bigint not null,
    bucket varchar(128) not null,
    object_key varchar(512) not null,
    created_at timestamptz default now(),
    updated_at timestamptz default now()
);

create index if not exists idx_file_meta_biz_id on file_meta(biz_id);

