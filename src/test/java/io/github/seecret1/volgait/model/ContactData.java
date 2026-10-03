package io.github.seecret1.volgait.model;

public record ContactData(String name, String email, String message) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String name = "";
        private String email = "";
        private String message = "";

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public ContactData build() {
            return new ContactData(name, email, message);
        }
    }
}

