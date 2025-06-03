package com.nelson.envmanagement.controller;

import com.nelson.envmanagement.Model.Environment;
import com.nelson.envmanagement.Model.User;
import com.nelson.envmanagement.Repository.EnvironmentRepository;
import com.nelson.envmanagement.Repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/environments")
public class EnvironmentController {
    private final EnvironmentRepository environmentRepository;
    private final UserRepository userRepository;

    public EnvironmentController(EnvironmentRepository environmentRepository, UserRepository userRepository) {
        this.environmentRepository = environmentRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createEnvironment(@Valid @RequestBody EnvironmentRequest request) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email);
            if (user == null) return ResponseEntity.status(401).body("User not found");
            System.out.println("envVars: " +request.getEnvVars());
            String containerName = "wildfly-" + System.currentTimeMillis();
            String envVarsFormatted = request.getEnvVars().stream()
                    .map(s -> "\\\"" + s + "\\\"")  // escape double quotes
                    .collect(Collectors.joining(","));
            String fullEnvVar = "[ " + envVarsFormatted + " ]"; // adding [] so for HCL (terrform language)
            String terraformPath = "/mnt/c/ProgramData/chocolatey/bin/terraform.exe";
            String command = String.format(
                    "cd infra && %s init && %s apply -auto-approve -var='container_name=%s' -var='port=%d' -var='env_vars=%s'",
                    terraformPath, terraformPath, containerName, request.getPort(), fullEnvVar); // %s denotes the arguments passed at last

            ProcessBuilder pb = new ProcessBuilder("bash", "-c",command);
            pb.redirectErrorStream(true);
            System.out.println("Running command: " + pb.command());

            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);  // print terraform output
                }
            }

            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.err.println(line); // print terraform error output
                }
            }

            int exitCode = process.waitFor();
            System.out.println("Terraform process exited with code: " + exitCode);

            //start the docker container that was created using terraform
            Process process2 = new ProcessBuilder("docker", "ps", "-q", "-f", "name=" + containerName).start();
            String containerId;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process2.getInputStream()))) {
                containerId = reader.lines().findFirst().orElse(null);
                System.out.println("contaierId: " + containerId);
            }

            Environment environment = new Environment();
            environment.setUser(user);
            environment.setName(request.getName());
            environment.setContainerId(containerId);
            environment.setStatus("running");
            environment.setPort(request.getPort());
            environment.setEnvVars(String.join(",", request.getEnvVars()));
            environment.setCreatedAt(LocalDateTime.now());
            environmentRepository.save(environment);

            return ResponseEntity.status(201).body(environment);
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(400).body("Environment creation failed: " + e.getMessage());
        }
    }

    @GetMapping()
    public List<Environment> getAllEnvironments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email);
        System.out.println(user);
        return environmentRepository.findByUser(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Environment> getEnvironmentDetails(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email);
        Environment environment = environmentRepository.findById(id)
                .filter(env -> env.getUser().getId().equals(user.getId()))
                .orElse(null);
        if(environment == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(environment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEnvironment(@PathVariable Long id) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email);
            Environment environment = environmentRepository.findById(id)
                    .filter(env -> env.getUser().getId().equals(user.getId()))
                    .orElse(null);
            if (environment == null) return ResponseEntity.status(404).body("Environment not found");

            ProcessBuilder pb = new ProcessBuilder("docker", "stop", environment.getContainerId());
            pb.start().waitFor();
            pb = new ProcessBuilder("docker", "rm", environment.getContainerId());
            pb.start().waitFor();

            environmentRepository.delete(environment);
            return ResponseEntity.ok("Environment deleted");
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(400).body("Environment deletion failed: " + e.getMessage());
        }
    }
}

class EnvironmentRequest {
    @NotBlank(message = "Name is required")
    private String name;
    @Min(value = 1024, message = "Port must be between 1024 and 65535")
    private Integer port;
    private List<String> envVars;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public List<String> getEnvVars() { return envVars; }
    public void setEnvVars(List<String> envVars) { this.envVars = envVars; }
}