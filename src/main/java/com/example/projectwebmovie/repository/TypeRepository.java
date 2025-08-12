package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TypeRepository extends JpaRepository<Type, Integer> {
    @Query("SELECT t FROM Type t WHERE t.typeId IN :ids")
    List<Type> findAllByIdIn(@Param("ids") Collection<Integer> ids);

}
