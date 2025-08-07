package com.example.projectwebmovie.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IdGenerateService {

    @PersistenceContext
    private EntityManager entityManager;

    public final int ID_NUMBER_LENGTH = 5;

    @Transactional(readOnly = true)
    public String generateStringId(String prefix, Class<?> entityClass, String idFieldName) {
        // Viết JPQL động
        String jpql = String.format(
                "SELECT e.%s FROM %s e WHERE e.%s LIKE :prefix ORDER BY e.%s DESC",
                idFieldName, entityClass.getSimpleName(), idFieldName, idFieldName);

        List<String> results = entityManager
                .createQuery(jpql, String.class)
                .setParameter("prefix", prefix + "%")
                .setMaxResults(1)
                .getResultList();

        String lastObject = results.isEmpty() ? null : results.get(0);

        return formatNextId(prefix, lastObject);
    }

    private String formatNextId(String prefix, String lastObject) {
        if (lastObject == null || lastObject.isEmpty()) {
            return prefix + String.format("%0" + ID_NUMBER_LENGTH + "d", 1); // PREFIX0001
        }

        try {
            String lastId = lastObject.substring(prefix.length());
            int nextId = Integer.parseInt(lastId) + 1;
            if (String.valueOf(nextId).length() <= ID_NUMBER_LENGTH) {
                return prefix + String.format("%0" + ID_NUMBER_LENGTH + "d", nextId);
            } else {
                // Nếu quá số chữ số, bỏ padding
                return prefix + nextId;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid last ID format: " + lastObject, e);
        }
    }
}
