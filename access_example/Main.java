import access_example.UserId;
import access_example.UserInfo;
import access_example.MedicalRecordEntry;
import access_example.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Optional;

public class Main {
  private List<MedicalRecordEntry> records = new ArrayList<>();
  private Log log;
  private HashMap<UserId, UserInfo> users;

  public static void main(String[] args) {
    UserId id_1 = new UserId(130);
    UserInfo info_1 = UserInfo.newPatient("John Doe");
    HashMap<UserId, UserInfo> map = new HashMap<>();
    map.put(id_1, info_1);
    System.out.println(map);
    System.out.println(info_1.name);
  }
}

