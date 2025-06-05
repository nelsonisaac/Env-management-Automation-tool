
package com.nelson.envmanagement.controller;

import com.nelson.envmanagement.Model.Deployment;
import com.nelson.envmanagement.Model.Environment;
import com.nelson.envmanagement.Model.User;
import com.nelson.envmanagement.Repository.DeploymentRepository;
import com.nelson.envmanagement.Repository.EnvironmentRepository;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5143")
@RestController
@RequestMapping("/api/environments")
public class DeploymentController {
    private final EnvironmentRepository environmentRepository;
    private final DeploymentRepository deploymentRepository;

    public DeploymentController(EnvironmentRepository environmentRepository, DeploymentRepository deploymentRepository) {
        this.environmentRepository = environmentRepository;
        this.deploymentRepository = deploymentRepository;
    }

    @GetMapping("/{id}/deployments")
    public ResponseEntity<?> getDeployments(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!environmentRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Environment not found");
        }
        List<Deployment> deployments = deploymentRepository.findByEnvironmentId(id);
        return ResponseEntity.ok(deployments);
    }

    @PostMapping("/{id}/deploy")
    public ResponseEntity<?> deployWar(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Environment environment = environmentRepository.findById(id)
                    .filter(env -> env.getUser().getEmail().equals(email))
                    .orElse(null);
            if (environment == null) return ResponseEntity.status(404).body("Environment not found");
            if (!file.getOriginalFilename().endsWith(".war")) return ResponseEntity.status(400).body("File must be a WAR");

            String containerId = environment.getContainerId();
            if (containerId == null) return ResponseEntity.status(400).body("Container ID not found");

            // Save the WAR file to a temporary location
            File tempFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            file.transferTo(tempFile);
            System.out.println(tempFile.toString());
            // Copy WAR to WildFly's deployments directory
            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "cp", tempFile.getAbsolutePath(),
                    containerId + ":/opt/jboss/wildfly/standalone/deployments/"
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            tempFile.delete();

            if (exitCode != 0) {
                throw new RuntimeException("Failed to copy WAR file to container");
            }

            Deployment deployment = new Deployment();
            deployment.setEnvironment(environment);
            deployment.setWarFileName(file.getOriginalFilename());
            deployment.setStatus("success");
            deployment.setDeployedAt(LocalDateTime.now());
            deploymentRepository.save(deployment);

            return ResponseEntity.ok("WAR deployed to environment " + id);
        } catch (Exception e) {
            e.printStackTrace();
            Deployment deployment = new Deployment();
            deployment.setEnvironment(environmentRepository.findById(id).orElse(null));
            deployment.setWarFileName(file.getOriginalFilename());
            deployment.setStatus("failed");
            deployment.setDeployedAt(LocalDateTime.now());
            deploymentRepository.save(deployment);
            return ResponseEntity.status(400).body("Deployment failed: " + e.toString());
        }
    }
}