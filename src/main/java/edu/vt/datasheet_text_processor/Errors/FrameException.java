package edu.vt.datasheet_text_processor.Errors;

import edu.vt.datasheet_text_processor.Errors.Context.Context;

public class FrameException extends ProcessorException {
    public FrameException(String message, Context context) {
        super(message, context);
    }
}
