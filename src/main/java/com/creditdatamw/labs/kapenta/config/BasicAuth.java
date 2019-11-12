package com.creditdatamw.labs.kapenta.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BasicAuth {

    @JsonProperty(required = false)
    private User user;

    @JsonProperty(required = false)
    private List<User> users;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public static class User {
        @JsonProperty
        private String username;

        @JsonProperty
        private String password;

        public User() {
        }

        public User(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            User user = (User) o;

            if (!username.equals(user.username)) return false;
            return password.equals(user.password);
        }

        @Override
        public int hashCode() {
            int result = username.hashCode();
            result = 31 * result + password.hashCode();
            return result;
        }
    }
}
