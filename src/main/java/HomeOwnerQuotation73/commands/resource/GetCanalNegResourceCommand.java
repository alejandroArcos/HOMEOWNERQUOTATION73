package HomeOwnerQuotation73.commands.resource;

import com.google.gson.Gson;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.ListaRegistro;
import com.tokio.pa.cotizadorModularServices.Bean.Registro;
import com.tokio.pa.cotizadorModularServices.Constants.CotizadorModularServiceKey;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso1;

import java.io.PrintWriter;
import java.util.Comparator;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;

@Component(immediate = true, property = { "javax.portlet.name=" + HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73,
"mvc.command.name=/cotizadores/paso1/canalNegocio" }, service = MVCResourceCommand.class)

public class GetCanalNegResourceCommand extends BaseMVCResourceCommand {
	
	@Reference
	CotizadorPaso1 _CMServicesP1;

	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		String p_usuario = user.getScreenName();
		
		String pantalla = ParamUtil.getString(resourceRequest, "pantalla");
		String auxCodigo = ParamUtil.getString(resourceRequest, "codigoAgente");
		
		String auxCodAgente[] = auxCodigo.split("-");
		String codigoAgente = auxCodAgente[0].trim();
		
		ListaRegistro listaCatCanalNegocio = _CMServicesP1.getCanalNegocio(
				CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
				CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
				CotizadorModularServiceKey.LIST_CAT_CAN_NEG,
				CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS,
				codigoAgente,
				p_usuario,
				pantalla);
		
		listaCatCanalNegocio.getLista().sort(Comparator.comparing(Registro::getDescripcion));
		
		System.out.println(listaCatCanalNegocio);
		
		Gson gson = new Gson();
		String jsonString = gson.toJson(listaCatCanalNegocio);
		PrintWriter writer = resourceResponse.getWriter();
		writer.write(jsonString);
	}

}
