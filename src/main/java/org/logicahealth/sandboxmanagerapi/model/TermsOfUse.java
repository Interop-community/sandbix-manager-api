package org.logicahealth.sandboxmanagerapi.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@NamedQueries({
        @NamedQuery(name="TermsOfUse.orderByCreatedTimestamp",
                query="SELECT c FROM TermsOfUse c ORDER BY c.createdTimestamp desc")
})
public class TermsOfUse {
    private Integer id;
    private Timestamp createdTimestamp;
    private String value;

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }
    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    @Lob
    @Column(length = 100000 )
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
