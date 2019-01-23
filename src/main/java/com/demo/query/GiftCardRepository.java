package com.demo.query;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@Profile("query")
@RepositoryRestResource(collectionResourceRel = "cards", path = "querycards")
interface GiftCardRepository extends JpaRepository<GiftCardEntity, String> {

    @RestResource(exported = false)
    @Override
    void deleteById(String aLong);

    @RestResource(exported = false)
    @Override
    void delete(GiftCardEntity entity);

    @RestResource(exported = false)
    @Override
    void deleteAll(Iterable<? extends GiftCardEntity> entities);

    @RestResource(exported = false)
    @Override
    void deleteAll();

    @RestResource(exported = false)
    @Override
    <S extends GiftCardEntity> S save(S entity);

    @RestResource(exported = false)
    @Override
    <S extends GiftCardEntity> List<S> saveAll(Iterable<S> entities);

}
