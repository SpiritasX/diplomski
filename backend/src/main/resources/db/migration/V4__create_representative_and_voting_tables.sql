CREATE TABLE representative_bodies (
    body_id     RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    name        VARCHAR2(150) NOT NULL,

    CONSTRAINT uq_representative_body_name
        UNIQUE (name)
);

CREATE TABLE representative_mandates (
    mandate_id      RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    student_index   VARCHAR2(20) NOT NULL REFERENCES accounts(student_index),
    body_id         RAW(16) NOT NULL REFERENCES representative_bodies(body_id),
    valid_from      TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    valid_until     TIMESTAMP WITH TIME ZONE,

    CONSTRAINT chk_representative_mandate_dates
        CHECK (valid_until IS NULL OR valid_until > valid_from)
);

CREATE TABLE voting_proposals (
    voting_proposal_id      RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    creator_student_index   VARCHAR2(20) NOT NULL REFERENCES accounts(student_index),
    creator_mandate_id      RAW(16) NOT NULL REFERENCES representative_mandates(mandate_id),
    creator_body_id         RAW(16) NOT NULL REFERENCES representative_bodies(body_id),
    title                   VARCHAR2(200) NOT NULL,
    description             VARCHAR2(1000),
    ballot_type             VARCHAR2(10),
    status                  VARCHAR2(10) DEFAULT 'DRAFT' NOT NULL,
    starts_at               TIMESTAMP WITH TIME ZONE,
    ends_at                 TIMESTAMP WITH TIME ZONE,
    quorum_type             VARCHAR2(15),
    quorum_value            NUMBER(19),
    decision_rule           VARCHAR2(20),
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    locked_at               TIMESTAMP WITH TIME ZONE,
    configuration_hash      VARCHAR2(64),

    CONSTRAINT chk_voting_proposal_dates
        CHECK (
            starts_at IS NULL
            OR ends_at IS NULL
            OR ends_at > starts_at
        ),
    CONSTRAINT chk_voting_proposal_ballot_type
        CHECK (ballot_type IN ('PUBLIC', 'SECRET')),
    CONSTRAINT chk_voting_proposal_status
        CHECK (status IN ('DRAFT', 'LOCKED', 'OPEN', 'CLOSED', 'CANCELLED')),
    CONSTRAINT chk_voting_proposal_quorum_type
        CHECK (quorum_type IN ('NONE', 'PERCENTAGE', 'ABSOLUTE')),
    CONSTRAINT chk_voting_proposal_quorum_value
        CHECK (
            status = 'DRAFT'
            OR (quorum_type = 'NONE' AND quorum_value IS NULL)
            OR (quorum_type = 'ABSOLUTE' AND quorum_value >= 1)
            OR (quorum_type = 'PERCENTAGE' AND quorum_value BETWEEN 1 AND 100)
        ),
    CONSTRAINT chk_voting_proposal_decision_rule
        CHECK (decision_rule IN ('PLURALITY', 'SIMPLE_MAJORITY', 'UNANIMITY')),
    CONSTRAINT chk_voting_proposal_locked_configuration
        CHECK (
            status = 'DRAFT'
            OR (
                ballot_type IS NOT NULL
                AND starts_at IS NOT NULL
                AND ends_at IS NOT NULL
                AND quorum_type IS NOT NULL
                AND decision_rule IS NOT NULL
                AND locked_at IS NOT NULL
                AND configuration_hash IS NOT NULL
            )
        )
);

CREATE OR REPLACE TRIGGER trg_voting_proposal_start_time
    BEFORE INSERT OR UPDATE OF starts_at
    ON voting_proposals
    FOR EACH ROW
BEGIN
    IF :NEW.starts_at IS NOT NULL AND :NEW.starts_at <= SYSTIMESTAMP THEN
        RAISE_APPLICATION_ERROR(
                -20001,
                'starts_at must be in the future'
        );
    END IF;
END;
/

CREATE TABLE voting_options (
    voting_proposal_id      RAW(16) NOT NULL REFERENCES voting_proposals(voting_proposal_id),
    voting_option_number    NUMBER(19) NOT NULL,
    text                    VARCHAR2(100) NOT NULL,

    CONSTRAINT pk_voting_options
        PRIMARY KEY (voting_proposal_id, voting_option_number),
    CONSTRAINT uq_voting_option_proposal_text
        UNIQUE (voting_proposal_id, text)
);

CREATE TABLE eligible_voters (
    voting_proposal_id  RAW(16) NOT NULL REFERENCES voting_proposals(voting_proposal_id),
    student_index       VARCHAR2(20) NOT NULL REFERENCES accounts(student_index),
    voted_at            TIMESTAMP WITH TIME ZONE,
    receipt_hash        VARCHAR2(64) NOT NULL,

    CONSTRAINT pk_eligible_voters
        PRIMARY KEY (voting_proposal_id, student_index),
    CONSTRAINT uq_eligible_voter_receipt_hash
        UNIQUE (receipt_hash)
);
