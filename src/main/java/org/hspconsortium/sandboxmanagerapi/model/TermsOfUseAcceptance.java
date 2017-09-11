package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
//@Table(name = "terms_of_use_acceptance")
//@IdClass(TermsOfUseAcceptanceId.class)
public class TermsOfUseAcceptance {
    private Integer id;
//    private User user;
    private TermsOfUse termsOfUse;
    private Timestamp acceptedTimestamp;

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

//    @Id
//    @ManyToOne
//    @PrimaryKeyJoinColumn(name="user_id", referencedColumnName = "id")
//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }

    public Timestamp getAcceptedTimestamp() {
        return acceptedTimestamp;
    }

    public void setAcceptedTimestamp(Timestamp acceptedTimestamp) {
        this.acceptedTimestamp = acceptedTimestamp;
    }

//    @Id
    @ManyToOne
//    @PrimaryKeyJoinColumn(name="terms_of_use_id", referencedColumnName = "id")
    @JoinColumn(name="terms_of_use_id", referencedColumnName = "id")
    @JsonIgnoreProperties("value")
    public TermsOfUse getTermsOfUse() {
        return termsOfUse;
    }

    public void setTermsOfUse(TermsOfUse termsOfUse) {
        this.termsOfUse = termsOfUse;
    }

}
