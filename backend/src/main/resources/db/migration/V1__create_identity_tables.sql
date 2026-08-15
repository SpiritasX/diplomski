CREATE TABLE study_programs (
    code    VARCHAR2(3) PRIMARY KEY,
    name    VARCHAR2(150) NOT NULL
);

CREATE TABLE enrollment_statuses (
    code    VARCHAR2(30) PRIMARY KEY,
    name    VARCHAR2(30) NOT NULL
);

CREATE TABLE accounts (
    student_index           VARCHAR2(20) PRIMARY KEY,
    first_name              VARCHAR2(100) NOT NULL,
    last_name               VARCHAR2(100) NOT NULL,
    email                   VARCHAR2(255) NOT NULL,
    password_hash           VARCHAR2(255) NOT NULL,
    study_program_code      VARCHAR2(3) NOT NULL REFERENCES study_programs(code),
    enrollment_status_code  VARCHAR2(30) NOT NULL REFERENCES enrollment_statuses(code),
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,

    CONSTRAINT chk_account_student_index CHECK (REGEXP_LIKE(student_index, '^[A-Z]{2,3} [0-9]{1,3}/[0-9]{4}$')),
    CONSTRAINT uq_account_email UNIQUE (email)
);

CREATE TABLE admin_mandates (
    admin_mandate_id    RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    student_index       VARCHAR2(20) NOT NULL REFERENCES accounts(student_index),
    valid_from          TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    valid_until         TIMESTAMP WITH TIME ZONE,

    CONSTRAINT chk_admin_mandate_dates
        CHECK (valid_until IS NULL OR valid_until > valid_from)
);