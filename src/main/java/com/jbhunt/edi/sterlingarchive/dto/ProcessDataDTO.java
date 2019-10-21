package com.jbhunt.edi.sterlingarchive.dto;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

@XmlRootElement(name = "ProcessData")
public class ProcessDataDTO {
    @XmlAnyElement(lax = true)
    private List<Object> anything;

    @XmlTransient
    public List<Object> getAnything() {
        return anything;
    }

    public void setAnything(List<Object> anything) {
        this.anything = anything;
    }
}
