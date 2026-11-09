package org.delcom.ifs25041pbo2026springbootcashflow;

import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point aplikasi. {@link DotEnv#load()} dipanggil sebelum Spring dimulai
 * supaya {@code APP_PORT} dari {@code .env} sudah terbaca oleh {@code application.yaml}.
 */
@SpringBootApplication
public class App implements AppShellConfigurator {

    public static void main(String[] args) {
        DotEnv.load();
        SpringApplication.run(App.class, args);
    }
}
