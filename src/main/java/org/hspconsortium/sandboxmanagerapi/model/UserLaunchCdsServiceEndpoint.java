package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hspconsortium.sandboxmanagerapi.model.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@NamedQueries({
        // Used to retrieve a UserLaunch to update it with a launch or to apply lastLaunchSeconds to a CDS launch scenario
        @NamedQuery(name="UserLaunchCdsServiceEndpoint.findByUserIdAndLaunchScenarioCdsServiceEndpointId",
                query="SELECT c FROM UserLaunchCdsServiceEndpoint c WHERE c.user.sbmUserId = :sbmUserId and c.launchScenarioCdsServiceEndpoint.id = :launchScenarioCdsServiceEndpointId"),
        // Used to delete a user's UserLaunch's when they are removed from a sandbox
        @NamedQuery(name="UserLaunchCdsServiceEndpoint.findByUserId",
                query="SELECT c FROM UserLaunchCdsServiceEndpoint c WHERE c.user.sbmUserId = :sbmUserId"),
        // Used to delete a user's UserLaunch's when a CDS launch scenario is deleted
        @NamedQuery(name="UserLaunchCdsServiceEndpoint.findByLaunchScenarioCdsServiceEndpointId",
                query="SELECT c FROM UserLaunchCdsServiceEndpoint c WHERE c.launchScenarioCdsServiceEndpoint.id = :launchScenarioCdsServiceEndpointId")
})
// UserLaunch is used to track the time a given user launched a given launch scenario
// for the purpose of showing recent launch scerarios in sandbox manager
public class UserLaunchCdsServiceEndpoint {
    private Integer id;
    private User user;
    private LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint;
    private Timestamp lastLaunch;
    private Long lastLaunchSeconds;

    public UserLaunchCdsServiceEndpoint() {}

    public UserLaunchCdsServiceEndpoint(User user, LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint, Timestamp lastLaunch) {
        this.user = user;
        this.launchScenarioCdsServiceEndpoint = launchScenarioCdsServiceEndpoint;
        this.lastLaunch = lastLaunch;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="user_id")
    @JsonIgnoreProperties(ignoreUnknown = true, allowSetters = true,
            value={"sandboxes", "termsOfUseAcceptances", "systemRoles"})
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @JsonIgnore
    public Timestamp getLastLaunch() {
        return lastLaunch;
    }

    public void setLastLaunch(Timestamp lastLaunch) {
        this.lastLaunch = lastLaunch;
    }

    @Transient
    public Long getLastLaunchSeconds() {
        if (this.lastLaunch != null) {
            this.lastLaunchSeconds = this.lastLaunch.getTime();
        }
        return this.lastLaunchSeconds;
    }

    public void setLastLaunchSeconds(Long lastLaunchSeconds) {
        this.lastLaunchSeconds = lastLaunchSeconds;
        if (lastLaunchSeconds != null) {
            this.lastLaunch = new Timestamp(lastLaunchSeconds);
        }
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="launch_scenario_cds_service_endpoint_id")
    public LaunchScenarioCdsServiceEndpoint getLaunchScenarioCdsServiceEndpoint() {
        return launchScenarioCdsServiceEndpoint;
    }

    public void setLaunchScenarioCdsServiceEndpoint(LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint) {
        this.launchScenarioCdsServiceEndpoint = launchScenarioCdsServiceEndpoint;
    }
}
