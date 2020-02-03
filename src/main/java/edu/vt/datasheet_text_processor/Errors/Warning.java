package edu.vt.datasheet_text_processor.Errors;

import edu.vt.datasheet_text_processor.Errors.Context.Context;

public class Warning {

    private Context context;

    public Warning() {}

    public Warning(Context context) {
        this.context = context;
    }

    public Warning(ProcessorException e) {
        this.context = e.getContext();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
