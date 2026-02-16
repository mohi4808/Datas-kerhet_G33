package access_example;
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
  private Log log = new Log();
  private HashMap<UserId, UserInfo> users = new HashMap<>();
  private int counter = 0;

  public BackendEntry(){
    // Some innitial hard coded users
    UserId doctorA = new UserId(1);
    UserId doctorB = new UserId(2);
    UserId nurseA = new UserId(3);
    UserId patient1 = new UserId(4);

    users.put(doctorA, UserInfo.newDoctor("Dr Doctor", "A"));
    users.put(doctorB, UserInfo.newDoctor("Dr Feelgood", "B"));
    users.put(nurseA, UserInfo.newNurse("R.N. Annie Wilkes", "A"));
    users.put(patient1, UserInfo.newPatient("John Doe"));
    
    RecordId record1 = createNewRecord(patient1, nurseA, new AuthenticatedId(doctorA));
    requestPatientRecords(patient1, new AuthenticatedId(patient1));
    requestPatientRecords(patient1, new AuthenticatedId(doctorB));
    log.print();
  }

  public List<MedicalRecordEntry> requestPatientRecords(UserId patient, AuthenticatedId requestor){
    // fetch all records the user has access to 
    List<MedicalRecordEntry> accesibleRecords = records.stream()
      .filter(r -> r.patient == patient)
      .filter(r-> r.patient == requestor.id() || isAssociated(r, requestor) || isSameDivision(r, requestor) || isAuthority(requestor))
      .collect(toList());

    log.start();
    log.append("User " + requestor.toString() + " requested patient records for patient " + patient.toString() + "\n");
    log.append("records handed out:\n");
    accesibleRecords
      .stream()
      .forEach(r -> log.append(r.recordId.toString() + "\n"));
    log.end();

    return accesibleRecords;
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
  public RecordId createNewRecord(UserId patient, UserId nurse, AuthenticatedId requestor){
    if(isDoctor(requestor)){
      counter++;
      MedicalRecordEntry entry = new MedicalRecordEntry(new RecordId(counter), patient, nurse, requestor.id(), divisionOf(requestor).get());
      records.add(entry);
    }
    return new RecordId(counter);
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
