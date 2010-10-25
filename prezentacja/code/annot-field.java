@Id
@Length(min = 3, max = 15)
@UserPrincipal
public String getUsername() {
    return username;
}