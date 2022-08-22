package com.opal;

public enum CommitStep {
	NOT_CURRENTLY_COMMITTING,
	STARTED_PHASE_ONE,
	DOING_PHASE_ONE,
	ENDED_PHASE_ONE,
	STARTED_PHASE_TWO,
	DOING_PHASE_TWO,
	ENDED_PHASE_TWO,
	COMMITTED,
	ROLLING_BACK,
	ROLLED_BACK,
}
