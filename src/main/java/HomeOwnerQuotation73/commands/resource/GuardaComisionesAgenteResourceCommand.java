package HomeOwnerQuotation73.commands.resource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.ComisionesResponse;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso3;

import java.io.PrintWriter;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;

@Component(
		immediate = true,
		property = {
				"javax.portlet.name=" + HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73,
					"mvc.command.name=/cotizadores/paso3/guardaComisionesAgente" 
				},
		service = MVCResourceCommand.class)

public class GuardaComisionesAgenteResourceCommand extends BaseMVCResourceCommand {
	
	@Reference
	CotizadorPaso3 _CMSP3;

	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		
		Gson gson = new Gson();
		
		String stringComisiones = ParamUtil.getString(resourceRequest, "comisiones");
		JsonArray p_comisiones = gson.fromJson(stringComisiones, JsonArray.class);
		
		int p_cotizacion = ParamUtil.getInteger(resourceRequest, "cotizacion");
		int p_version = ParamUtil.getInteger(resourceRequest, "version");
		String p_pantalla = ParamUtil.getString(resourceRequest, "pantalla");
		
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		String p_usuario = user.getScreenName();
		
		ComisionesResponse response = _CMSP3.guardaComisionesAgente(p_cotizacion, p_version, p_usuario, p_pantalla, p_comisiones);
		
		String responseString = gson.toJson(response);
		
		PrintWriter writer = resourceResponse.getWriter();
		writer.write(responseString);
		
	}

}
