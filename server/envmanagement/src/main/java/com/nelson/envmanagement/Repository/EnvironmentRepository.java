package com.nelson.envmanagement.Repository;

import com.nelson.envmanagement.Model.Environment;
import com.nelson.envmanagement.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
    List<Environment> findByUser(User user);
}
