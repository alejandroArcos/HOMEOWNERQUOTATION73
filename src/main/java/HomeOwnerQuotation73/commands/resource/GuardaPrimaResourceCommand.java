package HomeOwnerQuotation73.commands.resource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.CaratulaResponse;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso3;

import java.io.PrintWriter;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;

@Component(
	    immediate = true,
	    property = {
		        "javax.portlet.name="+ HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73,
		        "mvc.command.name=/cotizadores/paso3/guardaPrima"
	    },
	    service = MVCResourceCommand.class
	)

public class GuardaPrimaResourceCommand extends BaseMVCResourceCommand {
	
	@Reference
	CotizadorPaso3 _ServicePaso3;

	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		
		int cotizacion = ParamUtil.getInteger(resourceRequest, "cotizacion");
		int version = ParamUtil.getInteger(resourceRequest, "version");
		String pantalla = ParamUtil.getString(resourceRequest, "pantalla");
		String lista = ParamUtil.getString(resourceRequest, "lista");
		String usuario = "";
		double p_gastos = ParamUtil.getDouble(resourceRequest, "gastos");
		double p_recargoPago = ParamUtil.getDouble(resourceRequest, "recargoPago");
		
		HttpServletRequest originalRequest = PortalUtil
				.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourceRequest));
		int idPerfilUser = (int) originalRequest.getSession().getAttribute("idPerfil");
		
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		usuario = user.getScreenName();
		
		Gson gson = new Gson();
		
		JsonArray p_lista = gson.fromJson(lista, JsonArray.class);
		
		CaratulaResponse response = _ServicePaso3.calculaPrimaObjetivo(cotizacion, version, idPerfilUser,
				p_recargoPago, p_gastos, p_lista, usuario, pantalla);
		
		String jsonString = gson.toJson(response);
		
		PrintWriter writer = resourceResponse.getWriter();
		writer.write(jsonString);
		
	}

}
