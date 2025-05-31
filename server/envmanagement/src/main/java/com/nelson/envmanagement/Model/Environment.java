package com.nelson.envmanagement.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "environment")
public class Environment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    private String name;
    private String containerId;
    private String status;
    private Integer port;
    private String envVars;
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContainerId() { return containerId; }
    public void setContainerId(String containerId) { this.containerId = containerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getEnvVars() { return envVars; }
    public void setEnvVars(String envVars) { this.envVars = envVars; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
