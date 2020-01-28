package edu.vt.datasheet_text_processor.Errors;

public class Warning {

    private ProcessorException exception;

    public Warning(ProcessorException exception) {
        this.exception = exception;
    }

    public ProcessorException getException() {
        return exception;
    }

    public void setException(ProcessorException exception) {
        this.exception = exception;
    }
}
