package org.logicahealth.sandboxmanagerapi.model;

import java.sql.Timestamp;

public abstract class AbstractItem {
    protected Integer id;
    protected User createdBy;
    protected Timestamp createdTimestamp;
    protected Visibility visibility;

    public abstract Integer getId();
    public abstract void setId(Integer id);
    public abstract User getCreatedBy();
    public abstract void setCreatedBy(User createdBy);
    public abstract Timestamp getCreatedTimestamp();
    public abstract void setCreatedTimestamp(Timestamp createdTimestamp);
    public abstract Visibility getVisibility();
    public abstract void setVisibility(Visibility visibility);

}
