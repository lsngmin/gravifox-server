package com.gravifox.global.swagger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SwaggerUploadController {

    @PostMapping("/upload-swagger")
    public ResponseEntity<String> uploadSwaggerJson(@RequestBody String swaggerJson) {
        System.out.println("Swagger JSON 수신 완료:");
        System.out.println(swaggerJson);
        return ResponseEntity.ok("Swagger JSON 수신 완료");
    }
}
