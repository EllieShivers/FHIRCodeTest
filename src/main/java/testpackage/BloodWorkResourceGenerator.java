package testpackage;

import java.io.*;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.hl7.fhir.r4.model.*;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;

public class BloodWorkResourceGenerator extends LocalClient{

    static int CBC = 0;
    static int BMP = 1;
    static int CMP = 2;

    static ArrayList<CodeSet> PANELS = new ArrayList<>(Arrays.asList(
            new CodeSet("CBC WO Differential panel - Cord blood", "47288-6", "http://loinc.org"),
            new CodeSet("Basic metabolic 2000 panel - Serum or Plasma", "24321-2", "http://loinc.org"),
            new CodeSet("Comprehensive metabolic 2000 panel - Serum or Plasma", "24323-8", "http://loinc.org")
            ));

    static ArrayList<CodeSet> BMP_TESTS = new ArrayList<>(Arrays.asList(
            new CodeSet("Glucose [Mass/volume] in Serum or Plasma","2345-7", "http://loinc.org", "mg/dL", 74, 106),
            new CodeSet("Urea nitrogen [Mass/volume] in Serum or Plasma", "3094-0", "http://loinc.org", "mg/mL", 10, 20),
            new CodeSet("Creatinine [Mass/volume] in Serum or Plasma", "2160-0", "http://loinc.org", "mg/dL", 0, 1.5),
            new CodeSet("Urea nitrogen/Creatinine [Mass Ratio] in Serum or Plasma", "3097-3", "http://loinc.org", "mg/mg{creat}", 6, 22),
            new CodeSet("Glomerular filtration rate/1.73 sq M.predicted [Volume Rate/Area] in Serum or Plasma by Creatinine-based formula (MDRD)", "33914-3", "http://loinc.org"),
            new CodeSet("Calcium [Mass/volume] in Serum or Plasma", "17861-6", "http://loinc.org"),
            new CodeSet("Sodium [Moles/volume] in Serum or Plasma", "2951-2", "http://loinc.org"),
            new CodeSet("Potassium [Moles/volume] in Serum or Plasma", "2823-3", "http://loinc.org"),
            new CodeSet("Chloride [Moles/volume] in Serum or Plasma", "1075-0", "http://loinc.org"),
            new CodeSet("Bicarbonate [Moles/volume] in Serum or Plasma", "1963-8", "http://loinc.org"),
            new CodeSet("Carbon dioxide, total [Moles/volume] in Serum or Plasma", "2028-9", "http://loinc.org"),
            new CodeSet("Anion gap in Serum or Plasma", "33037-3", "http://loinc.org")
            ));

    public BloodWorkResourceGenerator(String serverBase)  {
        super(serverBase);
    }

    public MethodOutcome createPanelForPatient(String patientId, String date, int type) {
        // Create basic objects and set bundle type to transcation
        Bundle transactionBundle = new Bundle();
        transactionBundle.setType(Bundle.BundleType.TRANSACTION);
        DiagnosticReport diagnosticReport = new DiagnosticReport();
        Coding coding = new Coding();

        // Set DiagnosticReportId for Testing TODO: Change to method parameter
        diagnosticReport.setId("bwc-dr-01");

        // Setup Panel Coding
        coding.setCode(PANELS.get(type).code);
        coding.setDisplay(PANELS.get(type).display);
        coding.setSystem(PANELS.get(type).system);
        CodeableConcept codeableConcept = new CodeableConcept().addCoding(coding);
        diagnosticReport.setCode(codeableConcept);

        // Set Status to Final
        diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);

        // Set Subject
        diagnosticReport.setSubject(new Reference().setReference("Patient/" + patientId));

        // Set Date
        DateTimeType effectiveDateTimeType = new DateTimeType(date);
        diagnosticReport.setEffective(effectiveDateTimeType);

        // Create Related Observations
        ArrayList<Observation> observations = createObservationResourcesforPanel(patientId, type, diagnosticReport.getIdElement().getIdPart());

        // Add Observations to Reference list.
        for (Observation observation : observations) {
            diagnosticReport.addResult().setResource(observation);
        }

        // Add to Bundle
        transactionBundle.addEntry().setResource(diagnosticReport);
        for (Observation observation : observations) {
            transactionBundle.addEntry().setResource(observation);
        }

        //Output JSON for testing
        IParser parser = client.getFhirContext().newJsonParser();
        String serialized = parser.encodeResourceToString(transactionBundle);
        System.out.println(serialized);

        // Return
        return new MethodOutcome();
    }

    public ArrayList<Observation> createObservationResourcesforPanel(String patientId, int type, String reportId) {
        ArrayList<Observation> observations = new ArrayList<>();
        if (type == 1) {
            int counter = 1;
            for (CodeSet codeSet : BMP_TESTS){
                // Create observation and set ID, then increment counter for ID
                Observation observation = new Observation();
                observation.setId(reportId + "-" + counter++);
                // Set status and subject
                observation.setStatus(Observation.ObservationStatus.FINAL);
                observation.setSubject(new Reference().setReference("Patient/" + patientId));
                // Setup and add coding
                Coding coding = new Coding();
                coding.setSystem(codeSet.system);
                coding.setCode(codeSet.code);
                coding.setDisplay(codeSet.display);
                observation.setCode(new CodeableConcept().addCoding(coding));

                // Add Reference ranges if Available
                if (codeSet.low != null) {
                    Quantity low = new Quantity();
                    low.setSystem(CodeSet.UNITS_OF_MEASURE_SYSTEM);
                    low.setValue(codeSet.getLow());
                    low.setUnit(codeSet.unit);
                    low.setCode(codeSet.unit);

                    observation.addReferenceRange().setLow(low);
                }
                if (codeSet.high != null) {
                    Quantity high = new Quantity();
                    high.setSystem(CodeSet.UNITS_OF_MEASURE_SYSTEM);
                    high.setValue(codeSet.getHigh());
                    high.setUnit(codeSet.unit);
                    high.setCode(codeSet.unit);
                    observation.addReferenceRange().setHigh(high);
                }
                // Setup value
                Quantity valueQuantity = new Quantity();
                valueQuantity.setSystem(CodeSet.UNITS_OF_MEASURE_SYSTEM);
                valueQuantity.setUnit(codeSet.unit);
                valueQuantity.setCode(codeSet.unit);

                // Generate Random Value within Reference Range


                // Add Value to observation
                observation.setValue(valueQuantity);

                // Add the observation to the array
                observations.add(observation);
            }
        }
        return observations;
    }


    public String getOfficialDisplayName(String code){
        String name = "undefined";
        ArrayList<Parameters.ParametersParameterComponent> parameters = new ArrayList<Parameters.ParametersParameterComponent>();
        Parameters codeDetails;
        try {
            codeDetails = getDetailsOfLoincCode(code);
            parameters = (ArrayList<Parameters.ParametersParameterComponent>) codeDetails.getParameter();
        } catch (IOException e) {
            e.printStackTrace();
            return name;
        }
        System.out.println("Param Size: " + parameters.size());

        for (Parameters.ParametersParameterComponent param : parameters) {
            if (param.getName().matches("display"))
                name = param.getName() + ": " + param.getValue();
        }
        return name;
    }

    public Parameters getDetailsOfLoincCode(String code) throws IOException {
        Parameters details;
        URL url = new URL("https://fhir.loinc.org/CodeSystem/$lookup?system=http://loinc.org&code=" + code);
        System.out.print("URL: " + url);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        String user_name="ellieshivers";
        String password="Everclear11";

        String userCredentials = user_name+":"+password;
        String basicAuth = "Basic " + new String(new Base64().encode(userCredentials.getBytes()));
        con.setRequestProperty ("Authorization", basicAuth);
        con.setRequestProperty("Accept","application/json");

        int responseCode = con.getResponseCode();

        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);
        String jsonString = convertStreamToString(con.getInputStream());
        IParser parser = client.getFhirContext().newJsonParser();
        details = parser.parseResource(Parameters.class, jsonString);
        return details;
    }

    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
