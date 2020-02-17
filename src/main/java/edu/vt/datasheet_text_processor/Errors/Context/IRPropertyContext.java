package edu.vt.datasheet_text_processor.Errors.Context;

import java.util.List;

public class IRPropertyContext extends Context {
    private String name;
    private String prop;

    public IRPropertyContext() {
    }

    public IRPropertyContext(String message, String name, String prop) {
        super(message);
        this.name = name;
        this.prop = prop;
    }

    public IRPropertyContext(Context ctx, String name, String prop) {
        super(ctx);
        this.name = name;
        this.prop = prop;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProp() {
        return prop;
    }

    public void setProp(String prop) {
        this.prop = prop;
    }
}
