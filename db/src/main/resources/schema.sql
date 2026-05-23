
CREATE TABLE IF NOT EXISTS users (
    id           SERIAL PRIMARY KEY,
    username     VARCHAR(50) UNIQUE NOT NULL,
    password     VARCHAR(255) NOT NULL,
    full_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(100) UNIQUE NOT NULL,
    phone        VARCHAR(20),
    role         VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'CLIENT')),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS animals (
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(50) NOT NULL,
    species       VARCHAR(20) NOT NULL CHECK (species IN ('DOG', 'CAT', 'BIRD')),
    breed         VARCHAR(50),
    age           INT CHECK (age >= 0),
    gender        VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE')),
    weight        DECIMAL(5,2),
    color         VARCHAR(30),
    description   TEXT,
    status        VARCHAR(20) DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'PENDING', 'ADOPTED')),
    arrival_date  DATE DEFAULT CURRENT_DATE,
    is_trained    BOOLEAN,
    is_indoor     BOOLEAN,
    can_fly       BOOLEAN
);

CREATE TABLE IF NOT EXISTS adoptions (
    id              SERIAL PRIMARY KEY,
    animal_id       INT NOT NULL REFERENCES animals(id) ON DELETE CASCADE,
    client_id       INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    adoption_date   DATE DEFAULT CURRENT_DATE,
    status          VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED')),
    notes           TEXT,
    approved_by     INT REFERENCES users(id),

    UNIQUE(animal_id, client_id, adoption_date)
);


CREATE INDEX idx_animals_status   ON animals(status);
CREATE INDEX idx_animals_species  ON animals(species);
CREATE INDEX idx_adoptions_client ON adoptions(client_id);
CREATE INDEX idx_adoptions_animal ON adoptions(animal_id);
