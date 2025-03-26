-- Datenbank-Schema für Klausur-Generator

-- Module (Lehrveranstaltungen/Module)
CREATE TABLE IF NOT EXISTS modules (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

-- Fragen (Aufgaben)
CREATE TABLE IF NOT EXISTS questions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    question_text TEXT NOT NULL,
    estimated_time_minutes INTEGER NOT NULL,
    module_id INTEGER REFERENCES modules(id),
    bloom_level INTEGER NOT NULL CHECK (bloom_level BETWEEN 1 AND 6),
    question_format VARCHAR(10) NOT NULL CHECK (question_format IN ('OPEN', 'CLOSED')),
    closed_question_type VARCHAR(20) CHECK (
        closed_question_type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE', 'GAP_TEXT', 'MATCHING', 'RANKING')
        OR (question_format = 'OPEN' AND closed_question_type IS NULL)
    ),
    solution TEXT -- Lösungstext für offene Aufgaben
);

-- Antwortmöglichkeiten für geschlossene Aufgaben
CREATE TABLE IF NOT EXISTS answer_options (
    id SERIAL PRIMARY KEY,
    question_id INTEGER REFERENCES questions(id) ON DELETE CASCADE,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    option_order INTEGER -- für Ranking-Aufgaben
);

-- Klausuren
CREATE TABLE IF NOT EXISTS exams (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    module_id INTEGER REFERENCES modules(id),
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Klausur-Fragen-Zuordnung
CREATE TABLE IF NOT EXISTS exam_questions (
    exam_id INTEGER REFERENCES exams(id) ON DELETE CASCADE,
    question_id INTEGER REFERENCES questions(id) ON DELETE CASCADE,
    question_order INTEGER NOT NULL,
    PRIMARY KEY (exam_id, question_id)
);