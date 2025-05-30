package com.nelson.envmanagement.Repository;

import com.nelson.envmanagement.Model.Deployment;
import com.nelson.envmanagement.Model.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeploymentRepository extends JpaRepository<Deployment, Long> {
    List<Deployment> findByEnvironment(Environment environment);
}