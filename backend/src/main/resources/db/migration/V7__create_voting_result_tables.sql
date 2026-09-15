CREATE TABLE voting_results (
    voting_proposal_id      RAW(16) PRIMARY KEY REFERENCES voting_proposals(voting_proposal_id),
    eligible_count          NUMBER(19) NOT NULL,
    participation_count     NUMBER(19) NOT NULL,
    quorum_met              NUMBER(1) NOT NULL,
    outcome                 VARCHAR2(20) NOT NULL,
    computed_at             TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    published_at            TIMESTAMP WITH TIME ZONE,
    result_hash             VARCHAR2(64) NOT NULL,

    CONSTRAINT chk_voting_result_counts
        CHECK (
            eligible_count >= 0
            AND participation_count >= 0
            AND participation_count <= eligible_count
        ),
    CONSTRAINT chk_voting_result_quorum_met
        CHECK (quorum_met IN (0, 1)),
    CONSTRAINT chk_voting_result_outcome
        CHECK (outcome IN ('OPTION_SELECTED', 'TIE', 'NO_DECISION')),
    CONSTRAINT chk_voting_result_published_at
        CHECK (published_at IS NULL OR published_at >= computed_at)
);

CREATE TABLE voting_option_results (
    voting_proposal_id      RAW(16) NOT NULL,
    voting_option_number    NUMBER(19) NOT NULL,
    vote_count              NUMBER(19) NOT NULL,

    CONSTRAINT pk_voting_option_results
        PRIMARY KEY (voting_proposal_id, voting_option_number),
    CONSTRAINT fk_voting_option_result_result
        FOREIGN KEY (voting_proposal_id)
        REFERENCES voting_results(voting_proposal_id),
    CONSTRAINT fk_voting_option_result_option
        FOREIGN KEY (voting_proposal_id, voting_option_number)
        REFERENCES voting_options(voting_proposal_id, voting_option_number)
);
