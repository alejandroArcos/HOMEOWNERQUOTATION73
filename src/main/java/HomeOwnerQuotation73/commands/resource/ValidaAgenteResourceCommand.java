package HomeOwnerQuotation73.commands.resource;

import com.google.gson.Gson;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.tokio.pa.cotizadorModularServices.Bean.SimpleResponse;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorGenerico;

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
		"mvc.command.name=/cotizadores/validaAgente"
	},
	service = MVCResourceCommand.class
)

public class ValidaAgenteResourceCommand extends BaseMVCResourceCommand {
	
	@Reference
	CotizadorGenerico _CMGenerico;
	
	
	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		
		String auxCodigo = ParamUtil.getString(resourceRequest, "codigoAgente");
		int cotizacion = ParamUtil.getInteger(resourceRequest, "cotizacion");
		String codigoAgente = "";
		
		if(!auxCodigo.isEmpty()) {
			String auxCodAgente[] = auxCodigo.split("-");
			codigoAgente = auxCodAgente[0].trim();
		}
		
		SimpleResponse response = _CMGenerico.validaAgentes(cotizacion, codigoAgente,
				HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73);
		
		Gson gson = new Gson();
		String jsonString = gson.toJson(response);
		PrintWriter writer = resourceResponse.getWriter();
		writer.write(jsonString);
		
	}

}
