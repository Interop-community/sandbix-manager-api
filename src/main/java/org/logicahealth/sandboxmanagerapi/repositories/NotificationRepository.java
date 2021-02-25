package org.logicahealth.sandboxmanagerapi.repositories;

import org.logicahealth.sandboxmanagerapi.model.Notification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Integer> {
    List<Notification> findByUserId(@Param("userId") Integer userId);

    List<Notification> findByNewsItemId(@Param("newsItemId") Integer newsItemId);
}
