package HomeOwnerQuotation73.commands.resource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.DocumentoResponse;
import com.tokio.pa.cotizadorModularServices.Bean.IdCarpetaResponse;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorGenerico;

import java.io.PrintWriter;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;

@Component(immediate = true, property = {
		"javax.portlet.name=" + HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73,
		"mvc.command.name=/getDocumentosExistentes" }, service = MVCResourceCommand.class)

public class GetDocExistentes extends BaseMVCResourceCommand {
	@Reference
	CotizadorGenerico _ServiceGenerico;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand#
	 * doServeResource(javax.portlet.ResourceRequest,
	 * javax.portlet.ResourceResponse)
	 */
	@Override
	protected void doServeResource(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws Exception {
		// TODO Auto-generated method stub
		
		PrintWriter writer = resourceResponse.getWriter();
		String jsonString = null;
		try {		
			int cotizacion = ParamUtil.getInteger(resourceRequest, "cotizacion");
			int folio = ParamUtil.getInteger(resourceRequest, "folio");
			int version = ParamUtil.getInteger(resourceRequest, "version");
			int idDocumento = ParamUtil.getInteger(resourceRequest, "idDoc");
			int idCatalogoDetalle = ParamUtil.getInteger(resourceRequest, "idCatDet");
			
			
			User user = (User) resourceRequest.getAttribute(WebKeys.USER);
			String usuario = user.getScreenName();
			
			IdCarpetaResponse carpeta = _ServiceGenerico.SeleccionaIdCarpeta(folio, cotizacion, version);
			
			/*
			StringBuilder builder = new StringBuilder();
			builder.append("{");
			builder.append("\"idCarpeta\":" + carpeta.getIdCarpeta() + ",");
			builder.append("\"idDocumento\":" + idDocumento + ",");
			builder.append("\"idCatalogoDetalle\":" + idCatalogoDetalle + ",");
			builder.append("\"documento\":\"\",");
			builder.append("\"nombre\":\"\",");
			builder.append("\"extension\":\"\"");
			builder.append("}");
			*/
			
			JsonArray jsonDocumentos = new JsonArray();
			JsonObject enviaDocumentos = new JsonObject();
			
			enviaDocumentos.addProperty("idCarpeta", carpeta.getIdCarpeta());
			enviaDocumentos.addProperty("idDocumento", idDocumento);
			enviaDocumentos.addProperty("idCatalogoDetalle", idCatalogoDetalle);
			enviaDocumentos.addProperty("documento", "");
			enviaDocumentos.addProperty("nombre", "");
			enviaDocumentos.addProperty("extension", "");
			
			jsonDocumentos.add(enviaDocumentos);
			
			int rowNum = 0;
			String tipTransaccion = "B";	
			int activo = 1;
			String tipo = HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73;
			int idAsigna = 0;
			String parametros = "";
			String pantalla = HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73;	
//			String jsonDocumentos = builder.toString();
			
			
			DocumentoResponse doc = _ServiceGenerico
					.wsDocumentos(
							rowNum, tipTransaccion, jsonDocumentos, activo, tipo, idAsigna, parametros, usuario, pantalla);
			if(doc.getTotalRow() > 0){
				Gson gson = new Gson();
				 jsonString = gson.toJson(doc.getLista().get(0));
			}else{
				jsonString = "{\"code\" : \"4\", \"msg\" : \"No se encontro el archivo\" }";
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			jsonString = "{\"code\" : \"5\", \"msg\" : \"Error al consultar la información\" }";
		}
		writer.write(jsonString);
	}

}
