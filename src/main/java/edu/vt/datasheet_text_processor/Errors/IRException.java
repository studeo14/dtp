package edu.vt.datasheet_text_processor.Errors;

import edu.vt.datasheet_text_processor.Errors.Context.Context;

public class IRException extends ProcessorException {
    public IRException(String message, Context context) {
        super(message, context);
    }
}
