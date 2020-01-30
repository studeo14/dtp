package edu.vt.datasheet_text_processor.Errors;

import edu.vt.datasheet_text_processor.Errors.Context.Context;

public class SerializerException extends ProcessorException{
    public SerializerException(String message, Context context) {
        super(message, context);
    }

    public SerializerException(SerializerException e) {
        super(e);
    }
}
