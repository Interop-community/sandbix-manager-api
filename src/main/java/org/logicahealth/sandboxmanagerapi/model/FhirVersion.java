package org.logicahealth.sandboxmanagerapi.model;

public class FhirVersion {

    private String dstu2;
    private String stu3;
    private String r4;
    private String r5;
    private String apiBaseURL_dstu2;
    private String apiBaseURL_stu3;
    private String apiBaseURL_r4;
    private String apiBaseURL_r5;

    public String getDstu2() {
        return dstu2;
    }

    public void setDstu2(String dstu2) {
        this.dstu2 = dstu2;
    }

    public String getStu3() {
        return stu3;
    }

    public void setStu3(String stu3) {
        this.stu3 = stu3;
    }

    public String getR4() {
        return r4;
    }

    public void setR4(String r4) {
        this.r4 = r4;
    }

    public String getR5() {
        return r5;
    }

    public void setR5(String r5) {
        this.r5 = r5;
    }

    public String getApiBaseURL_dstu2() {
        return apiBaseURL_dstu2;
    }

    public void setApiBaseURL_dstu2(String apiBaseURL_dstu2) {
        this.apiBaseURL_dstu2 = apiBaseURL_dstu2;
    }

    public String getApiBaseURL_stu3() {
        return apiBaseURL_stu3;
    }

    public void setApiBaseURL_stu3(String apiBaseURL_stu3) {
        this.apiBaseURL_stu3 = apiBaseURL_stu3;
    }

    public String getApiBaseURL_r4() {
        return apiBaseURL_r4;
    }

    public void setApiBaseURL_r4(String apiBaseURL_r4) {
        this.apiBaseURL_r4 = apiBaseURL_r4;
    }

    public String getApiBaseURL_r5() {
        return apiBaseURL_r5;
    }

    public void setApiBaseURL_r5(String apiBaseURL_r5) {
        this.apiBaseURL_r5 = apiBaseURL_r5;
    }

    public FhirVersion() { }

    public FhirVersion(String dstu2, String stu3, String r4, String r5) {
        this.dstu2 = dstu2;
        this.stu3 = stu3;
        this.r4 = r4;
        this.r5 = r5;
    }

}
