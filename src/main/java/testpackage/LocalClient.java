package testpackage;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;

public class LocalClient {

    IGenericClient client = null;

    public LocalClient() {}
    public LocalClient(String serverBase) {
        FhirContext ctx = FhirContext.forR4();
        client = ctx.newRestfulGenericClient(serverBase);
    }

    public IBaseResource getResourceById(String id, String resourceType) {
        IBaseResource resource = client.read()
                .resource(resourceType)
                .withId(id)
                .execute();
        return resource;
    }

    public List<Patient> getPatientsByName(String name) {
        Bundle bundle = client.search().forResource(Patient.class)
                .where(Patient.NAME.matches().value(name))
                .returnBundle(Bundle.class).execute();
        List<Patient> results = (List<Patient>)(List<?>) fetchCompleteBundleAsList(bundle);
        return results;
    }


    public Bundle fetchCompleteBundle(Bundle partialBundle) {
        Bundle completeBundle = new Bundle();
        for (int i = 0; i < partialBundle.getTotal(); i++) {
            completeBundle.addEntry(partialBundle.getEntry().get(i));
        }
        while (partialBundle.getLink(IBaseBundle.LINK_NEXT) != null) {
            partialBundle = client.loadPage().next(partialBundle).execute();
            for (int i = 0; i < partialBundle.getTotal(); i++) {
                completeBundle.addEntry(partialBundle.getEntry().get(i));
            }
        }
        return completeBundle;
    }

    public List<IBaseResource> fetchCompleteBundleAsList(Bundle partialBundle) {
        ArrayList<IBaseResource> completeBundleAsList = new ArrayList<>();
        completeBundleAsList.addAll(BundleUtil.toListOfResources(client.getFhirContext(), partialBundle));
        while (partialBundle.getLink(IBaseBundle.LINK_NEXT) != null) {
            partialBundle = client.loadPage().next(partialBundle).execute();
            completeBundleAsList.addAll(BundleUtil.toListOfResources(client.getFhirContext(), partialBundle));
        }
        return completeBundleAsList;
    }
}
