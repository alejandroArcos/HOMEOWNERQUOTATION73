package HomeOwnerQuotation73.commands.resource;

import com.google.gson.Gson;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.SimpleResponse;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso3Familiar;

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
		        "mvc.command.name=/cotizadores/paso3/validaDescuento"
	    },
	    service = MVCResourceCommand.class
	)

public class ValidarDescuentoPolizaResourceCommand extends BaseMVCResourceCommand {
	
	@Reference
	CotizadorPaso3Familiar _ServicePaso3;

	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		
		int cotizacion = ParamUtil.getInteger(resourceRequest, "cotizacion");
		int version = ParamUtil.getInteger(resourceRequest, "version");
		double primaNueva = ParamUtil.getDouble(resourceRequest, "primaNueva");
		
		HttpServletRequest originalRequest = PortalUtil
				.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourceRequest));
		int idPerfilUser = (int) originalRequest.getSession().getAttribute("idPerfil");
		
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		
		Gson gson = new Gson();
		
		SimpleResponse response = _ServicePaso3.validaDescuento(cotizacion, version, primaNueva,
				idPerfilUser, user.getScreenName(), HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73);		
		
		String jsonString = gson.toJson(response);
		
		PrintWriter writer = resourceResponse.getWriter();
		writer.write(jsonString);	
	}
}