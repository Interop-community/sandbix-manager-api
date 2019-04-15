package org.hspconsortium.sandboxmanagerapi.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@ConfigurationProperties("tier-rules")
@Configuration
public class RulesList {
    private HashMap<String, Rule> tierRuleList;

    private Double threshold;

    public RulesList(HashMap<String, Rule> tierRuleList, Double threshold) {
        this.tierRuleList = tierRuleList;
        this.threshold = threshold;
    }

    public RulesList(HashMap<String, Rule> tierRuleList) {
        this.tierRuleList = tierRuleList;
    }

    public RulesList() { }

    public HashMap<String, Rule> getTierRuleList() {
        return tierRuleList;
    }

    public void setTierRuleList(HashMap<String, Rule> tierRuleList) {
        this.tierRuleList = tierRuleList;
    }

    public Double getThreshold() { return threshold; }

    public void setThreshold(Double threshold) { this.threshold = threshold; }
}
