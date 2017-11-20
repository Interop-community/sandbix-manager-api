package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.NewsItem;
import org.springframework.data.repository.CrudRepository;

public interface NewsItemRepository extends CrudRepository<NewsItem, Integer> {

}
