package edu.vt.datasheet_text_processor.Errors.Context;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Even though this will go into a NitriteDB this needs to be added.
 * Nitrite uses JacksonJSON underneath and that allows subclasses by using these annotations!
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TokenizerContext.class, name = "TokenizerContext")
})
public abstract class Context {
    private String message;

    public Context(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
