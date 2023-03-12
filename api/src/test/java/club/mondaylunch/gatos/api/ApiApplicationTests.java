package club.mondaylunch.gatos.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import club.mondaylunch.gatos.api.auth.SecurityConfig;

@SpringBootTest
@ComponentScan(basePackages = "club.mondaylunch.gatos.api", excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = { SecurityConfig.class })
})
class ApiApplicationTests {

    @Test
    void contextLoads() {
    }
}
