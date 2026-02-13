import access_example.UserId;
import access_example.UserInfo;
import access_example.MedicalRecordEntry;
import access_example.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Optional;

public class BackendEntry {
  private List<MedicalRecordEntry> records = new ArrayList<>();
  private Log log;
  private HashMap<UserId, UserInfo> users;

  public List<MedicalRecordEntry> requestPatientRecords(UserId patient, AuthenticatedId requestor){
    // nurse/doctor tries to access -> retrun those in the same division or with them listed in it 
    // patient tries to access their own -> return their own 
    // patient tries to access others -> Warning/Denied 
    return null; 
  }
  public void ReplaceRecordContent(RecordId recordID, AuthenticatedId requestor){
    // nurse/ doctor associated with record -> pass 
    // else -> warning 
  }
  public MedicalRecordEntry createNewRecord(AuthenticatedId creator_id, UserId patient_id, UserId nurse_id){
    // if doctor -> adds a new entry with them as the doctor under their divisision
    // else -> rejection / warning; 
    return null;
  }
  public void deleteRecord(AuthenticatedId id, RecordId recordId){
    // if authority -> yes 
    // else -> rejection /warning 
  }
}
