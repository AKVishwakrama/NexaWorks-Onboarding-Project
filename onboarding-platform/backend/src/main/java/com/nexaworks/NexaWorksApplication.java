package com.nexaworks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class NexaWorksApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexaWorksApplication.class, args);
        System.out.println("""
            
            ╔══════════════════════════════════════════════════════════════╗
            ║          NexaWorks Onboarding Platform — BACKEND            ║
            ╠══════════════════════════════════════════════════════════════╣
            ║  🚀 API Base:    http://localhost:8080/api                   ║
            ║  🗄️  H2 Console:  http://localhost:8080/h2-console           ║
            ║  ❤️  Health:      http://localhost:8080/api/auth/health      ║
            ╠══════════════════════════════════════════════════════════════╣
            ║  HR Login:       sunita.rao@nexaworks.in  / HR@123456       ║
            ║  Manager Login:  vikram.mehta@nexaworks.in / Mgr@123456     ║
            ║  Employee Login: aarav.sharma@nexaworks.in / Emp@123456     ║
            ╚══════════════════════════════════════════════════════════════╝
            """);
    }
}
