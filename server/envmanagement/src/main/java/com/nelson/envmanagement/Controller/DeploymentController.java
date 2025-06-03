package com.nelson.envmanagement.controller;

import com.nelson.envmanagement.Model.Deployment;
import com.nelson.envmanagement.Model.Environment;
import com.nelson.envmanagement.Model.User;
import com.nelson.envmanagement.Repository.DeploymentRepository;
import com.nelson.envmanagement.Repository.EnvironmentRepository;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
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


    @PostMapping("/{id}/deploy")
    public ResponseEntity<?> deployWar(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Environment environment = environmentRepository.findById(id)
                    .filter(env -> env.getUser().getEmail().equals(email))
                    .orElse(null);
            if (environment == null) return ResponseEntity.status(404).body("Environment not found");
            if (!file.getOriginalFilename().endsWith(".war")) return ResponseEntity.status(400).body("File must be a WAR");

            String managementUrl = String.format("http://localhost:%d/management", environment.getPort());
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost uploadFile = new HttpPost(managementUrl + "/add-content");
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", file.getInputStream(), ContentType.APPLICATION_OCTET_STREAM, file.getOriginalFilename());
            uploadFile.setEntity(builder.build());
            client.execute(uploadFile);

            HttpPost deploy = new HttpPost(managementUrl + "/deploy");
            builder = MultipartEntityBuilder.create();
            builder.addTextBody("name", file.getOriginalFilename());
            deploy.setEntity(builder.build());
            client.execute(deploy);
            client.close();

            Deployment deployment = new Deployment();
            deployment.setEnvironment(environment);
            deployment.setWarFileName(file.getOriginalFilename());
            deployment.setStatus("success");
            deployment.setDeployedAt(LocalDateTime.now());
            deploymentRepository.save(deployment);

            return ResponseEntity.ok("WAR deployed to environment " + id);
        } catch (Exception e) {
            Deployment deployment = new Deployment();
            deployment.setEnvironment(environmentRepository.findById(id).orElse(null));
            deployment.setWarFileName(file.getOriginalFilename());
            deployment.setStatus("failed");
            deployment.setDeployedAt(LocalDateTime.now());
            deploymentRepository.save(deployment);
            return ResponseEntity.status(400).body("Deployment failed: " + e.getMessage());
        }
    }
}