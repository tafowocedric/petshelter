INSERT INTO users (username, password, full_name, email, phone, role) VALUES
('admin',    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'System Administrator', 'admin@shelter.com', '+1234567890', 'ADMIN'),
('john_doe', '7f1524871be2754bf615a18f47ad13d44f60fb01b0bf1a8d4d2c6d1d8eb1c7e3', 'John Doe',             'john@example.com',  '+1234567891', 'CLIENT')
ON CONFLICT (username) DO NOTHING;

INSERT INTO animals (name, species, breed, age, gender, weight, color, description, is_trained, is_indoor, can_fly) VALUES
('Rex',      'DOG',  'Labrador',         3, 'MALE',   25.50, 'Golden', 'Friendly and energetic',  true,  null, null),
('Buddy',    'DOG',  'Golden Retriever', 5, 'MALE',   30.00, 'Cream',  'Loves children and walks', true,  null, null),
('Whiskers', 'CAT',  'Persian',          2, 'FEMALE',  4.20, 'White',  'Calm and affectionate',   null,  true, null),
('Mittens',  'CAT',  'Siamese',          4, 'FEMALE',  3.80, 'Cream',  'Curious and playful',     null,  true, null),
('Tweety',   'BIRD', 'Canary',           1, 'MALE',    0.02, 'Yellow', 'Loves to sing',           null,  null, true),
('Coco',     'BIRD', 'Parrot',           7, 'FEMALE',  0.45, 'Green',  'Can mimic words',         null,  null, true);
