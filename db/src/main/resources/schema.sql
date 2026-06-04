
CREATE TABLE IF NOT EXISTS users (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    username     TEXT UNIQUE NOT NULL,
    password     TEXT NOT NULL,
    full_name    TEXT NOT NULL,
    email        TEXT UNIQUE NOT NULL,
    phone        TEXT,
    role         TEXT NOT NULL CHECK (role IN ('ADMIN', 'CLIENT')),
    created_at   TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS animals (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT NOT NULL,
    species       TEXT NOT NULL CHECK (species IN ('DOG', 'CAT', 'BIRD')),
    breed         TEXT,
    age           INTEGER CHECK (age >= 0),
    gender        TEXT CHECK (gender IN ('MALE', 'FEMALE')),
    weight        REAL,
    color         TEXT,
    description   TEXT,
    status        TEXT DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'PENDING', 'ADOPTED')),
    arrival_date  TEXT DEFAULT CURRENT_DATE,
    is_trained    INTEGER,
    is_indoor     INTEGER,
    can_fly       INTEGER
);

CREATE TABLE IF NOT EXISTS adoptions (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    animal_id       INTEGER NOT NULL REFERENCES animals(id) ON DELETE CASCADE,
    client_id       INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    adoption_date   TEXT DEFAULT CURRENT_DATE,
    status          TEXT DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED')),
    notes           TEXT,
    approved_by     INTEGER REFERENCES users(id),

    UNIQUE(animal_id, client_id, adoption_date)
);

CREATE INDEX IF NOT EXISTS idx_animals_status   ON animals(status);
CREATE INDEX IF NOT EXISTS idx_animals_species  ON animals(species);
CREATE INDEX IF NOT EXISTS idx_adoptions_client ON adoptions(client_id);
CREATE INDEX IF NOT EXISTS idx_adoptions_animal ON adoptions(animal_id);
