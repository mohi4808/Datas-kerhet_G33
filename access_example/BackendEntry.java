import access_example.UserId;
import access_example.UserInfo;
import access_example.UserType;
import access_example.MedicalRecordEntry;
import access_example.Log;
import access_example.RecordId;
import access_example.AuthenticatedId;
import static java.util.stream.Collectors.*;

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
    return records.stream()
      .filter(r -> r.patient == patient)
      .filter(r-> r.patient == requestor.id() || isAssociated(r, requestor) || isSameDivision(r, requestor))
      .collect(toList());
  }
  public void ReplaceRecordContent(RecordId recordId, String newContent, AuthenticatedId requestor){
    if(!isDoctor(requestor) && !isNurse(requestor)){
      return;
    } 
    MedicalRecordEntry entry = records.stream().filter(r -> r.recordId == recordId).findAny().orElse(null);
    if(entry == null){
      return;
    }
    if(!isAssociated(entry,requestor) && !isSameDivision(entry, requestor)){
      return;
    }
    entry.content = newContent;
  }
  public void createNewRecord(UserId patient, UserId nurse, AuthenticatedId requestor){
    if(isDoctor(requestor)){
      counter++;
      MedicalRecordEntry entry = new MedicalRecordEntry(new RecordId(counter), patient, nurse, requestor.id(), divisionOf(requestor).get());
      records.add(entry);
    }
  }
  public void deleteRecord(RecordId recordId, AuthenticatedId requestor){
    if (isAuthority(requestor)){
      records.removeIf(r -> r.recordId == recordId);
    }
  }
  private boolean isAuthority(AuthenticatedId requestor){
    UserInfo info = users.get(requestor.id());
    return info.type == UserType.AUTHORITY;
  }  
  private boolean isDoctor(AuthenticatedId requestor){
    UserInfo info = users.get(requestor.id());
    return info.type == UserType.DOCTOR;
  }
  private boolean isNurse(AuthenticatedId requestor){
    UserInfo info = users.get(requestor.id());
    return info.type == UserType.NURSE;
  }
  private boolean isAssociated(MedicalRecordEntry entry, AuthenticatedId requestor){
    return (entry.nurse == requestor.id() || entry.doctor == requestor.id());
  }
  private boolean isSameDivision(MedicalRecordEntry entry, AuthenticatedId requestor){
    if (!divisionOf(requestor).isPresent()){
      return false;
    } 
    return entry.division == divisionOf(requestor).get();
  }
  private Optional<String> divisionOf(AuthenticatedId requestor){
    UserInfo info = users.get(requestor.id());
    return info.division;
  }
}
