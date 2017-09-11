package org.hspconsortium.sandboxmanagerapi.model;

import java.io.Serializable;

public class TermsOfUseAcceptanceId implements Serializable {
    private Integer user;
    private Integer termsOfUse;

    public Integer getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user = user;
    }

    public Integer getTermsOfUse() {
        return termsOfUse;
    }

    public void setTermsOfUse(Integer termsOfUse) {
        this.termsOfUse = termsOfUse;
    }

    @Override
    public int hashCode() {
        return (Integer)(user + termsOfUse);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof TermsOfUseAcceptanceId) {
            TermsOfUseAcceptanceId otherId = (TermsOfUseAcceptanceId) object;
            return (otherId.user.equals(this.user)) && (otherId.termsOfUse.equals(this.termsOfUse));
        }
        return false;
    }
}
