package edu.vt.datasheet_text_processor.Errors;

import edu.vt.datasheet_text_processor.Errors.Context.Context;

public class ProcessorException extends Throwable {

    private Context context;

    public ProcessorException(String message, Context context) {
        super(message);
        this.context = context;
    }

    public ProcessorException(SerializerException e) {
        super(e.getMessage());
        this.context = e.getContext();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
