-- create user table
    create table "User" (
    id serial primary key,
    name varchar(359) check (name
	    
    ~'^[a-za-z0-9]+$'),
    surname varchar(350) check (surname
	    
    ~'^[a-za-z0-9]+$'),
    email varchar(255) unique check (email
	    
        ~'^[a-za-z0-9._%+-]+@[a-za-z0-9.-]+\.[a-za-z]{2,}$'),
    password varchar(255) check (password
	    
        ~'^(?=.*[a-z])(?=.*[a-z])(?=.*\d)(?=.*[\$!?\])[a-za-z\d$!?]{8,}$')
);

-- create microclimate table
    create table microclimate (
    id serial primary key,
    temperature varchar(20),
    ventilation varchar(150),
    lightlevel numeric check (lightlevel > 0),
    humidity_id int
);

-- create humidity table
    create table humidity (
    id serial primary key,
    relativehumidity numeric check (relativehumidity > 0),
    absolutehumidity numeric check (absolutehumidity > 0)
);

-- create planparameters table
    create table planparameters (
    id serial primary key,
    temperaturesked varchar(100),
    lightsofftime timestamp
);

-- create planpattern table
    create table planpattern (
    id serial primary key,
    optimalmicroclimate_id int,
    device varchar(200),
    planparameters_id int
);

-- create theme table
    create table theme (
    id int primary key,
    title varchar(255)
);

-- create topicsinfo table
    create table topicsinfo (
    id int primary key,
    description text,
    type varchar(255),
    info bytea
);

-- create a junction table for the many-to-many relationship
    create table themetopicsinfo (
    theme_id int,
    topics_info_id int,
    primary key (theme_id, topics_info_id),
    constraint fk_theme
        foreign key (theme_id) 
        references theme(id),
    constraint fk_topics_info
        foreign key (topics_info_id) 
        references topicsinfo(id)
);

-- create microclimateplan table
    create table microclimateplan (
    id serial primary key,
    planpattern_id int,
    initiallymicroclimate_id int,
    user_id int,
	topic_id int
);


-- add foreign key constraints
    alter table microclimate
    add constraint fk_humidity_id
foreign key (humidity_id) references humidity(id) on delete cascade on update
        cascade;

    alter table planpattern
    add constraint fk_optimalmicroclimate_id
foreign key (optimalmicroclimate_id) references microclimate(id) on delete
        cascade on update cascade;

    alter table planpattern
    add constraint fk_planparameters_id
foreign key (planparameters_id) references planparameters(id) on delete cascade
        on update cascade;

    alter table microclimateplan
    add constraint fk_planpattern_id
foreign key (planpattern_id) references planpattern(id) on delete cascade on
        update cascade;

    alter table microclimateplan
    add constraint fk_initiallymicroclimate_id
foreign key (initiallymicroclimate_id) references microclimate(id) on delete
        cascade on update cascade;

    alter table microclimateplan
    add constraint fk_user_id
foreign key (user_id) references "User"(id) ON DELETE CASCADE ON UPDATE CASCADE;

    alter table microclimateplan
    add constraint fk_topic_id
foreign key (topic_id) 
references topicsinfo(id) on delete cascade on update cascade;
