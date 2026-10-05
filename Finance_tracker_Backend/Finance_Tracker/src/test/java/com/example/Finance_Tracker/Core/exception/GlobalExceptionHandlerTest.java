package com.example.Finance_Tracker.Core.exception;

import com.example.Finance_Tracker.Transaction.exception.NotFoundException;
import com.example.Finance_Tracker.User.exception.EmailAlreadyExistsException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    /** Throwaway controller that raises each kind of exception the handler must translate. */
    @RestController
    @RequestMapping("/test")
    public static class ThrowingController {

        @GetMapping("/not-found")
        public void notFound() {
            throw new NotFoundException("Transaction not found with id: 42");
        }

        @GetMapping("/forbidden")
        public void forbidden() {
            throw new AccessDeniedException("internal ownership detail");
        }

        @GetMapping("/conflict")
        public void conflict() {
            throw new EmailAlreadyExistsException("User already exists");
        }

        @GetMapping("/bad-input")
        public void badInput() {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        @GetMapping("/boom")
        public void boom() {
            throw new RuntimeException("SQL state 42P01: relation secret_table does not exist");
        }

        @GetMapping("/items/{id}")
        public String item(@PathVariable Long id) {
            return "ok";
        }

        @PostMapping("/validated")
        public String validated(@Valid @RequestBody Payload payload) {
            return "ok";
        }
    }

    public record Payload(@NotBlank(message = "Name is required") String name) {
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void notFound_returns404WithMessageAndPath() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Transaction not found with id: 42"))
                .andExpect(jsonPath("$.path").value("/test/not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void accessDenied_returns403WithGenericMessage() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource"))
                .andExpect(content().string(not(containsString("internal ownership detail"))));
    }

    @Test
    void emailAlreadyExists_returns409() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists"));
    }

    @Test
    void illegalArgument_returns400() throws Exception {
        mockMvc.perform(get("/test/bad-input"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired reset token"));
    }

    @Test
    void unexpectedException_returns500WithoutLeakingDetails() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(content().string(not(containsString("secret_table"))));
    }

    @Test
    void validationFailure_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/test/validated")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").value("Name is required"))
                .andExpect(jsonPath("$.message").value("name: Name is required"));
    }

    @Test
    void malformedJson_returns400InApiErrorShape() throws Exception {
        mockMvc.perform(post("/test/validated")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void pathVariableTypeMismatch_returns400() throws Exception {
        mockMvc.perform(get("/test/items/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void wrongHttpMethod_returns405InApiErrorShape() throws Exception {
        mockMvc.perform(post("/test/not-found"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                .andExpect(jsonPath("$.message").value(containsString("POST")));
    }
}
