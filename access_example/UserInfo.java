
package access_example;
import java.util.Optional;
import access_example.UserType;

public class UserInfo {
  public String name;
  public Optional<String> division;
  public UserType type;

  private UserInfo(String name, Optional<String> division, UserType type) {
    this.name = name;
    this.division = division;
    this.type = type; 
  }
  public static UserInfo newPatient(String name){
    return new UserInfo(name, Optional.empty(), UserType.PATIENT);
  }
  public static UserInfo newNurse(String name, String division){
    return new UserInfo(name, Optional.of(division), UserType.NURSE);
  }
  public static UserInfo newDoctor(String name, String division){
    return new UserInfo(name, Optional.of(division), UserType.DOCTOR);
  }
  public static UserInfo newAuthority(String name){
    return new UserInfo(name, Optional.empty(), UserType.AUTHORITY);
  }
}
