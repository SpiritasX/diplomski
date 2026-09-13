ALTER TABLE eligible_voters
    ADD voting_option_number NUMBER(19);

ALTER TABLE eligible_voters
    ADD CONSTRAINT fk_eligible_voter_public_option
        FOREIGN KEY (voting_proposal_id, voting_option_number)
        REFERENCES voting_options(voting_proposal_id, voting_option_number);

CREATE TABLE secret_participations (
    voting_proposal_id  RAW(16) NOT NULL,
    student_index       VARCHAR2(20) NOT NULL,
    recorded_at         TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,

    CONSTRAINT pk_secret_participations
        PRIMARY KEY (voting_proposal_id, student_index),
    CONSTRAINT fk_secret_participation_voter
        FOREIGN KEY (voting_proposal_id, student_index)
        REFERENCES eligible_voters(voting_proposal_id, student_index)
);

CREATE TABLE secret_ballots (
    voting_proposal_id      RAW(16) NOT NULL,
    receipt_hash            VARCHAR2(64) NOT NULL,
    voting_option_number    NUMBER(19) NOT NULL,
    cast_at                 TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,

    CONSTRAINT pk_secret_ballots
        PRIMARY KEY (voting_proposal_id, receipt_hash),
    CONSTRAINT fk_secret_ballot_proposal
        FOREIGN KEY (voting_proposal_id)
        REFERENCES voting_proposals(voting_proposal_id),
    CONSTRAINT fk_secret_ballot_option
        FOREIGN KEY (voting_proposal_id, voting_option_number)
        REFERENCES voting_options(voting_proposal_id, voting_option_number)
-- TODO: uncomment when we have a lookup api
--     CONSTRAINT uq_receipt_hash
--         UNIQUE (voting_proposal_id?, receipt_hash)
);
