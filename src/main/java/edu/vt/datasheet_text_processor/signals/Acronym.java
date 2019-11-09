package edu.vt.datasheet_text_processor.signals;

import org.dizitart.no2.IndexType;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.io.Serializable;

@Indices({
        @Index(value = "acronym", type = IndexType.NonUnique),
        @Index(value = "expanded", type = IndexType.Unique)
})
public class Acronym implements Serializable {

    @Id
    private NitriteId emId;

    private String acronym;
    private String expanded;

    public Acronym() {
    }

    public Acronym(String acronym, String expanded) {
        this.acronym = acronym;
        this.expanded = expanded;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getExpanded() {
        return expanded;
    }

    public void setExpanded(String expanded) {
        this.expanded = expanded;
    }
}
