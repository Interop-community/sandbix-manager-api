package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@NamedQueries({
        // Used to retrieve a UserLaunch to update it with a launch or to apply lastLaunchSeconds to a launch scenario
        @NamedQuery(name="UserLaunch.findByUserIdAndLaunchScenarioId",
        query="SELECT c FROM UserLaunch c WHERE c.user.sbmUserId = :sbmUserId and c.launchScenario.id = :launchScenarioId"),
        // Used to retrieve a UserLaunch to update it with a launch or to apply lastLaunchSeconds to a CDS launch scenario
        @NamedQuery(name="UserLaunch.findByUserIdAndLaunchScenarioCdsId",
                query="SELECT c FROM UserLaunch c WHERE c.user.sbmUserId = :sbmUserId and c.launchScenarioCds.id = :launchScenarioCdsId"),
        // Used to delete a user's UserLaunch's when they are removed from a sandbox
        @NamedQuery(name="UserLaunch.findByUserId",
                query="SELECT c FROM UserLaunch c WHERE c.user.sbmUserId = :sbmUserId"),
        // Used to delete a user's UserLaunch's when a launch scenario is deleted
        @NamedQuery(name="UserLaunch.findByLaunchScenarioId",
                query="SELECT c FROM UserLaunch c WHERE c.launchScenario.id = :launchScenarioId"),
        // Used to delete a user's UserLaunch's when a CDS launch scenario is deleted
        @NamedQuery(name="UserLaunch.findByLaunchScenarioCdsId",
                query="SELECT c FROM UserLaunch c WHERE c.launchScenarioCds.id = :launchScenarioCdsId")
})
// UserLaunch is used to track the time a given user launched a given launch scenario
// for the purpose of showing recent launch scerarios in sandbox manager
public class UserLaunch {
    private Integer id;
    private User user;
    private LaunchScenario launchScenario;
    private LaunchScenarioCds launchScenarioCds;
    private Timestamp lastLaunch;
    private Long lastLaunchSeconds;

    public UserLaunch() {}

    public UserLaunch(User user, LaunchScenario launchScenario, LaunchScenarioCds launchScenarioCds, Timestamp lastLaunch) {
        this.user = user;
        this.launchScenario = launchScenario;
        this.launchScenarioCds = launchScenarioCds;
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
    @JoinColumn(name="launch_scenario_id")
    public LaunchScenario getLaunchScenario() {
        return launchScenario;
    }

    public void setLaunchScenario(LaunchScenario launchScenario) {
        this.launchScenario = launchScenario;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="launch_scenario_cds_id")
    public LaunchScenarioCds getLaunchScenarioCds() {
        return launchScenarioCds;
    }

    public void setLaunchScenarioCds(LaunchScenarioCds launchScenarioCds) {
        this.launchScenarioCds = launchScenarioCds;
    }
}
