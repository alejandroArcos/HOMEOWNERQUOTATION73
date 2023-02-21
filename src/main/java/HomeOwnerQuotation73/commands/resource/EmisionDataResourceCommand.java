package HomeOwnerQuotation73.commands.resource;

import com.google.gson.Gson;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.EmisionDataResponse;
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
		        "javax.portlet.name="+ HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73,
		        "mvc.command.name=/emisionData"
	    },
	    service = MVCResourceCommand.class
	)

public class EmisionDataResourceCommand extends BaseMVCResourceCommand{
	@Reference
	CotizadorPaso3 _ServicePaso3;
	
	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		String pantalla = HomeOwnerQuotation73PortletKeys.CotizadorPaso3;
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		String usuario = user.getScreenName();
		int version = ParamUtil.getInteger(resourceRequest, "version");
		int cotizacion = ParamUtil.getInteger(resourceRequest, "cotizacion");
		
		EmisionDataResponse envio = fEmisionData(cotizacion, version, usuario, pantalla);	
		
		Gson gson = new Gson();
		String json = gson.toJson(envio);
		PrintWriter writer = resourceResponse.getWriter();
		writer.write(json);	
	}
	
	private EmisionDataResponse fEmisionData (int cotizacion, int version, String usuario, String pantalla){
		try {
			return _ServicePaso3.getEmisionData(cotizacion, version, usuario, pantalla);
		} catch (Exception e) {
			return null;
		}
	}
}
