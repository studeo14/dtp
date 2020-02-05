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
        @JsonSubTypes.Type(value = TokenizerContext.class, name = "TokenizerContext"),
        @JsonSubTypes.Type(value = BitAccessNormalizerContext.class, name = "BitAccessNormalizerContext"),
        @JsonSubTypes.Type(value = BitAccessNormalizerFinderContext.class, name = "BitAccessNormalizerFinderContext"),
        @JsonSubTypes.Type(value = FrameFinderContext.class, name = "FrameFinderContext"),
        @JsonSubTypes.Type(value = GenericContext.class, name = "GenericContext"),
        @JsonSubTypes.Type(value = IRContext.class, name = "IRContext"),
        @JsonSubTypes.Type(value = IRPropertyContext.class, name = "IRPropertyContext"),
        @JsonSubTypes.Type(value = SemanticExpressionContext.class, name = "SemanticExpressionContext"),
        @JsonSubTypes.Type(value = SerializerContext.class, name = "SerializerContext"),
        @JsonSubTypes.Type(value = IRConsequentContext.class, name = "IRConsequentContext"),
        @JsonSubTypes.Type(value = IRCompoundContext.class, name = "IRCompoundContext")
})
public abstract class Context {
    private String message;

    public Context() {}

    public Context(String message) {
        this.message = message;
    }

    public Context(Context ctx) {
        this.message = ctx.message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
