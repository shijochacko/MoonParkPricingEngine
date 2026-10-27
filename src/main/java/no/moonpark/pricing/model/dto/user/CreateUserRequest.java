package no.moonpark.pricing.model.dto.user;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 100) @Pattern(regexp = "^[a-zA-Z0-9._-]+$") String username,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 200) String fullName,
        @NotBlank @Size(min = 12, max = 128) String password,
        @NotEmpty Set<@NotBlank String> roles) {

    @Override
    public String toString() {
        return "CreateUserRequest[username=" + username + ", email=" + email + ", roles=" + roles + ", password=***]";
    }
}
