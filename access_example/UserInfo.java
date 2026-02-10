
package access_example;
import java.util.Optional;
public class UserInfo {
  public String name;
  public Optional<String> division;

  public UserInfo(String name, Optional<String> division) {
    this.name = name;
    this.division = division;
  }
  public static UserInfo newPatient(String name){
    return new UserInfo(name, Optional.empty());
  }
}
