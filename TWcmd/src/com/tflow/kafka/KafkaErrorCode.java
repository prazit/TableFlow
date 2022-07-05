package com.tflow.kafka;

/**
 * All code need to count down from -1.
 */
public enum KafkaErrorCode {

    /**
     * IGNORED when the header or data is not for this client.
     */
    IGNORED_HEADER(-1),
    IGNORED_DATA(-2),

    /**
     * Error from Read Command Service.
     */
    INTERNAL_SERVER_ERROR(-11),
    PROJECT_EDITING_BY_ANOTHER(-12),
    DATA_FILE_NOT_FOUND(-13),
    UNSUPPORTED_FILE_TYPE(-14),

    REQUIRES_RECORD_ID(-21),
    REQUIRES_PROJECT_ID(-22),
    REQUIRES_STEP_ID(-23),
    REQUIRES_DATATABLE_ID(-24),
    REQUIRES_TRANSFORMTABLE_ID(-25),
    ;

    private long code;

    public static KafkaErrorCode parse(long code) {
        for (KafkaErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) return errorCode;
        }
        return null;
    }

    public long getCode() {
        return code;
    }

    KafkaErrorCode(long code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return name();
    }
}
