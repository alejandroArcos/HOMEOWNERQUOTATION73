package HomeOwnerQuotation73.commands.resource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.DocumentoResponse;
import com.tokio.pa.cotizadorModularServices.Bean.IdCarpetaResponse;
import com.tokio.pa.cotizadorModularServices.Bean.InfoCotizacion;
import com.tokio.pa.cotizadorModularServices.Constants.CotizadorModularServiceKey;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorGenerico;

import java.io.File;
import java.io.PrintWriter;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.io.FileUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;

@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73,
		"mvc.command.name=/cotizadores/paso3/cargaSlipWordURL"
	},
	service = MVCResourceCommand.class
)

public class EnviaArchivosResourceCommand extends BaseMVCResourceCommand{
	@Reference
	CotizadorGenerico _ServiceGenerico;
	@Reference
	private DLAppService _dlAppService;
	
	private static final Log _log = LogFactoryUtil.getLog(EnviaArchivosResourceCommand.class);
	
	InfoCotizacion infCot = null;
	
	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		enviaArchivos(resourceRequest, resourceResponse);
	}
	
	void enviaArchivos(ResourceRequest resourceRequest, ResourceResponse resourceResponse) {
		
		try {
			
			Gson gson = new Gson();
			
			User user = (User) resourceRequest.getAttribute(WebKeys.USER);
			String p_usuario = user.getScreenName();
			
			generaInfoCot(resourceRequest);
			
			String auxiliarDoc = HtmlUtil.unescape(ParamUtil.getString(resourceRequest, "auxiliarDoc"));
			
			JSONObject jsonObj;
			jsonObj = JSONFactoryUtil.createJSONObject(auxiliarDoc);
			UploadPortletRequest uploadRequest = PortalUtil.getUploadPortletRequest(resourceRequest);
			
			IdCarpetaResponse carpeta = _ServiceGenerico.SeleccionaIdCarpeta((int)infCot.getFolio(), (int)infCot.getCotizacion(),
					infCot.getVersion());
			
			JsonArray jsonDocumentos = new JsonArray();
			
			String nombre = "file-0";
			File file = uploadRequest.getFile(nombre);

			JsonObject enviaDocumentos = new JsonObject();

			JSONObject jsonObj2;
			jsonObj2 = JSONFactoryUtil.createJSONObject(jsonObj.getString("plantillaSlip"));
			
			String nom = jsonObj2.getString("nom").replace(" ", "_");

			enviaDocumentos.addProperty("nombre", nom);
			enviaDocumentos.addProperty("extension", jsonObj2.getString("ext"));
			enviaDocumentos.addProperty("idCarpeta", carpeta.getIdCarpeta());
			enviaDocumentos.addProperty("idDocumento", "0");
			enviaDocumentos.addProperty("idCatalogoDetalle", 22);

			enviaDocumentos.addProperty("documento",
				Base64.encode(FileUtils.readFileToByteArray(file)));
			enviaDocumentos.addProperty("url", "");
			enviaDocumentos.addProperty("leer", 0);
			
			jsonDocumentos.add(enviaDocumentos);
			
			System.out.println("json :" + enviaDocumentos.toString());
			
			try {
				DocumentoResponse respuesta = _ServiceGenerico.wsDocumentos(
						CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
						CotizadorModularServiceKey.TMX_CTE_TRANSACCION_POST,
						jsonDocumentos, 1, "COTIZACIONES", (int) infCot.getCotizacion(), "",
						p_usuario, HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73);
				
				String responseString = gson.toJson(respuesta);
				
				PrintWriter writer = resourceResponse.getWriter();
				writer.write(responseString);
			} catch (Exception e) {
				// TODO: handle exception
				e.getStackTrace();
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("error en el archivo");
		}
	}
	
	private void generaInfoCot(ResourceRequest resourceRequest){
		
		Gson gson = new Gson();
		
		String infoCot = ParamUtil.getString(resourceRequest, "infoCot");
		System.out.println("infoCot String :" + infoCot);
		infCot = gson.fromJson(infoCot, InfoCotizacion.class);
		System.out.println("infCot objeto: " + infCot);
		
	}
	
	void elimianArchivo(long idDoc){
		try {
			_dlAppService.deleteFileEntry(idDoc);
		} catch (PortalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
