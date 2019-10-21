package com.jbhunt.edi.sterlingarchive.dto;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

@XmlRootElement(name = "ArchiveData")
public class ArchiveDataDTO {
    @XmlAnyElement(lax = true)
    private List<Object> anything;

    @XmlTransient
    public List<Object> getAnything() {
        return anything;
    }

    public void setAnything(List<Object> anything) {
        this.anything = anything;
    }

    @XmlElement(name = "ParentBPID")
    private String parentBpId;

    @XmlTransient
    public String getParentBpId() {
        return parentBpId;
    }

    public void setParentBpId(String parentBpId) {
        this.parentBpId = parentBpId;
    }

    @XmlElement(name = "ArchiveBPID")
    private String archiveBpId;

    @XmlTransient
    public String getArchiveBpId() {
        return archiveBpId;
    }

    public void setArchiveBpId(String archiveBpId) {
        this.archiveBpId = archiveBpId;
    }

    @XmlElement(name = "ClusterID")
    private String clusterId;

    @XmlTransient
    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    @XmlElement(name = "ErrorFlag")
    private Boolean errorFlag;

    @XmlTransient
    public Boolean getErrorFlag() {
        return errorFlag;
    }

    public void setErrorFlag(Boolean errorFlag) {
        this.errorFlag = errorFlag;
    }

/*    @XmlElement(name = "ErrorMessage")
    private String errorMessage;

    @XmlTransient
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }*/
}
