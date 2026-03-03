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

  public UserInfo whoAmI(AuthenticatedId requestor){
    return users.get(requestor.id());
  } 

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
    
    RecordId record1 = createNewRecord(patient1, nurseA, new AuthenticatedId(doctorA)).get();
    requestPatientRecords(patient1, new AuthenticatedId(patient1));
    requestPatientRecords(patient1, new AuthenticatedId(doctorB));
    replaceRecordContent(record1, "test ", new AuthenticatedId(patient1));
    replaceRecordContent(record1, "test ", new AuthenticatedId(nurseA));
    replaceRecordContent(record1, "test ", new AuthenticatedId(doctorB));
    //log.print();
  }

  public List<MedicalRecordEntry> requestPatientRecords(UserId patient, AuthenticatedId requestor){
    // fetch all records the user has access to 
    List<MedicalRecordEntry> accesibleRecords = records.stream()
      .filter(r -> r.patient.equals(patient))
      .filter(r-> r.patient.equals(requestor.id()) || isAssociated(r, requestor) || isSameDivision(r, requestor) || isAuthority(requestor))
      .collect(toList());

    log.start();
    log.append("User " + requestor.toString() + " requested patient records for patient " + patient.toString() + "\n");
    log.append("records handed out:\n");
    accesibleRecords
      .stream()
      .forEach(r -> log.append(r.recordId.toString() + "\n"));
    return accesibleRecords;
  }
  public void replaceRecordContent(RecordId recordId, String newContent, AuthenticatedId requestor){
    log.start();
    log.append("User " + requestor.toString() + " requested to change the record " + recordId.toString() + "\n"); 
    if(!isDoctor(requestor) && !isNurse(requestor) && !isAuthority(requestor)){ 
      log.append("DENIED: unsuitable role, patients can't change record content\n");
      return;
    } 
    MedicalRecordEntry entry = records.stream().filter(r -> r.recordId.equals(recordId)).findAny().orElse(null);
    if(entry == null){
      log.append("ERROR: no such entry exists\n");
      return;
    }
    if(!isAssociated(entry,requestor) && !isSameDivision(entry, requestor)){
      log.append("DENIED: user is not associated with the record nor in the same division \n");
      return;
    }
    log.append("user was allowed access\n");
    entry.content = newContent;
  }
  public Optional<RecordId> createNewRecord(UserId patient, UserId nurse, AuthenticatedId requestor){
    log.start();
    log.append("User " + requestor.toString() + " request creation of new record for patient " + patient.toString() + "\n");
    if(isDoctor(requestor)){
      counter++;
      RecordId recordId = new RecordId(counter);
      MedicalRecordEntry entry = new MedicalRecordEntry(recordId, patient, nurse, requestor.id(), divisionOf(requestor).get());
      records.add(entry);
      log.append("Record " + recordId.toString() + " was successfully created\n");
      return Optional.of(recordId);
    } else {
      log.append("DENIED: not a doctor\n");
      return Optional.empty();
    }
  }
  public void deleteRecord(RecordId recordId, AuthenticatedId requestor){
    log.start();
    log.append("User " + requestor.toString() + " request deletion of record " + recordId.toString() + "\n");
    if (isAuthority(requestor)){
      log.append("Deletion allowed, if the record existed it has now been deleted");
      records.removeIf(r -> r.recordId.equals(recordId));
    } else {
      log.append("DENIED: user is not an authority");
    }

  }
  private boolean isAuthority(AuthenticatedId requestor){
    UserInfo info = users.get(requestor.id());
    return info.type.equals(UserType.AUTHORITY);
  }  
  private boolean isDoctor(AuthenticatedId requestor){
    UserInfo info = users.get(requestor.id());
    return info.type.equals(UserType.DOCTOR);
  }
  private boolean isNurse(AuthenticatedId requestor){
    UserInfo info = users.get(requestor.id());
    return info.type.equals(UserType.NURSE);
  }
  private boolean isAssociated(MedicalRecordEntry entry, AuthenticatedId requestor){
    return (entry.nurse.equals(requestor.id()) || entry.doctor.equals(requestor.id()));
  }
  private boolean isSameDivision(MedicalRecordEntry entry, AuthenticatedId requestor){
    if (!divisionOf(requestor).isPresent()){
      return false;
    } 
    return entry.division.equals(divisionOf(requestor).get());
  }
  private Optional<String> divisionOf(AuthenticatedId requestor){
    UserInfo info = users.get(requestor.id());
    return info.division;
  }
}
