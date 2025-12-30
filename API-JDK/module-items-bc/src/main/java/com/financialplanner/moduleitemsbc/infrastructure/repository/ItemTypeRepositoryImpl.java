package com.financialplanner.moduleitemsbc.infrastructure.repository;

import com.financialplanner.moduleitemsbc.domain.entity.ItemTypeEntity;
import com.financialplanner.moduleitemsbc.domain.model.ItemType;
import com.financialplanner.moduleitemsbc.domain.repository.ItemTypeRepository;
import com.financialplanner.moduleitemsbc.infrastructure.mapper.ItemTypeMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@link ItemTypeRepository} interface for managing
 * {@link ItemTypeEntity} objects. This class serves as a bridge between the
 * persistence layer and the domain layer, enabling CRUD operations while
 * adhering to the repository abstraction.
 *
 * The {@link ItemTypeRepositoryImpl} utilizes a JPA-based repository
 * to perform database interactions and a mapping layer to convert
 * between domain models and persistence entities.
 */
@Repository
public class ItemTypeRepositoryImpl implements ItemTypeRepository {

    /**
     * Reference to the JPA repository used for performing CRUD operations
     * on {@link ItemTypeEntity} instances in the underlying data store.
     *
     * This repository is based on Spring Data JPA and leverages its built-in
     * functionality to simplify database access for the {@link ItemTypeEntity}
     * entity class. The {@code jpa} variable is used as the primary entry point
     * for persistence operations within the {@link ItemTypeRepositoryImpl} implementation.
     */
    private final JpaItemTypeRepository jpa;
    /**
     * Mapper responsible for converting between {@link ItemTypeEntity} and {@link ItemType} objects.
     * This field is used to encapsulate the logic for transforming persistence layer entities
     * into domain models and vice versa.
     *
     * Its primary purpose is to abstract away the mapping logic, ensuring that the repository
     * implementations remain focused on persistence concerns and use domain models for
     * business operations.
     */
    private final ItemTypeMapper mapper;

    /**
     * Creates a new instance of ItemTypeRepositoryImpl, which serves as an implementation
     * of the ItemTypeRepository interface for managing {@link ItemType} entities.
     * This implementation relies on a JPA repository for data persistence and a mapper
     * for converting between domain objects and entity objects.
     *
     * @param jpa    the JPA repository used for accessing and managing {@link ItemTypeEntity} instances; must not be null
     * @param mapper the mapper used for transforming between {@link ItemTypeEntity} and {@link ItemType} instances; must not be null
     */
    public ItemTypeRepositoryImpl(JpaItemTypeRepository jpa, ItemTypeMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    /**
     * Saves the provided {@link ItemTypeEntity} instance and maps it to a domain model {@link ItemType}.
     * This method persists the entity to the database and converts the saved entity
     * into its corresponding domain representation.
     *
     * @param itemType the {@link ItemTypeEntity} object to be persisted; must not be null
     * @return the resulting {@link ItemType} domain model after the entity has been saved
     */
    @Override
    public ItemType save(ItemTypeEntity itemType) {
        ItemTypeEntity saved = jpa.save(itemType);
        return mapper.toDomain(saved);
    }

    /**
     * Retrieves an {@link Optional} containing a domain model {@link ItemType} based on the
     * provided unique identifier. If the entity with the given ID exists in the persistence store,
     * it is transformed into a domain model and returned. If no such entity is found, an empty
     * {@link Optional} is returned.
     *
     * @param id the unique identifier of the {@link ItemType} to be retrieved; must not be null
     * @return an {@link Optional} containing the {@link ItemType} if found, or an empty {@link Optional} if not found
     */
    @Override
    public Optional<ItemType> findById(Long id) {
        return jpa.findById(id)
            .map(mapper::toDomain);
    }

    /**
     * Retrieves all instances of {@link ItemType} from the underlying data source.
     * This method converts each {@link ItemTypeEntity} retrieved from the repository
     * into its corresponding domain model {@link ItemType}, ensuring a separation
     * between the persistence layer and the domain layer.
     *
     * @return a list of {@link ItemType} objects representing all available item types
     */
    @Override
    public List<ItemType> findAll() {
        List<ItemType> list = new ArrayList<>();
        for (ItemTypeEntity itemTypeEntity : jpa.findAll()) {
            ItemType domain = mapper.toDomain(itemTypeEntity);
            list.add(domain);
        }
        return list;
    }

    /**
     * Deletes the entity with the specified unique identifier from the underlying data store.
     * This method removes the record corresponding to the given ID if it exists.
     *
     * @param id the unique identifier of the entity to be deleted; must not be null
     */
    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}

