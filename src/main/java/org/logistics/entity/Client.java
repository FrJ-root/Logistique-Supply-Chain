package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clients")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String contactInfo;

    public static ClientBuilder builder() {
        return new ClientBuilder();
    }

    public static class ClientBuilder {
        private Long id;
        private String name;
        private String contactInfo;

        public ClientBuilder id(Long id) { this.id = id; return this; }
        public ClientBuilder name(String name) { this.name = name; return this; }
        public ClientBuilder contactInfo(String contactInfo) { this.contactInfo = contactInfo; return this; }

        public Client build() {
            return new Client(id, name, contactInfo);
        }
    }
}
