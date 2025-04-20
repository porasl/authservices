package com.porasl.authservices.user;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.porasl.authservices.user.Permission.*;

public enum Role {

  USER(Collections.emptySet()),

  ADMIN(Set.of(
    ADMIN_READ, ADMIN_UPDATE, ADMIN_DELETE, ADMIN_CREATE,
    MANAGER_READ, MANAGER_UPDATE, MANAGER_DELETE, MANAGER_CREATE
  )),

  MANAGER(Set.of(
    MANAGER_READ, MANAGER_UPDATE, MANAGER_DELETE, MANAGER_CREATE
  ));

  private final Set<Permission> permissions;

  // ✅ Define constructor (REQUIRED for enums)
  Role(Set<Permission> permissions) {
    this.permissions = permissions;
  }

  public Set<Permission> getPermissions() {
    return permissions;
  }

  public List<SimpleGrantedAuthority> getAuthorities() {
    List<SimpleGrantedAuthority> authorities = permissions.stream()
      .map(p -> new SimpleGrantedAuthority(p.getPermission()))
      .collect(Collectors.toList());
    authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
    return authorities;
  }
}
