package at.meks.powerdataupload

import at.meks.powerdata.PowerDataRepo
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.UriInfo
import org.jboss.resteasy.reactive.RestForm
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.multipart.FileUpload
import java.nio.file.Files

@Path("/upload")
class PowerDataResource(private val powerDataRepo: PowerDataRepo) {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun hello(@RestForm consumptionFile: FileUpload, @RestForm fedInFile: FileUpload, @Context uriInfo: UriInfo): RestResponse<Any> {
        val fedInReader = Files.newBufferedReader(fedInFile.uploadedFile())
        val consumptionReader = Files.newBufferedReader(consumptionFile.uploadedFile())
        val parser = PowerFileParser(fedInReader, consumptionReader)

        parser.stream().forEach { powerData -> powerDataRepo.add(powerData) }

        return RestResponse.seeOther(uriInfo.getBaseUriBuilder()
            .path("/index.html").build())
    }
}
