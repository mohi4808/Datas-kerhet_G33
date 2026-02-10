import java.util.HashMap;
import java.util.Optional;
import access_example.UserId;
import access_example.UserInfo;

public class UserTable {
  private HashMap<UserId, UserInfo> map;
  public UserTable(){
    this.map = new HashMap<>();
  }
}
