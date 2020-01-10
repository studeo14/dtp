package edu.vt.datasheet_text_processor.signals;


import org.dizitart.no2.IndexType;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.io.Serializable;
import java.util.List;

@Indices({
        @Index(value = "name", type = IndexType.Unique)
})
public class Signal implements Serializable {

    @Id
    private NitriteId emId;

    private String name;
    private List<String> acronyms;

    public Signal() {
    }

    public Signal(String name) {
        this.name = name;
    }

    public Signal(String name, List<String> acronyms) {
        this.name = name;
        this.acronyms = acronyms;
    }

    public NitriteId getEmId() {
        return emId;
    }

    public void setEmId(NitriteId emId) {
        this.emId = emId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAcronyms() {
        return acronyms;
    }

    public void setAcronyms(List<String> acronyms) {
        this.acronyms = acronyms;
    }
}
