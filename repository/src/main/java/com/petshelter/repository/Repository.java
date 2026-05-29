package com.petshelter.repository;

import com.petshelter.exception.ShelterException;

import java.util.List;
import java.util.Optional;

// Generic CRUD contract for all repositories.
public interface Repository<T, ID> {
    T save(T entity) throws ShelterException;
    T update(T entity) throws ShelterException;

    Optional<T> findById(ID id) throws ShelterException;
    List<T> findAll() throws ShelterException;

    boolean deleteById(ID id) throws ShelterException;

    boolean existsById(ID id) throws ShelterException;

    long count() throws ShelterException;
}