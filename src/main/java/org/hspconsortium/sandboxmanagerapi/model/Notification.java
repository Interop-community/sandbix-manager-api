package org.hspconsortium.sandboxmanagerapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@NamedQueries({
        @NamedQuery(name = "Notification.findByUserId",
                query = "SELECT c FROM Notification c WHERE c.user.id = :userId AND c.hidden = false"),
        @NamedQuery(name = "Notification.findByNewsItemId",
                query = "SELECT c FROM Notification c WHERE c.newsItem.id = :newsItemId"),
})
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Timestamp createdTimestamp;

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="news_item_id")
    private NewsItem newsItem;

    private Boolean seen;

    private Boolean hidden;
}
