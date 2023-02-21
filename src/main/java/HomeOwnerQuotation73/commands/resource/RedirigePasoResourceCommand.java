package HomeOwnerQuotation73.commands.resource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.InfoCotizacion;
import com.tokio.pa.cotizadorModularServices.Util.CotizadorModularUtil;

import java.io.PrintWriter;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;

@Component(
		immediate = true,
		property = {
				"javax.portlet.name=" + HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73,
				"mvc.command.name=/cotizadores/redirigePasoX" 
				},
		service = MVCResourceCommand.class)

public class RedirigePasoResourceCommand extends BaseMVCResourceCommand{
	@Override
	protected void doServeResource(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws Exception {
		Gson gson = new Gson();
		JsonObject infoUrl = new JsonObject();
		String infoCot = ParamUtil.getString(resourceRequest, "infoCot");
		System.err.println("infoCot: " + infoCot);
		String paso = ParamUtil.getString(resourceRequest, "paso");
		System.err.println("paso: " + paso);
		InfoCotizacion infCot = gson.fromJson(infoCot, InfoCotizacion.class);
		System.err.println("infCot: " + infCot);
		String url = generaUrl(infCot, resourceRequest, paso);
		System.err.println("url: " + url);

		if(Validator.isNull(url)){
			infoUrl.addProperty("code", 2);
			infoUrl.addProperty("msg", "Error al redireccionar");
		}else{
			infoUrl.addProperty("code", 0);
			infoUrl.addProperty("msg", url);
		}
		
		System.err.println("infoUrl: " + infoUrl);
		PrintWriter writer = resourceResponse.getWriter();
		writer.write(infoUrl.toString());
	}
	
	/**
	 * @param infCot
	 * @param resourceRequest
	 * @param paso recordar que el formato del paso es--> /pasoX, donde la x es al paso donde va 
	 * @return
	 */
	private String generaUrl(InfoCotizacion infCot, ResourceRequest resourceRequest, String paso){

		try {

			HttpServletRequest originalRequest = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourceRequest));

			ThemeDisplay themeDisplay = (ThemeDisplay) resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);
			String parametro = CotizadorModularUtil.encodeURL(infCot);
			final long GROUP_ID = themeDisplay.getLayout().getGroupId();
			System.err.println("GROUP_ID:" + GROUP_ID);
			System.err.println("paso:" + paso);
			
			/*
			Layout layout = LayoutLocalServiceUtil.getFriendlyURLLayout(GROUP_ID, true, paso);
			String urlCotizador = layout.getRegularURL(originalRequest);
			*/
			
			return parametro;
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return "";
		}
	}
}
