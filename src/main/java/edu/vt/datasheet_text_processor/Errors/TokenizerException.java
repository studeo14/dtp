package edu.vt.datasheet_text_processor.Errors;

import edu.vt.datasheet_text_processor.Errors.Context.Context;

public class TokenizerException extends ProcessorException {
    public TokenizerException(String message, Context context) {
        super(message, context);
    }
}
