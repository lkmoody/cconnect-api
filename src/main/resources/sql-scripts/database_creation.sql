create table core.groups
(
    id            uuid      default gen_random_uuid() not null
        constraint groups_pk
            primary key,
    name          text                                not null,
    "displayName" text                                not null,
    created       timestamp default now()             not null,
    updated       timestamp default now()             not null
);

alter table core.groups
    owner to postgres;




create table core.bills
(
    id           uuid      default gen_random_uuid() not null
        constraint bills_pk
            primary key,
    name         text                                not null,
    description  text                                not null,
    "voteClosed" boolean   default false             not null,
    "groupId"    uuid                                not null
        constraint bills_groups_null_fk
            references core.groups,
    created      timestamp default now()             not null,
    updated      timestamp default now()             not null
);

alter table core.bills
    owner to postgres;

create table core.users
(
    id            uuid      default gen_random_uuid() not null
        constraint users_pk
            primary key,
    "authId"      text                                not null,
    "firstName"   text,
    "lastName"    text,
    "displayName" text,
    phone         integer,
    email         text                                not null,
    created       timestamp default now()             not null,
    updated       timestamp default now()             not null
);

alter table core.users
    owner to postgres;



create table core."user-groups"
(
    id        uuid default gen_random_uuid() not null
        constraint "user-groups_pk"
            primary key,
    "userId"  uuid                           not null
        constraint "user-groups_users_null_fk"
            references core.users,
    "groupId" uuid                           not null
        constraint "user-groups_groups_null_fk"
            references core.groups
);

alter table core."user-groups"
    owner to postgres;

create table core."user-settings"
(
    id                            uuid      default gen_random_uuid() not null
        constraint "user-settings_pk"
            primary key
                deferrable,
    "userId"                      uuid                                not null
        constraint "user-settings_users_null_fk"
            references core.users,
    "voteTextNotificationEnabled" boolean   default false             not null,
    "twitterVotePostEnabled"      boolean   default false             not null,
    created                       timestamp default now()             not null,
    updated                       timestamp default now()             not null
);

alter table core."user-settings"
    owner to postgres;

create table core."user-twitters"
(
    id                         uuid      default gen_random_uuid() not null
        constraint "user-twitter_pk"
            primary key,
    "userId"                   uuid                                not null
        constraint "user-twitters_users_null_fk"
            references core.users,
    token                      text,
    secret                     text,
    "requestAccessToken"       text,
    "requestAccessTokenSecret" text,
    created                    timestamp default now()             not null,
    updated                    timestamp default now()             not null
);

alter table core."user-twitters"
    owner to postgres;

create table core."vote-details"
(
    id        uuid      default gen_random_uuid() not null
        constraint "voteDetails_pk"
            primary key,
    "voteId"  uuid                                not null,
    vote      text                                not null,
    pros      text,
    cons      text,
    reasoning text                                not null,
    created   timestamp default now()             not null,
    updated   timestamp default now()             not null
);

alter table core."vote-details"
    owner to postgres;

create table core.votes
(
    id             uuid      default gen_random_uuid() not null
        constraint vote_pk
            primary key,
    "billId"       uuid                                not null
        constraint votes_bills_null_fk
            references core.bills,
    "userId"       uuid                                not null,
    "voteDetailId" uuid
        constraint "votes_vote-details_null_fk"
            references core."vote-details",
    created        timestamp default now()             not null,
    updated        timestamp default now()             not null
);

alter table core.votes
    owner to postgres;











