package HomeOwnerQuotation73.commands.resource;

import com.google.gson.Gson;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.SlipResponse;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso3Familiar;

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
		        "mvc.command.name=/getSlip"
	    },
	    service = MVCResourceCommand.class
	)

public class SlipResourceCommand extends BaseMVCResourceCommand{
	@Reference
	CotizadorPaso3Familiar _ServicePaso3;
	
	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		String usuario = "";
		String pantalla = HomeOwnerQuotation73PortletKeys.PANTALLA_FAMILIAR;

		String cotizacion = ParamUtil.getString(resourceRequest, "cotizacion");
		int version = ParamUtil.getInteger(resourceRequest, "version");
		int p_word = ParamUtil.getInteger(resourceRequest, "word");
		
		try{
			User user = (User) resourceRequest.getAttribute(WebKeys.USER);
			usuario = user.getScreenName();
		} catch(Exception e){
			SessionErrors.add(resourceRequest, "errorUsuario" );
			SessionMessages.add(resourceRequest, PortalUtil.getPortletId(resourceRequest) + SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
		}
		
		SlipResponse response = fGetSlip(cotizacion, version, usuario, pantalla, p_word);
		
		Gson gson = new Gson();
		String jsonString = gson.toJson(response);
		
		PrintWriter writer = resourceResponse.getWriter();
		writer.write(jsonString);
	}
	
	private SlipResponse fGetSlip(String cotizacion, int version, String usuario, String pantalla, int p_word) {
		try {
			return _ServicePaso3.getSlip(cotizacion, version, usuario, pantalla, p_word);
			/*return null;*/
		} catch (Exception e) {
			/* TODO Auto-generated catch block	*/
			return null;
		}
	}
	
}
