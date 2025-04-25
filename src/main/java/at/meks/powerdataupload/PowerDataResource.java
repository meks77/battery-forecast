package at.meks.powerdataupload;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Comparator;

@Path("/upload")
public class PowerDataResource {

    @Inject PowerDataRepo powerDataRepo;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public RestResponse<?> hello(@RestForm FileUpload consumptionFile, @RestForm FileUpload fedInFile, @Context UriInfo uriInfo)
            throws IOException {
        try(Reader fedInReader = Files.newBufferedReader(fedInFile.uploadedFile());
            Reader consumptionReader = Files.newBufferedReader(consumptionFile.uploadedFile())) {
            PowerFileParser parser = new PowerFileParser(fedInReader, consumptionReader);
            parser.stream().forEach(powerDataRepo::add);
        }

        Integer year = powerDataRepo.years().stream().max(Comparator.naturalOrder()).orElse(LocalDate.now().getYear());
        return RestResponse.seeOther(uriInfo.getBaseUriBuilder().path("/index.html").build());
    }
}
