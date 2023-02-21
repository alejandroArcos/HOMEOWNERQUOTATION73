package HomeOwnerQuotation73.commands.resource;

import com.google.gson.JsonArray;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.PortalUtil;
import com.tokio.cotizadorUtilities.excel.Interface.CargaMasivaFamiliar;

import java.io.File;
import java.io.PrintWriter;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;

@Component(
		immediate = true, property = { 
				"javax.portlet.name=" + HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73, 
				"mvc.command.name=/cotizadores/paso1/cargaMasiva" }, service = MVCResourceCommand.class
)

public class CargaMasivaResourceCommand extends BaseMVCResourceCommand {
	
	@Reference
	CargaMasivaFamiliar _CUServices;

	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		// TODO Auto-generated method stub

		System.out.println("Entramos a la lectura del Archivo Excel");
		
		UploadPortletRequest uploadRequest = PortalUtil.getUploadPortletRequest(resourceRequest);
		
		File carga = uploadRequest.getFile("docAgenSusc");
		
		JsonArray jsonArray = _CUServices.readFile(carga);
		String jsonString = jsonArray.toString();
		
		PrintWriter writer = resourceResponse.getWriter();
		writer.write(jsonString);
		
	}

}
