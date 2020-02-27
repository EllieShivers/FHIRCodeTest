package testpackage;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocalClientTest {
    //String serverBase = "https://vonk.fire.ly/R4";
    String serverBase = "http://hapi.fhir.org/baseR4/";
    //String serverBase = "https://apps.hdap.gatech.edu/gt-fhir/fhir/";

    @Test
    void testGetResourceById() {
        LocalClient localClient = new LocalClient(serverBase);
        String resourceId = "57";
        String resourceType = "Patient";
        IBaseResource resource = localClient.getResourceById(resourceId, resourceType);
        System.out.println(resource.getClass());
        String expected = "Dr. Prof. Felix Stefan Meyer";
        String actual = "";
        if (resource instanceof Patient) {
            actual = ((Patient)resource).getNameFirstRep().getNameAsSingleString();
        }
        assertEquals(expected, actual);
    }

    @Test
    void testGetPatientsByName() {
        LocalClient localClient = new LocalClient(serverBase);
        String patientName = "john";
        List<Patient> results = localClient.getPatientsByName(patientName);
        System.out.println("Results List Size: " + results.size());
        for (Patient patient : results) {
            System.out.println(patient.getNameFirstRep().getNameAsSingleString());
        }

    }
}
