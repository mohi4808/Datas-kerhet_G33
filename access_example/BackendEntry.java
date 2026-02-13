import access_example.UserId;
import access_example.UserInfo;
import access_example.MedicalRecordEntry;
import access_example.Log;
import access_example.RecordId;
import access_example.AuthenticatedId;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Optional;

public class BackendEntry {
  private List<MedicalRecordEntry> records = new ArrayList<>();
  private Log log;
  private HashMap<UserId, UserInfo> users;
  private int counter;

  public List<MedicalRecordEntry> requestPatientRecords(UserId patient, AuthenticatedId requestor){
    // nurse/doctor tries to access -> retrun those in the same division or with them listed in it 
    // patient tries to access their own -> return their own 
    // patient tries to access others -> Warning/Denied 
    return null; 
  }
  public void ReplaceRecordContent(RecordId recordId, String newContent, AuthenticatedId requestor){
    if(!isDoctor(requestor) && !isNurse(requestor)){
      return;
    } 
    if(!isAssociated(requestor) && !isSameDivision(requestor)){
      return;
    }
    MedicalRecordEntry entry = records.stream().filter(r -> r.recordId == recordId).findAny().orElse(null);
    if(entry != null){
      entry.content = newContent;
    }
  }
  public void createNewRecord(UserId patient, UserId nurse, AuthenticatedId requestor){
    if(isDoctor(requestor)){
      counter++;
      MedicalRecordEntry entry = new MedicalRecordEntry(new RecordId(counter), patient, nurse, requestor.id(), divisionOf(requestor), "");
      records.add(entry);
    }
  }
  public void deleteRecord(RecordId recordId, AuthenticatedId requestor){
    if (isAuthority(requestor)){
      records.removeIf(r -> r.recordId == recordId);
    }
  }
  private boolean isAuthority(AuthenticatedId requestor){
    return false;
  }  
  private boolean isDoctor(AuthenticatedId requestor){
    return false;
  }
  private boolean isNurse(AuthenticatedId requestor){
    return false;
  }
  private boolean isAssociated(AuthenticatedId requestor){
    return false;
  }
  private boolean isSameDivision(AuthenticatedId requestor){
    return false;
  }
  private String divisionOf(AuthenticatedId requestor){
    return "todo";
  }
}
