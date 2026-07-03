
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(15),
    role VARCHAR(20) NOT NULL,        -- PARENT / CAREGIVER / ADMIN
    created_at TIMESTAMP DEFAULT NOW()
);


CREATE TABLE caregiver_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    bio TEXT,
    hourly_rate DECIMAL(6,2),
    experience_years INT,
    specializations VARCHAR(255),     -- comma-separated: infants, toddlers
    is_verified BOOLEAN DEFAULT FALSE,
    doc_url VARCHAR(500),             -- uploaded ID/cert URL
    city VARCHAR(100),
    average_rating DECIMAL(3,2) DEFAULT 0.0
);
CREATE TABLE availability_slots (
    id BIGSERIAL PRIMARY KEY,
    caregiver_id BIGINT REFERENCES caregiver_profiles(id),
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,         -- e.g. 18:00
    end_time TIME NOT NULL,           -- e.g. 22:00
    is_booked BOOLEAN DEFAULT FALSE
);


CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT REFERENCES users(id),
    caregiver_id BIGINT REFERENCES caregiver_profiles(id),
    slot_id BIGINT REFERENCES availability_slots(id),
    status VARCHAR(20) DEFAULT 'PENDING',
    duration_hours DECIMAL(4,2),
    total_amount DECIMAL(8,2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);


CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT REFERENCES bookings(id),
    parent_id BIGINT REFERENCES users(id),
    caregiver_id BIGINT REFERENCES caregiver_profiles(id),
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);