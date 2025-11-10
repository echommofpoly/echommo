package com.poly.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.poly.model.Enemy;

@Repository
public interface EnemyRepository extends JpaRepository<Enemy, Integer> {

    // Find an enemy by name (if names are unique or you need to check)
    Optional<Enemy> findByNameIgnoreCase(String name);

    // Get a random enemy from the table using database-specific function (SQL Server: NEWID())
    // Note: This might not be the most performant way for very large tables.
    // Consider alternative strategies like fetching IDs and picking randomly if performance becomes an issue.
    @Query(value = "SELECT TOP 1 * FROM enemy ORDER BY NEWID()", nativeQuery = true)
    Optional<Enemy> findRandomEnemy();
}