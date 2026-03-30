package com.porasl.authservices.user;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.annotation.Transient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.porasl.authservices.connection.UserConnection;
import com.porasl.authservices.connection.UserConnection.Status;
import com.porasl.authservices.token.Token;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "user")
public class User implements UserDetails {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue
	private long id;
	private String firstname;
	private String lastname;
	private String email;
	private String password;
	private String activationcode;
	private boolean status;
	private int type;
	private int updatedby;
	private boolean approved;
	private boolean blocked;
	private boolean accountNonExpired;
	private String phoneNumber;

	@Column(name = "updateddate")
	@Temporal(TemporalType.TIMESTAMP)
	private long updatedDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "createddate")
	private long createdDate;

	@Enumerated(EnumType.STRING)
	private Role role;

	@Column(name = "is_placeholder", nullable = false)
	private boolean isPlaceholder;
	
	private String profileImageUrl;

	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Token> tokens;

	@OneToMany(mappedBy = "requester", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<UserConnection> sentConnections;

	@OneToMany(mappedBy = "target", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<UserConnection> receivedConnections;

	public User() {}

	public User(long id, String firstname, String lastname, String email, String password, String activationcode,
				boolean status, int type, int updatedby, boolean approved, boolean blocked, boolean accountNonExpired,
				String phoneNumber, long updatedDate, long createdDate, Role role, boolean isPlaceholder, String profileImageUrl) {
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.password = password;
		this.activationcode = activationcode;
		this.status = status;
		this.type = type;
		this.updatedby = updatedby;
		this.approved = approved;
		this.blocked = blocked;
		this.accountNonExpired = accountNonExpired;
		this.phoneNumber = phoneNumber;
		this.updatedDate = updatedDate;
		this.createdDate = createdDate;
		this.role = role;
		this.isPlaceholder = isPlaceholder;
		this.profileImageUrl = profileImageUrl;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return role.getAuthorities();
	}

	@Override
	public String getPassword() { return password; }

	@Override
	public String getUsername() { return email; }

	public long getId() { return id; }
	public void setId(long id) { this.id = id; }
	public String getFirstname() { return firstname; }
	public void setFirstname(String firstname) { this.firstname = firstname; }
	public String getLastname() { return lastname; }
	public void setLastname(String lastname) { this.lastname = lastname; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public void setPassword(String password) { this.password = password; }
	public String getActivationcode() { return activationcode; }
	public void setActivationcode(String activationcode) { this.activationcode = activationcode; }
	public boolean getStatus() { return status; }
	public void setStatus(boolean status) { this.status = status; }
	public int getType() { return type; }
	public void setType(int type) { this.type = type; }
	public int getUpdatedby() { return updatedby; }
	public void setUpdatedby(int updatedby) { this.updatedby = updatedby; }
	public boolean isApproved() { return approved; }
	public void setApproved(boolean approved) { this.approved = approved; }
	public boolean isBlocked() { return blocked; }
	public void setBlocked(boolean blocked) { this.blocked = blocked; }
	public boolean isAccountNonExpired() { return accountNonExpired; }
	public void setAccountNonExpired(boolean accountNonExpired) { this.accountNonExpired = accountNonExpired; }
	public String getPhoneNumber() { return phoneNumber; }
	public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
	public long getUpdatedDate() { return updatedDate; }
	public void setUpdatedDate(long updatedDate) { this.updatedDate = updatedDate; }
	public long getCreatedDate() { return createdDate; }
	public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }
	public Role getRole() { return role; }
	public void setRole(Role role) { this.role = role; }
	public boolean isPlaceholder() { return isPlaceholder; }
	public void setPlaceholder(boolean isPlaceholder) { this.isPlaceholder = isPlaceholder; }
	public String getProfileImageUrl() { return profileImageUrl; }
	public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
	public List<Token> getTokens() { return tokens; }
	public void setTokens(List<Token> tokens) { this.tokens = tokens; }
	public List<UserConnection> getSentConnections() { return sentConnections; }
	public void setSentConnections(List<UserConnection> sentConnections) { this.sentConnections = sentConnections; }
	public List<UserConnection> getReceivedConnections() { return receivedConnections; }
	public void setReceivedConnections(List<UserConnection> receivedConnections) { this.receivedConnections = receivedConnections; }

	@Override
	public boolean isAccountNonLocked() { return true; }

	@Override
	public boolean isCredentialsNonExpired() { return true; }

	@Override
	public boolean isEnabled() { return true; }

	@Transient
	public List<User> getFriends() {
	    return Stream.concat(
	            sentConnections.stream()
	                .filter(c -> c.getStatus() == UserConnection.Status.ACCEPTED)
	                .map(UserConnection::getTarget),
	            receivedConnections.stream()
	                .filter(c -> c.getStatus() == UserConnection.Status.ACCEPTED)
	                .map(UserConnection::getRequester)
	    ).toList();
	}

	@Override
	public String toString() {
	    return "User{id=" + id + 
	           ", email='" + email + '\'' + 
	           ", firstname='" + firstname + '\'' + 
	           ", lastname='" + lastname + '\'' +
	           '}';
	}

	public void setStatus(Status accepted) {
		// TODO Auto-generated method stub
	}

	public static UserBuilder builder() {
		return new UserBuilder();
	}

	public static class UserBuilder {
		private String firstname;
		private String lastname;
		private String email;
		private String password;
		private String activationcode;
		private Role role;
		private boolean status;
		private boolean approved;
		private boolean blocked;
		private boolean accountNonExpired;
		private long createdDate;
		private long updatedDate;
		private String profileImageUrl;

		public UserBuilder firstname(String firstname) { this.firstname = firstname; return this; }
		public UserBuilder lastname(String lastname) { this.lastname = lastname; return this; }
		public UserBuilder email(String email) { this.email = email; return this; }
		public UserBuilder password(String password) { this.password = password; return this; }
		public UserBuilder activationcode(String activationcode) { this.activationcode = activationcode; return this; }
		public UserBuilder role(Role role) { this.role = role; return this; }
		public UserBuilder status(boolean status) { this.status = status; return this; }
		public UserBuilder approved(boolean approved) { this.approved = approved; return this; }
		public UserBuilder blocked(boolean blocked) { this.blocked = blocked; return this; }
		public UserBuilder accountNonExpired(boolean accountNonExpired) { this.accountNonExpired = accountNonExpired; return this; }
		public UserBuilder createdDate(long createdDate) { this.createdDate = createdDate; return this; }
		public UserBuilder updatedDate(long updatedDate) { this.updatedDate = updatedDate; return this; }
		public UserBuilder profileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; return this; }

		public Object accountNonLocked(boolean accountNonExpired) { return accountNonExpired; }
		public UserBuilder credentialsNonExpired(boolean accountNonExpired) { return this; }

		public User build() {
			User user = new User();
			user.firstname = this.firstname;
			user.lastname = this.lastname;
			user.email = this.email;
			user.password = this.password;
			user.activationcode = this.activationcode;
			user.role = this.role;
			user.status = this.status;
			user.approved = this.approved;
			user.blocked = this.blocked;
			user.accountNonExpired = this.accountNonExpired;
			user.createdDate = this.createdDate;
			user.updatedDate = this.updatedDate;
			user.profileImageUrl = this.profileImageUrl;
			return user;
		}
	}
}