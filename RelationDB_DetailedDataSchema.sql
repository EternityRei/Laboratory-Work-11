-- create user table
create table "User" (
    id serial primary key,
    name varchar(359) check (name ~ '^[a-za-z0-9]+$'),
    surname varchar(350) check (surname ~ '^[a-za-z0-9]+$'),
    email varchar(255) unique check (email ~
    '^[a-za-z0-9._%+-]+@[a-za-z0-9.-]+\.[a-za-z]{2,}$'),
    password varchar(255) check (password ~
    '^(?=.*[a-z])(?=.*[a-z])(?=.*\d)(?=.*[\$!?\\])[a-za-z\d$!?]{8,}$')
);

-- create microclimate table
create table microclimate (
    id serial primary key,
    temperature varchar(20),
    ventilation varchar(150),
    lightlevel numeric check (lightlevel > 0),
    humidity_id int
);
...


-- Create Humidity table
CREATE TABLE Humidity (
    id SERIAL PRIMARY KEY,
    relativeHumidity NUMERIC CHECK (relativeHumidity > 0),
    absoluteHumidity NUMERIC CHECK (absoluteHumidity > 0)
);

-- Create PlanParameters table
CREATE TABLE PlanParameters (
    id SERIAL PRIMARY KEY,
    temperatureSked VARCHAR(100),
    lightsOffTime TIMESTAMP
);

-- Create PlanPattern table
CREATE TABLE PlanPattern (
    id SERIAL PRIMARY KEY,
    optimalMicroclimate_id INT,
    device VARCHAR(200),
    planParameters_id INT
);

-- Create Theme table
CREATE TABLE Theme (
    id INT PRIMARY KEY,
    title VARCHAR(255)
);

-- Create TopicsInfo table
CREATE TABLE TopicsInfo (
    id INT PRIMARY KEY,
    description TEXT,
    type VARCHAR(255),
    info BYTEA
);

-- Create a junction table for the many-to-many relationship
CREATE TABLE ThemeTopicsInfo (
    theme_id INT,
    topics_info_id INT,
    PRIMARY KEY (theme_id, topics_info_id),
    CONSTRAINT fk_theme
        FOREIGN KEY (theme_id) 
        REFERENCES Theme(id),
    CONSTRAINT fk_topics_info
        FOREIGN KEY (topics_info_id) 
        REFERENCES TopicsInfo(id)
);

-- Create MicroclimatePlan table
CREATE TABLE MicroclimatePlan (
    id SERIAL PRIMARY KEY,
    planPattern_id INT,
    initiallyMicroclimate_id INT,
    user_id INT,
	topic_id INT
);


-- Add foreign key constraints
ALTER TABLE Microclimate
ADD CONSTRAINT fk_humidity_id
FOREIGN KEY (humidity_id) REFERENCES Humidity(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE PlanPattern
ADD CONSTRAINT fk_optimalMicroclimate_id
FOREIGN KEY (optimalMicroclimate_id) REFERENCES Microclimate(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE PlanPattern
ADD CONSTRAINT fk_planParameters_id
FOREIGN KEY (planParameters_id) REFERENCES PlanParameters(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE MicroclimatePlan
ADD CONSTRAINT fk_planPattern_id
FOREIGN KEY (planPattern_id) REFERENCES PlanPattern(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE MicroclimatePlan
ADD CONSTRAINT fk_initiallyMicroclimate_id
FOREIGN KEY (initiallyMicroclimate_id) REFERENCES Microclimate(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE MicroclimatePlan
ADD CONSTRAINT fk_user_id
FOREIGN KEY (user_id) REFERENCES "User"(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE MicroclimatePlan
ADD CONSTRAINT fk_topic_id
FOREIGN KEY (topic_id) 
REFERENCES TopicsInfo(id) ON DELETE CASCADE ON UPDATE CASCADE;
