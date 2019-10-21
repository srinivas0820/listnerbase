package com.jbhunt.edi.sterlingarchive.dto;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    public ObjectFactory() { }

    public ArchiveDataDTO createArchiveDataDTO() {
        return new ArchiveDataDTO();
    }
}