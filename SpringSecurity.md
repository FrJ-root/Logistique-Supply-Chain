Spring Security POC — Logistics Project
Table of Contents

Contexte et Objectif

Concepts de Base

Architecture Spring Security

Configuration Spring Security

Basic Auth Implementation

Roles et Protection des Endpoints

Test & Validation

Bonnes pratiques

Contexte et Objectif

Ce projet a pour objectif d’implémenter une couche de sécurité Basic Auth pour le backend Spring Boot d’une application logistique.

Fonctionnalités sécurisées :

/api/admin/** → ADMIN uniquement

/api/inventory/**, /api/shipments/** → ADMIN et WAREHOUSE_MANAGER

/api/orders/**, /api/client/** → ADMIN et CLIENT

Authentification stateless via Basic Auth

Sessions désactivées pour les APIs REST

Concepts de Base
1. Authentification vs Autorisation

Authentification : vérifier l’identité de l’utilisateur (login/password)

Autorisation : vérifier si l’utilisateur a le droit d’accéder à une ressource (roles/permissions)

2. Sécurité Web courante

Attaques : brute force, XSS, CSRF, session fixation, vol de session

HTTPS obligatoire pour sécuriser Basic Auth

APIs REST → sécurité backend essentielle

3. Défense en profondeur

Plusieurs couches de sécurité : Spring Security, HTTPS, validation côté serveur

Architecture Spring Security

SecurityFilterChain : pipeline des filtres de sécurité

DelegatingFilterProxy : relaye les requêtes HTTP vers Spring Security

AuthenticationManager : valide les credentials via un AuthenticationProvider

AuthenticationProvider : stratégie de validation (ex: DaoAuthenticationProvider pour DB)

UserDetailsService : charge les utilisateurs depuis la DB

PasswordEncoder : encode/compare les mots de passe (BCrypt)

Roles / Authorities : rôle (ADMIN) → authority (ROLE_ADMIN)

Flux d’une requête sécurisée

Requête HTTP → SecurityFilterChain

BasicAuthenticationFilter → extraction header Authorization

AuthenticationManager → AuthenticationProvider → UserDetailsService

Vérification mot de passe via PasswordEncoder

Autorisation → vérification des roles/authorities

Requête transmise au Controller

Configuration Spring Security
@Bean
public PasswordEncoder passwordEncoder() {
return new BCryptPasswordEncoder();
}

@Bean
public DaoAuthenticationProvider authProvider() {
DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
provider.setUserDetailsService(userDetailsService);
provider.setPasswordEncoder(passwordEncoder());
return provider;
}

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
http
.csrf(csrf -> csrf.disable())
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
.authorizeHttpRequests(auth -> auth
.requestMatchers("/api/admin/**").hasRole("ADMIN")
.requestMatchers("/api/inventory/**", "/api/shipments/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
.requestMatchers("/api/orders/**", "/api/client/**").hasAnyRole("ADMIN", "CLIENT")
.anyRequest().authenticated()
)
.httpBasic(Customizer.withDefaults());

    return http.build();
}


CSRF désactivé pour APIs REST

Sessions stateless

Basic Auth utilisée via HTTPS obligatoire

Basic Auth Implementation
Fonctionnement

Header HTTP Authorization: Basic <base64(username:password)>

Décodé et validé par BasicAuthenticationFilter

Passwords stockés hachés avec BCrypt

Exemple utilisateur ADMIN
Username	Password	Role
admin	admin123	ADMIN
Roles et Protection des Endpoints
Endpoint	Roles autorisés
/api/admin/**	ADMIN
/api/inventory/**, /api/shipments/**	ADMIN, WAREHOUSE_MANAGER
/api/orders/**, /api/client/**	ADMIN, CLIENT
Exemples de controllers
@RestController
@RequestMapping("/api/admin")
public class AdminController {
@GetMapping("/dashboard")
public String adminDashboard() {
return "Welcome ADMIN!";
}
}

@RestController
@RequestMapping("/api/client")
public class ClientController {
@GetMapping("/dashboard")
public String clientDashboard() {
return "Welcome CLIENT!";
}
}

Test & Validation
Postman / cURL

Auth Type: Basic Auth

URL: http://localhost:8082/api/admin/dashboard

Username / Password: admin/admin123

Response: "Welcome ADMIN!"

Tester pour les rôles CLIENT et WAREHOUSE_MANAGER également.

Bonnes pratiques

Pas de mot de passe en clair

BCrypt + salage pour hachage

Roles centralisés dans l’enum Role

APIs REST → stateless

HTTPS obligatoire pour Basic Auth

✅ Avec cette configuration et documentation, vous avez un POC complet Spring Security Basic Auth pour tous les rôles du projet.