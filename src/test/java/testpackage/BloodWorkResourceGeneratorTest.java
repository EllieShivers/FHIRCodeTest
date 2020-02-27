package testpackage;

import org.hl7.fhir.r4.model.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BloodWorkResourceGeneratorTest {
    String serverBase = "https://apps.hdap.gatech.edu/hapiR4/baseR4/";

    @Test
    void createPanelForPatient() {
        BloodWorkResourceGenerator client = new BloodWorkResourceGenerator(serverBase);

        String patientId = "33428";
        String date = "2020-02-03T12:00:00.000+05:00"; // YYYY-MM-DD
        int type = 1;
        client.createPanelForPatient(patientId, date, type);

    }

    @Test
    void getDetailsOfLoincCode() {
        BloodWorkResourceGenerator client = new BloodWorkResourceGenerator(serverBase);
        String code = "47288-6";
        Parameters codeDetails = new Parameters();
        ArrayList<Parameters.ParametersParameterComponent> parameters = new ArrayList<Parameters.ParametersParameterComponent>();
        try {
            codeDetails = client.getDetailsOfLoincCode(code);
            parameters = (ArrayList<Parameters.ParametersParameterComponent>) codeDetails.getParameter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Param Size: " + parameters.size());

        for (Parameters.ParametersParameterComponent param : parameters) {
            if (param.getName().matches("display"))
                System.out.println(param.getName() + ": " + param.getValue());
        }

    }
}
