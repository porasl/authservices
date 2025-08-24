package com.porasl.authservices.user;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.porasl.authservices.token.Token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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

	@Column(name = "updateddate")
	@Temporal(TemporalType.TIMESTAMP)
	private long updatedDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "createddate")
	private long createdDate;

	@Enumerated(EnumType.STRING)
	private Role role;

	@OneToMany(mappedBy = "user")
	private List<Token> tokens;
	
	private String profileImageUrl;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return role.getAuthorities();
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	// Additional getters for missing methods
	public Role getRole() {
		return role;
	}

	public String getActivationcode() {
		return activationcode;
	}

	public void setActivationcode(String activationcode) {
		this.activationcode = activationcode;
	}

	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	// Additional getters/setters for missing methods
	public long getId() {
		return id;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	// Manual builder method
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
		private long createdDate;
		private long updatedDate;
		private String profileImageUrl;

		public UserBuilder firstname(String firstname) {
			this.firstname = firstname;
			return this;
		}

		public UserBuilder lastname(String lastname) {
			this.lastname = lastname;
			return this;
		}

		public UserBuilder email(String email) {
			this.email = email;
			return this;
		}

		public UserBuilder password(String password) {
			this.password = password;
			return this;
		}

		public UserBuilder activationcode(String activationcode) {
			this.activationcode = activationcode;
			return this;
		}

		public UserBuilder role(Role role) {
			this.role = role;
			return this;
		}

		public UserBuilder status(boolean status) {
			this.status = status;
			return this;
		}

		public UserBuilder approved(boolean approved) {
			this.approved = approved;
			return this;
		}

		public UserBuilder blocked(boolean blocked) {
			this.blocked = blocked;
			return this;
		}

		public UserBuilder createdDate(long createdDate) {
			this.createdDate = createdDate;
			return this;
		}

		public UserBuilder updatedDate(long updatedDate) {
			this.updatedDate = updatedDate;
			return this;
		}

		public UserBuilder profileImageUrl(String profileImageUrl) {
			this.profileImageUrl = profileImageUrl;
			return this;
		}

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
			user.createdDate = this.createdDate;
			user.updatedDate = this.updatedDate;
			user.profileImageUrl = this.profileImageUrl;
			return user;
		}
	}
}